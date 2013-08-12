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
package com.rultor.conveyer;

import java.util.NoSuchElementException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Thread-safe circular buffer of bytes.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = { "data", "head", "tail" })
final class CircularBuffer {

    /**
     * Bytes.
     */
    private final transient byte[] data;

    /**
     * Next byte to write to.
     */
    private transient int head;

    /**
     * Next byte to read from.
     */
    private transient int tail;

    /**
     * Ctor.
     * @param size Buffer size in bytes
     */
    protected CircularBuffer(final int size) {
        this.data = new byte[size];
    }

    /**
     * Add byte.
     * @param item Byte to write
     */
    public void write(final byte item) {
        synchronized (this.data) {
            this.data[this.head] = item;
            ++this.head;
            if (this.head == this.data.length) {
                this.head = 0;
            }
            if (this.head == this.tail) {
                ++this.tail;
            }
        }
    }

    /**
     * Does it have any bytes to read?
     * @return TRUE if it's empty
     */
    public boolean isEmpty() {
        synchronized (this.data) {
            return this.head == this.tail;
        }
    }

    /**
     * Read the next available byte.
     * @return The byte
     */
    public byte read() {
        synchronized (this.data) {
            if (this.isEmpty()) {
                throw new NoSuchElementException(
                    "circular buffer is empty"
                );
            }
            final byte item = this.data[this.tail];
            ++this.tail;
            if (this.tail == this.data.length) {
                this.tail = 0;
            }
            return item;
        }
    }

}
