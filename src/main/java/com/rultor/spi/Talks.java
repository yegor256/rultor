/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.spi;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.jcabi.aspects.Immutable;
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XMLDocument;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Talks in a repo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Talks {

    /**
     * Talk exists already?
     * @param number The number
     * @return TRUE if it exists
     * @since 1.3
     */
    boolean exists(long number);

    /**
     * Get an existing talk (runtime exception if it's absent).
     * @param number The number
     * @return Talk
     * @since 1.3
     */
    Talk get(long number);

    /**
     * Talk exists already?
     * @param name The name
     * @return TRUE if it exists
     */
    boolean exists(String name);

    /**
     * Get an existing talk (runtime exception if it's absent).
     * @param name The name
     * @return Talk
     */
    Talk get(String name);

    /**
     * Create a new one (runtime exception if it exists already).
     * @param name The name
     * @throws IOException If fails
     */
    void create(String name) throws IOException;

    /**
     * Get only active talks.
     * @return Talks
     */
    Iterable<Talk> active();

    /**
     * Get recent talks.
     * @return Talks
     */
    Iterable<Talk> recent();

    /**
     * In directory.
     */
    @Immutable
    final class InDir implements Talks {
        /**
         * Dir.
         */
        private final transient String path;
        /**
         * Ctor.
         */
        public InDir() {
            this.path = Files.createTempDir().getAbsolutePath();
        }
        @Override
        public boolean exists(final long number) {
            return this.get(number) != null;
        }
        @Override
        public Talk get(final long number) {
            return Iterables.find(
                this.active(),
                new Predicate<Talk>() {
                    @Override
                    public boolean apply(final Talk talk) {
                        try {
                            return talk.read().xpath("/talk/@number").get(0)
                                .equals(Long.toString(number));
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
        }
        @Override
        public boolean exists(final String name) {
            return this.get(name) != null;
        }
        @Override
        public Talk get(final String name) {
            return Iterables.find(
                this.active(),
                new Predicate<Talk>() {
                    @Override
                    public boolean apply(final Talk talk) {
                        try {
                            return talk.read().xpath("/talk/@name").get(0)
                                .equals(name);
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
        }
        @Override
        public void create(final String name) throws IOException {
            FileUtils.write(
                new File(new File(this.path), name),
                new StrictXML(
                    new XMLDocument(
                        String.format(
                            "<talk name='%s' number='1' later='false'/>",
                            name
                        )
                    ),
                    Talk.SCHEMA
                ).toString(),
                CharEncoding.UTF_8
            );
        }
        @Override
        public Iterable<Talk> active() {
            return Iterables.transform(
                FileUtils.listFiles(new File(this.path), new String[0], false),
                new Function<File, Talk>() {
                    @Override
                    public Talk apply(final File file) {
                        return new Talk.InFile(file);
                    }
                }
            );
        }
        @Override
        public Iterable<Talk> recent() {
            return this.active();
        }
    }
}
