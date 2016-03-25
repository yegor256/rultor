/**
 * Copyright (c) 2009-2016, rultor.com
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
package com.rultor.agents.twitter;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Quietly;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Twitter via OAuth2.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
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

    @Override
    @Quietly
    public void post(final String msg) throws IOException {
        final TwitterFactory factory = new TwitterFactory();
        final twitter4j.Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(this.key, this.secret);
        twitter.setOAuthAccessToken(new AccessToken(this.token, this.tsecret));
        try {
            twitter.updateStatus(msg);
        } catch (final TwitterException ex) {
            throw new IOException(ex);
        }
    }

}
