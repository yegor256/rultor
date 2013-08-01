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
package com.rultor.snapshot;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.Xembler;
import org.xembly.XemblyBuilder;

/**
 * Detail in Xembly.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "script")
public final class XemblyDetail implements Detail {

    /**
     * Pattern to use for matching.
     */
    private static final Pattern PTN = Pattern.compile(
        ".*χembly '([^']+)'.*"
    );

    /**
     * Encapsulated xembly program.
     */
    private final transient String script;

    /**
     * Public ctor.
     * @param scrpt Xembly script to encapsulate
     */
    public XemblyDetail(final String scrpt) {
        this.script = scrpt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("χembly '%s'", this.script);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refine(final Document story) {
        new Xembler(new Directives(this.script)).exec(story);
    }

    /**
     * Does it look like spec detail.
     * @param line Line to check
     * @return TRUE if yes
     */
    public static boolean contains(final String line) {
        return XemblyDetail.PTN.matcher(line).matches();
    }

    /**
     * Decode text.
     * @param text Text to decode
     * @return Detail found or runtime exception
     */
    public static Detail parse(final String text) {
        final Matcher matcher = XemblyDetail.PTN.matcher(text);
        Validate.isTrue(matcher.matches(), "invalid line '%s'", text);
        return new XemblyDetail(matcher.group(1));
    }

    /**
     * Convenient utility method to log xembly.
     * @param builder Builder of xembly code
     */
    public static void log(final XemblyBuilder builder) {
        Logger.info(
            XemblyDetail.class,
            new XemblyDetail(builder.toString()).toString()
        );
    }

}
