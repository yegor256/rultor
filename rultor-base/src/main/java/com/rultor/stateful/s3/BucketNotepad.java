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
package com.rultor.stateful.s3;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.aws.S3Client;
import com.rultor.spi.Work;
import com.rultor.stateful.Notepad;
import java.util.Collection;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Notepads in bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "work", "client", "prefix" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class BucketNotepad implements Notepad {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Object name.
     */
    private final transient String prefix;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param pfx Prefix
     * @param clnt Client
     */
    public BucketNotepad(@NotNull(message = "work can't be NULL")
        final Work wrk,
        @NotNull(message = "S3 prefix can't be NULL") final String pfx,
        @NotNull(message = "S3 client can't be NULL") final S3Client clnt) {
        this.work = wrk;
        this.prefix = pfx;
        this.client = clnt;
    }

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param clnt Client
     */
    public BucketNotepad(final Work wrk, final S3Client clnt) {
        this(wrk, "", clnt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "S3 bucket notepad at `%s` with `%s` prefix accessed with %s",
            this.client.bucket(), this.prefix, this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.notepad().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.notepad().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object object) {
        return this.notepad().contains(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        return this.notepad().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return this.notepad().toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(final T[] array) {
        return this.notepad().toArray(array);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final String line) {
        return this.notepad().add(line);
    }

    @Override
    public boolean remove(final Object line) {
        return this.notepad().remove(line);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> list) {
        return this.notepad().containsAll(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends String> list) {
        return this.notepad().addAll(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> list) {
        return this.notepad().removeAll(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> list) {
        return this.notepad().retainAll(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.notepad().clear();
    }

    /**
     * Get the notepad.
     * @return Notepad
     */
    private Notepad notepad() {
        return new ObjectNotepad(
            String.format(
                "%s%s/%s.notepad",
                this.prefix,
                this.work.owner(),
                this.work.rule()
            ),
            this.client
        );
    }

}
