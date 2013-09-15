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
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;
import org.xembly.Directives;

/**
 * Merge history.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
@Widget.Stylesheet("merge-history.xsl")
public final class MergeHistory implements Widget {

    /**
     * JSON schema for "merge" tag.
     */
    private static final NormJson TAG_MERGE = new NormJson(
        BuildHealth.class.getResourceAsStream("tag-merge.json")
    );

    /**
     * JSON schema for "on-pull-request" tag.
     */
    private static final NormJson TAG_ONREQUEST = new NormJson(
        BuildHealth.class.getResourceAsStream("tag-on-pull-request.json")
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public Directives render(final Stand stand) {
        Directives dirs = new Directives()
            .add("width").set("6").up()
            .add("merges");
        final Iterable<Pulse> pulses = Iterables.limit(
            stand.pulses().query()
                .withTag("on-pull-request")
                .withTag("merge").fetch(),
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
        final JsonObject request = tags.get("on-pull-request")
            .data(MergeHistory.TAG_ONREQUEST);
        final JsonObject merge = tags.get("merge").data(MergeHistory.TAG_MERGE);
        final Coordinates coords = pulse.coordinates();
        Directives dirs = new Directives().add("merge")
            .add("coordinates")
            .add("rule").set(coords.rule()).up()
            .add("owner").set(coords.owner().toString()).up()
            .add("scheduled").set(coords.scheduled().toString()).up()
            .up()
            .add("request")
            .add("name").set(merge.getString("request")).up()
            .add("failure").set(merge.getString("failure")).up()
            .add("params");
        final JsonObject params = merge.getJsonObject("params");
        for (String key : params.keySet()) {
            dirs = dirs.add("param")
                .add("name").set(key).up()
                .add("value").set(params.getString(key)).up().up();
        }
        return dirs.up().up().add("duration")
            .set(Long.toString(request.getInt("duration")))
            .up()
            .add("code")
            .set(Integer.toString(request.getInt("code")))
            .up()
            .up();
    }

}
