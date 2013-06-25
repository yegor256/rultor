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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Signal;
import com.rultor.spi.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Text protocol.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "src")
@Loggable(Loggable.DEBUG)
final class Protocol {

    /**
     * Source.
     */
    @Immutable
    public interface Source {
        /**
         * Get input stream.
         * @return Stream
         * @throws IOException If fails
         */
        InputStream stream() throws IOException;
    }

    /**
     * Stream source.
     */
    private final transient Protocol.Source src;

    /**
     * Public ctor.
     * @param source Source
     */
    protected Protocol(final Protocol.Source source) {
        this.src = source;
    }

    /**
     * Find this signal in the stream.
     * @param mnemo Signal name
     * @param def Default value, if not found
     * @return Found value
     * @throws IOException If fails
     */
    public String find(final Signal.Mnemo mnemo, final String def)
        throws IOException {
        final BufferedReader reader =
            new BufferedReader(new InputStreamReader(this.src.stream()));
        String value = null;
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (Signal.exists(line)) {
                final Signal signal = Signal.valueOf(line);
                if (signal.key().equals(mnemo)) {
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

    /**
     * Find all stages.
     * @return Stages found
     * @throws IOException If IO problem inside
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Stage> stages() throws IOException {
        final Collection<Stage> stages = new LinkedList<Stage>();
        final BufferedReader reader =
            new BufferedReader(new InputStreamReader(this.src.stream()));
        final ConcurrentMap<String, Long> starts =
            new ConcurrentHashMap<String, Long>(0);
        while (true) {
            final String txt = reader.readLine();
            if (txt == null) {
                break;
            }
            if (Signal.exists(txt) && Conveyer.Line.Simple.has(txt)) {
                final Conveyer.Line line = Conveyer.Line.Simple.parse(txt);
                final Signal signal = Signal.valueOf(txt);
                if (signal.key().equals(Signal.Mnemo.START)) {
                    starts.put(signal.value(), line.msec());
                } else if (signal.key().equals(Signal.Mnemo.SUCCESS)
                    || signal.key().equals(Signal.Mnemo.FAILURE)) {
                    stages.add(
                        Protocol.toStage(
                            line, signal, starts.get(signal.value())
                        )
                    );
                }
            }
        }
        return Collections.unmodifiableCollection(stages);
    }

    /**
     * Convert signal and line to stage.
     * @param line Line
     * @param signal Signal
     * @param start When started or NULL if unknown
     * @return The stage
     */
    private static Stage toStage(final Conveyer.Line line,
        final Signal signal, final Long start) {
        Stage.Result result;
        if (signal.key().equals(Signal.Mnemo.SUCCESS)) {
            result = Stage.Result.SUCCESS;
        } else {
            result = Stage.Result.FAILURE;
        }
        long begin = 0;
        if (start != null) {
            begin = start;
        }
        return new Stage.Simple(
            result,
            begin,
            line.msec(),
            signal.value()
        );
    }

}
