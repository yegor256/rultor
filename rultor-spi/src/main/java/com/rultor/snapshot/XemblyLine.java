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
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.XemblySyntaxException;

/**
 * Log line in Xembly.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "directives")
public final class XemblyLine {

    /**
     * Mark (reads as "χemβly").
     */
    public static final String MARK = "\u03c7em\u03b2ly";

    /**
     * Pattern to use for matching.
     */
    private static final Pattern PTN = Pattern.compile(
        String.format(".*%s '([^']+)'", Pattern.quote(XemblyLine.MARK))
    );

    /**
     * Encapsulated xembly directives.
     */
    private final transient Array<Directive> directives;

    /**
     * Public ctor.
     * @param dirs Xembly directives to encapsulate
     */
    public XemblyLine(final Collection<Directive> dirs) {
        this.directives = new Array<Directive>(dirs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s '%s'", XemblyLine.MARK, this.xembly());
    }

    /**
     * Get Xembly script.
     * @return Script
     */
    public String xembly() {
        return new StringBuilder(StringUtils.join(this.directives, "; "))
            .append(';').toString();
    }

    /**
     * Does it look like xembly line.
     * @param line Line to check
     * @return TRUE if yes (no strong guarantee though)
     */
    public static boolean existsIn(final String line) {
        return line.contains(XemblyLine.MARK) && line.endsWith("'");
    }

    /**
     * Decode text.
     * @param text Text to decode
     * @return Detail found or runtime exception
     * @throws XemblySyntaxException If can't parse
     * @checkstyle RedundantThrows (4 lines)
     */
    public static XemblyLine parse(final String text)
        throws XemblySyntaxException {
        final Matcher matcher = XemblyLine.PTN.matcher(text);
        final XemblyLine line;
        if (matcher.matches()) {
            line = new XemblyLine(new Directives(matcher.group(1)));
        } else {
            line = new XemblyLine(new ArrayList<Directive>(0));
        }
        return line;
    }

    /**
     * Convenient utility method to log xembly.
     */
    public void log() {
        Logger.info(this, this.toString());
    }

}
