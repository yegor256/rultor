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
package com.rultor.conveyer;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.rexsl.test.RestTester;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Version of running conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
final class Version {

    /**
     * Get revision from the web server.
     * @return Revision found there
     */
    public boolean same() {
        boolean same;
        final String mine = Manifests.read("Rultor-Revision");
        try {
            final String base = RestTester
                .start(URI.create("http://www.rultor.com/misc/version"))
                .get("read revision from web node")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .getBody();
            if (mine.equals(base) || !base.matches("[a-f0-9]+")) {
                same = true;
            } else {
                same = false;
                Logger.info(
                    Main.class,
                    "#same(): we're in %s while %s is the newest one",
                    mine,
                    base
                );
            }
        } catch (AssertionError ex) {
            Logger.warn(Main.class, "#same(): %s", ex);
            same = true;
        }
        return same;
    }

}
