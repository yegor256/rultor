/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.RtGitHub;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * Tests for ${@link StartsTalks}.
 *
 * @since 1.9
 */
final class StartsTalksTest {

    /**
     * StartsTalks can start a talk.
     *
     * @throws Exception In case of error.
     */
    @Test
    @Disabled
    void startsTalks() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hey, do it");
        final SuperAgent agent = new StartsTalks(repo.github());
        final Talks talks = new Talks.InDir();
        agent.execute(talks);
        MatcherAssert.assertThat(
            "Active talk should not be created",
            talks.active(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    /**
     * StartsTalks can mark notifications as read.
     *
     * @param status Status of the response from GitHub
     * @throws Exception In case of error.
     */
    @ParameterizedTest
    @ValueSource(
        ints = {
            HttpURLConnection.HTTP_ACCEPTED,
            HttpURLConnection.HTTP_RESET
        }
    )
    void marksNotificationsAsRead(final int status) throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("[]"))
            .next(new MkAnswer.Simple(status))
            .start();
        try (container) {
            new StartsTalks(new RtGitHub(container.home()))
                .execute(new Talks.InDir());
            container.take();
            MatcherAssert.assertThat(
                "Notifications should not stay unread",
                container.take().method(),
                Matchers.equalTo(Request.PUT)
            );
        }
    }

    /**
     * StartsTalks can start a talk by a subscribed notification.
     *
     * @throws Exception In case of error.
     */
    @Test
    void startsTalkBySubscribedNotificationWithMention() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(StartsTalksTest.notification("subscribed")))
            .next(new MkAnswer.Simple("{\"body\":\"@rultor status\"}"))
            .next(new MkAnswer.Simple("{\"login\":\"rultor\"}"))
            .next(new MkAnswer.Simple("{\"html_url\":\"http://x/o/r/issues/7\"}"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_ACCEPTED))
            .start();
        try (container) {
            final Talks talks = Mockito.mock(Talks.class);
            final Talk talk = Mockito.mock(Talk.class);
            Mockito.when(talks.get("o/r#7")).thenReturn(talk);
            new StartsTalks(new RtGitHub(container.home())).execute(talks);
            Mockito.verify(talk).active(true);
        }
    }

    /**
     * StartsTalks can ignore a subscribed notification without a mention.
     *
     * @throws Exception In case of error.
     */
    @Test
    void ignoresSubscribedNotificationWithoutMention() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(StartsTalksTest.notification("subscribed")))
            .next(new MkAnswer.Simple("{\"body\":\"hello, world\"}"))
            .next(new MkAnswer.Simple("{\"login\":\"rultor\"}"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_ACCEPTED))
            .start();
        try (container) {
            final Talks talks = Mockito.mock(Talks.class);
            new StartsTalks(new RtGitHub(container.home())).execute(talks);
            Mockito.verifyNoInteractions(talks);
        }
    }

    private static String notification(final String reason) {
        return String.join(
            "",
            "[{\"reason\":\"", reason, "\",",
            "\"repository\":{\"full_name\":\"o/r\"},",
            "\"subject\":{",
            "\"url\":\"https://api.github.com/repos/o/r/issues/7\",",
            "\"latest_comment_url\":",
            "\"https://api.github.com/repos/o/r/issues/comments/9\"}}]"
        );
    }
}
