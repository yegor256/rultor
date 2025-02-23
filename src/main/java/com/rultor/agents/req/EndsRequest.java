/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
