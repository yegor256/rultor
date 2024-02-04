/**
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Finishes and reports merge results.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
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
        final long msec = new Time(daemon.xpath("ended/text()").get(0)).msec()
            - new Time(daemon.xpath("started/text()").get(0)).msec();
        final boolean success = code == 0;
        Logger.info(this, "request finished: %b", success);
        final Directives dirs = new Directives().xpath("/talk/request")
            .add("msec").set(Long.toString(msec)).up()
            .add("success").set(Boolean.toString(success)).up();
        final List<String> highlights = daemon.xpath("highlights/text()");
        if (!highlights.isEmpty()) {
            dirs.add("highlights").set(highlights.get(0)).up();
        }
        final List<String> tail = daemon.xpath("tail/text()");
        if (!tail.isEmpty()) {
            dirs.add("tail").set(tail.get(0)).up();
        }
        return dirs;
    }

}
