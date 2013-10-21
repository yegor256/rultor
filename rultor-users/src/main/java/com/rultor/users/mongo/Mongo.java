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
package com.rultor.users.mongo;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Mongo client container.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Mongo {

    /**
     * Get DB.
     * @return The db
     * @throws IOException If IO fails
     */
    DB get() throws IOException;

    /**
     * Simple implementation.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "host", "port", "name", "user", "password" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements Mongo {
        /**
         * Host name.
         */
        private final transient String host;
        /**
         * Port number.
         */
        private final transient int port;
        /**
         * Database name.
         */
        private final transient String name;
        /**
         * User name.
         */
        private final transient String user;
        /**
         * Password.
         */
        private final transient String password;
        /**
         * Public ctor.
         * @param hst Host name
         * @param prt Port number
         * @param database DB name
         * @param login User name
         * @param pwd Password
         * @checkstyle ParameterNumber (4 lines)
         */
        public Simple(final String hst, final int prt, final String database,
            final String login, final String pwd) {
            this.host = hst;
            this.port = prt;
            this.name = database;
            this.user = login;
            this.password = pwd;
        }
        @Override
        @Cacheable(forever = true)
        @RetryOnFailure(verbose = false)
        public DB get() throws IOException {
            final MongoClient client = new MongoClient(this.host, this.port);
            final DB database = client.getDB(this.name);
            if (!StringUtils.isEmpty(this.user)) {
                Validate.isTrue(
                    database.authenticate(
                        this.user,
                        this.password.toCharArray()
                    ),
                    "failed to authenticate with MongoDB at '%s:%d/%s' as '%s'",
                    this.host, this.port, this.name, this.user
                );
            }
            return database;
        }
    }

}
