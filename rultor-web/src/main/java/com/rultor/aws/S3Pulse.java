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
package com.rultor.aws;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Pulse;
import com.rultor.spi.Spec;
import com.rultor.spi.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Pulse in Amazon S3.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "key")
@Loggable(Loggable.DEBUG)
final class S3Pulse implements Pulse {

    /**
     * S3 key.
     */
    private final transient Key key;

    /**
     * Public ctor.
     * @param akey S3 key
     */
    protected S3Pulse(final Key akey) {
        this.key = akey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public Date started() {
        try {
            return new Date(
                Long.parseLong(
                    this.find(
                        "started",
                        Long.toString(System.currentTimeMillis())
                    )
                )
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Stage> stages() {
        return new ArrayList<Stage>(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public Spec spec() {
        try {
            return new Spec.Simple(this.find("spec", "java.lang.Integer(0)"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return Caches.INSTANCE.read(this.key);
    }

    /**
     * Find this signal in the stream.
     * @param name Signal name
     * @param def Default value, if not found
     * @return Found value
     * @throws IOException If fails
     */
    private String find(final String name, final String def)
        throws IOException {
        final BufferedReader reader =
            new BufferedReader(new InputStreamReader(this.read()));
        String value = null;
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (Pulse.Signal.exists(line)) {
                final Pulse.Signal signal = Pulse.Signal.valueOf(line);
                if (signal.key().equals(name)) {
                    value = signal.value();
                    break;
                }
            }
        }
        if (value == null) {
            value = def;
        }
        return value;
    }

}
