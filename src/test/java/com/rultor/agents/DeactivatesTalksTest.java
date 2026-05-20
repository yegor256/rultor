/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link DeactivatesTalks}.
 * @since 1.3
 */
final class DeactivatesTalksTest {

    /**
     * DeactivatesTalks can deactivate a talk.
     * @throws Exception In case of error.
     */
    @Test
    void deactivatesTalk() throws Exception {
        final Talks talks = Mockito.mock(Talks.class);
        Mockito.doReturn(
            Collections.singleton(
                new Talk.InFile(
                    new XMLDocument(
                        "<talk later='false' name='a' number='1'/>"
                    )
                )
            )
        ).when(talks).active();
        Assertions.assertDoesNotThrow(
            () -> new DeactivatesTalks().execute(talks)
        );
    }
}
