/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.twitter;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Quietly;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import twitter4j.TwitterException;

/**
 * Twitter via OAuth2.
 *
 * @since 1.30
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "key", "secret", "token", "tsecret" })
public final class OAuthTwitter implements Twitter {

    /**
     * Key.
     */
    private final transient String key;

    /**
     * Secret.
     */
    private final transient String secret;

    /**
     * Token.
     */
    private final transient String token;

    /**
     * Token secret.
     */
    private final transient String tsecret;

    /**
     * Ctor.
     * @param tkey Key
     * @param scrt Secret
     * @param tkn Token
     * @param tscrt Token secret
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public OAuthTwitter(final String tkey, final String scrt,
        final String tkn, final String tscrt) {
        this.key = tkey;
        this.secret = scrt;
        this.token = tkn;
        this.tsecret = tscrt;
    }

    /**
     * Send message to tweet.
     * @param msg Message to post
     * @throws IOException If fail
     */
    @Quietly
    public void post(final String msg) throws IOException {
        final twitter4j.Twitter twitter = twitter4j.Twitter.newBuilder()
            .oAuthAccessToken(this.token, this.tsecret)
            .oAuthConsumer(this.key, this.secret)
            .build();
        try {
            twitter.v1().tweets().updateStatus(msg);
        } catch (final TwitterException ex) {
            throw new IOException(ex);
        }
    }
}
