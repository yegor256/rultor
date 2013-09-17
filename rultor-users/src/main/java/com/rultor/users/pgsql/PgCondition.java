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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Sheet;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Condition in PostgreSQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "parent", "clause" })
@Loggable(Loggable.DEBUG)
final class PgCondition implements Sheet.Condition {

    /**
     * Sheet.
     */
    private final transient PgSheet parent;

    /**
     * Query.
     */
    private final transient String clause;

    /**
     * Public ctor.
     * @param sheet Parent sheet
     */
    protected PgCondition(final PgSheet sheet) {
        this(sheet, "");
    }

    /**
     * Public ctor.
     * @param sheet Parent sheet
     * @param sql Inherited SQL query
     */
    private PgCondition(final PgSheet sheet, final String sql) {
        this.parent = sheet;
        this.clause = sql;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet sheet() {
        return this.parent.with(this.clause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet.Condition equalTo(final String column, final String value) {
        String tag;
        do {
            tag = RandomStringUtils.randomAlphabetic(Tv.TEN);
        } while (value.contains(tag));
        return new PgCondition(
            this.parent,
            String.format(
                "%s %s = $%s$%s$%3$s$",
                this.clause, column, tag, value
            )
        );
    }

}
