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
package com.rultor.widget;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Stand;
import com.rultor.spi.Tags;
import com.rultor.spi.Widget;
import com.rultor.tools.Exceptions;
import com.rultor.tools.NormJson;
import com.rultor.tools.Time;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directives;

/**
 * Build history.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
@Widget.Stylesheet("build-history.xsl")
public final class BuildHistory implements Widget {

    /**
     * JSON schema for "ci" tag.
     */
    private static final NormJson TAG_CI = new NormJson(
        BuildHealth.class.getResourceAsStream("tag-ci.json")
    );

    /**
     * JSON schema for "on-commit" tag.
     */
    private static final NormJson TAG_ONCOMMIT = new NormJson(
        BuildHealth.class.getResourceAsStream("tag-on-commit.json")
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public Directives render(final Stand stand) {
        Directives dirs = new Directives()
            .add("width").set("6").up()
            .add("builds");
        final Iterable<Pulse> pulses = Iterables.limit(
            stand.pulses().query()
                .withTag("on-commit")
                .withTag("ci").fetch(),
            Tv.HUNDRED
        );
        for (Pulse pulse : pulses) {
            try {
                dirs = dirs.append(this.render(pulse));
            } catch (NormJson.JsonException ex) {
                Exceptions.info(this, ex);
            }
        }
        return dirs;
    }

    /**
     * Convert pulse to directives.
     * @param pulse Pulse to convert
     * @return Directives
     * @throws NormJson.JsonException If can't parse
     * @checkstyle RedundantThrows (5 lines)
     */
    private Directives render(final Pulse pulse)
        throws NormJson.JsonException {
        final Tags tags = pulse.tags();
        final JsonObject commit = tags.get("on-commit")
            .data(BuildHistory.TAG_ONCOMMIT);
        final JsonObject scm = tags.get("ci").data(BuildHistory.TAG_CI);
        final Coordinates coords = pulse.coordinates();
        return new Directives().add("build")
            .add("coordinates")
            .add("rule").set(coords.rule()).up()
            .add("owner").set(coords.owner().toString()).up()
            .add("scheduled").set(coords.scheduled().toString()).up()
            .up()
            .add("commit")
            .add("name")
            .set(StringUtils.substring(scm.getString("name"), 0, Tv.SEVEN))
            .up()
            .add("time")
            .set(new Time(scm.getString("time")).toString()).up()
            .add("author").set(scm.getString("author")).up()
            .up()
            .add("duration").set(Long.toString(commit.getInt("duration"))).up()
            .add("code").set(Integer.toString(commit.getInt("code"))).up()
            .up();
    }

}
