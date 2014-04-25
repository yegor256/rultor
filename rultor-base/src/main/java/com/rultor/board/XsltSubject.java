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
package com.rultor.board;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.snapshot.XSLT;
import java.io.StringReader;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * XSL transformation subject.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "xslt")
@Loggable(Loggable.DEBUG)
public final class XsltSubject implements Bill {

    /**
     * Original bill.
     */
    private final transient Bill origin;

    /**
     * XSL transformation.
     */
    private final transient String xslt;

    /**
     * Public constructor.
     * @param xsltran XSL transformation.
     * @param bill Original bill
     */
    public XsltSubject(
        @NotNull(message = "transformation can't be NULL") final String xsltran,
        @NotNull(message = "bill can't be NULL") final Bill bill
    ) {
        this.origin = bill;
        this.xslt = xsltran;
    }

    @Override
    public String body() {
        return this.origin.body();
    }

    @Override
    public String subject() {
        try {
            return new XSLT(
                new StreamSource(new StringReader(this.origin.subject())),
                new StreamSource(new StringReader(this.xslt))
            ).xml();
        } catch (final TransformerException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String sender() {
        return this.origin.sender();
    }

    @Override
    public Collection<String> recipients() {
        return this.origin.recipients();
    }

}
