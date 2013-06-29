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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Drain;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Drain of one directory.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "dir")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class DirectoryDrain implements Drain {

    /**
     * Directory name.
     */
    private final transient String dir;

    /**
     * Public ctor.
     * @param folder Folder where to keep all files
     */
    protected DirectoryDrain(@NotNull final File folder) {
        this.dir = folder.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> pulses() throws IOException {
        final Collection<File> files = FileUtils.listFiles(
            new File(this.dir), new String[] {"log"}, false
        );
        final SortedSet<Long> numbers = new TreeSet<Long>();
        for (File file : files) {
            numbers.add(
                Long.valueOf(
                    FilenameUtils.getBaseName(file.getAbsolutePath())
                )
            );
        }
        return numbers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final long date, final Iterable<String> lines)
        throws IOException {
        final PrintWriter out = new PrintWriter(
            new BufferedWriter(new FileWriter(this.file(date), true))
        );
        for (String line : lines) {
            out.println(line);
        }
        out.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read(final long date) throws IOException {
        final File file = this.file(date);
        final InputStream stream;
        if (file.exists()) {
            stream = new FileInputStream(file);
        } else {
            stream = IOUtils.toInputStream("");
        }
        return stream;
    }

    /**
     * File with body.
     * @param date Date of pulse
     * @return File with body
     */
    private File file(final long date) {
        final File folder = new File(this.dir);
        folder.mkdirs();
        return new File(folder, String.format("%d.log", date));
    }

}
