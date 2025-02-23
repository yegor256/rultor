/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import co.stateful.Sttc;
import co.stateful.mock.MkSttc;
import com.jcabi.github.Github;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import com.yegor256.WeAreOnline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for ${@link Agents}.
 *
 * @since 1.7
 */
final class AgentsTest {

    /**
     * Agents can process a talk.
     * @throws Exception In case of error.
     */
    @Test
    @ExtendWith(WeAreOnline.class)
    void processesTalk() throws Exception {
        final Talk talk = new Talk.InFile();
        final Github github = new MkGithub();
        final Sttc sttc = new MkSttc();
        final Profile profile = new Profile.Fixed();
        Assertions.assertDoesNotThrow(
            () -> new Agents(github, sttc)
                .agent(talk, profile).execute(talk)
        );
    }
}
