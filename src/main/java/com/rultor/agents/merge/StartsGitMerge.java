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
package com.rultor.agents.merge;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Merges.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "profile")
public final class StartsGitMerge extends AbstractAgent {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    public StartsGitMerge(final Profile prof) {
        super(
            "/talk/merge-request-git[not(success)]",
            "/talk[not(daemon)]"
        );
        this.profile = prof;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("//merge-request-git").get(0);
        final ImmutableMap.Builder<String, String> vars =
            new ImmutableMap.Builder<String, String>();
        vars.put("BASE", req.xpath("base/text()").get(0));
        vars.put("BASE_BRANCH", req.xpath("base-branch/text()").get(0));
        vars.put("HEAD", req.xpath("head/text()").get(0));
        vars.put("HEAD_BRANCH", req.xpath("head-branch/text()").get(0));
        vars.put("SCRIPT", this.script());
        final String script = StringUtils.join(
            Iterables.concat(
                Iterables.transform(
                    vars.build().entrySet(),
                    new Function<Map.Entry<String, String>, String>() {
                        @Override
                        public String apply(
                            final Map.Entry<String, String> input) {
                            return String.format(
                                "%s=\"%s\"", input.getKey(), input.getValue()
                            );
                        }
                    }
                ),
                Collections.singleton(
                    IOUtils.toString(
                        this.getClass().getResourceAsStream("merge.sh"),
                        CharEncoding.UTF_8
                    )
                )
            ),
            "\n"
        );
        final String hash = req.xpath("@id").get(0);
        Logger.info(
            this, "git merge %s started in %s",
            hash, xml.xpath("/talk/@name").get(0)
        );
        return new Directives().xpath("/talk")
            .add("daemon")
            .attr("id", hash)
            .add("script").set(script);
    }

    /**
     * Make a script to run.
     * @return Script
     * @throws IOException If fails
     */
    private String script() throws IOException {
        final XML xml = this.profile.read();
        final String script;
        if (xml.nodes("/p/merge/script").isEmpty()) {
            script = "";
        } else if (xml.nodes("/p/merge/script/item").isEmpty()) {
            script = xml.xpath("/p/merge/script/text()").get(0);
        } else {
            script = StringUtils.join(
                xml.xpath("/p/merge/script/item/text()"), "; "
            );
        }
        return script;
    }

}
