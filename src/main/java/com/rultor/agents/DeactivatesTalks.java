/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.text.Joined;

/**
 * Deactivates empty talks.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class DeactivatesTalks implements SuperAgent {

    /**
     * Which talks should be deactivated.
     */
    private static final String XPATH = new Joined(
        "",
        "/talk[@later='false' and not(request) and not(daemon)",
        " and not(shell)]"
    ).toString();

    @Override
    public void execute(final Talks talks) throws IOException {
        for (final Talk talk : talks.active()) {
            final XML xml = talk.read();
            if (!xml.nodes(DeactivatesTalks.XPATH).isEmpty()) {
                talk.active(false);
                Logger.info(this, "%s deactivated", talk.name());
            }
        }
    }

}
