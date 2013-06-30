/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.drain.files;

import com.google.common.base.Charsets;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulses;
import com.rultor.spi.Time;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Buffered with a help of files.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "dir", "origin" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
public final class BufferedDrain implements Drain {

    /**
     * How often to flush, in milliseconds.
     */
    private final transient long maximum;

    /**
     * How long to keep them alive locally.
     */
    private final transient long lifetime;

    /**
     * Directory name.
     */
    private final transient String dir;

    /**
     * Original drain.
     */
    private final transient Drain origin;

    /**
     * Public ctor.
     * @param max How often should be flush, in milliseconds
     * @param age Maximum age of file locally
     * @param folder Folder where to keep all files
     * @param drain Original drain
     * @checkstyle ParameterNumber (5 lines)
     */
    public BufferedDrain(final long max, final long age,
        @NotNull final File folder, @NotNull final Drain drain) {
        this.maximum = max;
        this.lifetime = age;
        this.dir = folder.getAbsolutePath();
        this.origin = drain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final int total;
        final File folder = new File(this.dir);
        if (folder.exists()) {
            total = folder.listFiles().length;
        } else {
            total = 0;
        }
        return Logger.format(
            // @checkstyle LineLength (1 line)
            "%s buffered by %d file(s) `%s` with %[ms]s flush interval and %[ms]s lifetime",
            this.origin,
            this.dir,
            total,
            this.maximum,
            this.lifetime
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pulses pulses() throws IOException {
        return this.origin.pulses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Time date, final Iterable<String> lines)
        throws IOException {
        final PrintWriter out = new PrintWriter(
            new BufferedWriter(new FileWriter(this.extra(date), true))
        );
        for (String line : lines) {
            out.println(line);
        }
        out.close();
        this.watch(date, this.maximum);
        this.watch(date, this.lifetime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read(final Time date) throws IOException {
        final File body = this.body(date);
        final File extra = this.extra(date);
        synchronized (this.dir) {
            if (!body.exists()) {
                IOUtils.copyLarge(
                    this.origin.read(date),
                    new FileOutputStream(body)
                );
            }
            FileUtils.touch(extra);
            this.watch(date, this.maximum);
            this.watch(date, this.lifetime);
            return new SequenceInputStream(
                new FileInputStream(body),
                new FileInputStream(extra)
            );
        }
    }

    /**
     * File is too old.
     * @param file The file
     * @param msec Maximum age in msec
     * @return TRUE if too old
     */
    private boolean expired(final File file, final long msec) {
        return System.currentTimeMillis() - file.lastModified() > msec;
    }

    /**
     * Take a look at this file in some time.
     * @param date Pulse date
     * @param delay In how many milliseconds to do this checking
     * @throws IOException If IO error
     */
    private void watch(final Time date, final long delay) throws IOException {
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                    try {
                        BufferedDrain.this.clean(date);
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        ).start();
    }

    /**
     * Clean files if expired.
     * @param date Pulse date
     * @throws IOException If IO error
     */
    private void clean(final Time date) throws IOException {
        final File body = this.body(date);
        final File extra = this.extra(date);
        synchronized (this.dir) {
            if (extra.exists() && this.expired(body, this.maximum)) {
                this.flush(date);
            }
            if (this.expired(body, this.lifetime)) {
                body.delete();
            }
        }
    }

    /**
     * Flush all accumulated lines to the origin drain.
     * @param date Date of the pulse
     * @throws IOException If IO problem inside
     */
    private void flush(final Time date) throws IOException {
        final File extra = this.extra(date);
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(extra), Charsets.UTF_8)
        );
        final Collection<String> buffered = new LinkedList<String>();
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            buffered.add(line);
        }
        this.origin.append(date, buffered);
        IOUtils.copyLarge(
            new FileInputStream(extra),
            new FileOutputStream(this.body(date))
        );
        extra.delete();
    }

    /**
     * File with body.
     * @param date Date of pulse
     * @return File with body
     */
    private File body(final Time date) {
        return this.file(date, "body");
    }

    /**
     * File with lines.
     * @param date Date of pulse
     * @return File with lines
     */
    private File extra(final Time date) {
        return this.file(date, "extra");
    }

    /**
     * File.
     * @param date Date of pulse
     * @param ext Extension
     * @return File
     */
    private File file(final Time date, final String ext) {
        final File folder = new File(this.dir);
        folder.mkdirs();
        return new File(folder, String.format("%d.%s", date.millis(), ext));
    }

}
