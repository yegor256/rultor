/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link DeactivatesTalks}.
 *
 * @since 1.3
 */
final class DeactivatesTalksTest {

    /**
     * DeactivatesTalks can deactivate a talk.
     * @throws Exception In case of error.
     */
    @Test
    void deactivatesTalk() throws Exception {
        final SuperAgent agent = new DeactivatesTalks();
        final Talk talk = new Talk.InFile(
            new XMLDocument(
                "<talk later='false' name='a' number='1'/>"
            )
        );
        final Talks talks = Mockito.mock(Talks.class);
        Mockito.doReturn(Collections.singleton(talk)).when(talks).active();
        Assertions.assertDoesNotThrow(
            () -> agent.execute(talks)
        );
    }

}
