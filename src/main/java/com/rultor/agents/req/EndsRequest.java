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
package com.rultor.agents.req;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.text.ParseException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Finishes and reports merge results.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class EndsRequest extends AbstractAgent {

    /**
     * Ctor.
     */
    public EndsRequest() {
        super(
            "/talk/request[type and not(success)]",
            "/talk/daemon[started and ended and code]"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) {
        final XML daemon = xml.nodes("/talk/daemon").get(0);
        final int code = Integer.parseInt(daemon.xpath("code/text()").get(0));
        final long msec = EndsRequest.iso(daemon.xpath("ended/text()").get(0))
            - EndsRequest.iso(daemon.xpath("started/text()").get(0));
        final boolean success = code == 0;
        Logger.info(this, "request finished: %b", success);
        return new Directives().xpath("/talk/request")
            .add("msec").set(Long.toString(msec)).up()
            .add("success").set(Boolean.toString(success));
    }

    /**
     * Parse ISO date into msec.
     * @param iso ISO date
     * @return Msec
     */
    private static long iso(final String iso) {
        try {
            return DateFormatUtils.ISO_DATETIME_FORMAT.parse(iso).getTime();
        } catch (final ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
