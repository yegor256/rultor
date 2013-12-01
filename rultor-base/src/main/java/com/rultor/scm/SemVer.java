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
package com.rultor.scm;

import com.github.zafarkhaja.semver.Version;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.snapshot.Step;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Branches sorted according to SemVer recommendations.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="http://semver.org/">Semantic Versioning</a>
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "scm", "regex" })
@Loggable(Loggable.DEBUG)
public final class SemVer implements SCM {

    /**
     * SCM.
     */
    private final transient SCM scm;

    /**
     * Regular expression to extract version part.
     */
    private final transient String regex;

    /**
     * Public ctor.
     * @param src SCM
     * @param reg Regular expression
     */
    public SemVer(
        @NotNull(message = "regex can't be NULL") final String reg,
        @NotNull(message = "SCM can't be NULL") final SCM src) {
        this.scm = src;
        this.regex = reg;
    }

    @Override
    @Loggable(value = Loggable.DEBUG, limit = Tv.FIVE)
    public Branch checkout(final String name) throws IOException {
        return this.scm.checkout(name);
    }

    @Override
    @Step("${result.size()} branch(es) match `${this.regex}`")
    @Loggable(value = Loggable.DEBUG, limit = Tv.FIVE)
    public List<String> branches() throws IOException {
        final List<String> ordered = new LinkedList<String>();
        final Pattern pattern = Pattern.compile(this.regex);
        // @checkstyle LineLength (1 line)
        final Pattern version = Pattern.compile("^((\\d+)\\.(\\d+)\\.(\\d+))(?:-([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?(?:\\+([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?$");
        ordered.addAll(
            Lists.newLinkedList(
                Iterables.filter(
                    this.scm.branches(),
                    new Predicate<String>() {
                        @Override
                        public boolean apply(final String name) {
                            final Matcher matcher = pattern.matcher(name);
                            return matcher.matches()
                                && version.matcher(matcher.group(1)).matches();
                        }
                    }
                )
            )
        );
        Collections.sort(
            ordered,
            new Comparator<String>() {
                @Override
                public int compare(final String left, final String right) {
                    final Matcher lft = pattern.matcher(left);
                    lft.matches();
                    final Matcher rht = pattern.matcher(right);
                    rht.matches();
                    return Version.valueOf(lft.group(1))
                        .compareTo(Version.valueOf(rht.group(1)));
                }
            }
        );
        return Collections.unmodifiableList(ordered);
    }

    @Override
    public URI uri() {
        return this.scm.uri();
    }

}
