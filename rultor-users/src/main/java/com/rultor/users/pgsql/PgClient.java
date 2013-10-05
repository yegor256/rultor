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
package com.rultor.users.pgsql;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jolbox.bonecp.BoneCPDataSource;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PostgreSQL client.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface PgClient {

    /**
     * Get data source.
     * @return The data source
     */
    DataSource get();

    /**
     * Simple implementation.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "jdbc")
    @Loggable(Loggable.DEBUG)
    final class Simple implements PgClient {
        /**
         * JDBC URL.
         */
        private final transient String jdbc;
        /**
         * JDBC password.
         */
        private final transient String password;
        /**
         * JDBC user.
         */
        private final transient String user;

        /**
         * Public ctor.
         * @param url JDBC URL
         * @param pwd Password
         */
        public Simple(final String url, final String pwd) {
            this(url, pwd, null);
        }

        /**
         * Constructor.
         * @param url JDBC URL
         * @param pwd Password
         * @param usr Username
         */
        public Simple(final String url, final String pwd, final String usr) {
            this.jdbc = url;
            this.password = pwd;
            this.user = usr;
        }
        /**
         * {@inheritDoc}
         *
         * <p>Don't increase numbers here, since Heroku limits
         * the number of connections to PostgreSQL. Maximum we can have is
         * twenty, but keep in mind that this class is used by multiple
         * instances of the module, including web and conveyer. And each
         * of them may have multiple instances of themselves. Thus, it's
         * better to keep this number as little as possible. We don't have
         * long-running transactions - no need to have many connections.
         */
        @Override
        @Cacheable(forever = true)
        public DataSource get() {
            final BoneCPDataSource src = new BoneCPDataSource();
            src.setDriverClass("org.postgresql.Driver");
            src.setJdbcUrl(this.jdbc);
            if (this.user != null) {
                src.setUser(this.user);
            }
            src.setPassword(this.password);
            src.setPartitionCount(Tv.THREE);
            src.setMaxConnectionsPerPartition(1);
            src.setMinConnectionsPerPartition(1);
            src.setAcquireIncrement(1);
            src.setDisableConnectionTracking(true);
            return src;
        }
    }

}
