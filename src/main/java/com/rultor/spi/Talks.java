/**
 * Copyright (c) 2009-2018, rultor.com
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

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XMLDocument;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sorted;
import org.cactoos.list.SolidList;
import org.cactoos.text.JoinedText;

/**
 * Talks in a repo.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
     * Delete an existing talk (runtime exception if it's absent).
     * @param name The name
     */
    void delete(String name);

    /**
     * Create a new one (runtime exception if it exists already).
     * @param repo Name of the repository it is in
     * @param name The name
     * @throws IOException If fails
     */
    void create(String repo, String name) throws IOException;

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
     * Get siblings, since this date (all talks will be older that this date).
     * @param repo Repo name
     * @param since Date
     * @return Talks
     */
    Iterable<Talk> siblings(String repo, Date since);

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
         * @throws IOException ex
         */
        public InDir() throws IOException {
            this.path = Files.createTempDirectory("")
                .toAbsolutePath()
                .toString();
        }
        @Override
        public boolean exists(final long number) {
            return this.get(number) != null;
        }
        @Override
        public Talk get(final long number) {
            return new Filtered<>(
                talk -> {
                    try {
                        return talk.read().xpath("/talk/@number").get(0)
                            .equals(Long.toString(number));
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                },
                this.active()
            ).iterator().next();
        }
        @Override
        public boolean exists(final String name) {
            return this.get(name) != null;
        }
        @Override
        public Talk get(final String name) {
            return new Filtered<>(
                talk -> {
                    try {
                        return talk.read().xpath("/talk/@name").get(0)
                            .equals(name);
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                },
                this.active()
            ).iterator().next();
        }
        @Override
        public void delete(final String name) {
            FileUtils.deleteQuietly(new File(new File(this.path), name));
        }
        @Override
        public void create(final String repo, final String name)
            throws IOException {
            final File file = new File(new File(this.path), name);
            FileUtils.write(
                file,
                new StrictXML(
                    new XMLDocument(
                        new JoinedText(
                            " ",
                            String.format(
                                "<talk name='%s' number='1' later='false'>",
                                name
                            ),
                            "<wire>",
                            String.format(
                                "<href>https://github.com/%s</href>",
                                name
                            ),
                            "</wire></talk>"
                        ).asString()
                    ),
                    Talk.SCHEMA
                ).toString(),
                CharEncoding.UTF_8
            );
            Logger.info(this, "talk '%s' created in %s", name, file);
        }
        @Override
        public Iterable<Talk> active() {
            final Collection<File> files = FileUtils.listFiles(
                new File(this.path), null, false
            );
            final SolidList<File> list = new SolidList<>(
                new Sorted<>(
                    files
                )
            );
            Logger.info(this, "%d files in %s", files.size(), this.path);
            return new Mapped<>(
                file -> new Talk.InFile(file),
                list
            );
        }
        @Override
        public Iterable<Talk> recent() {
            return this.active();
        }
        @Override
        public Iterable<Talk> siblings(final String repo, final Date since) {
            return this.active();
        }
    }
}
