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
package com.rultor.env;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.rultor.snapshot.Radar;
import com.rultor.snapshot.XemblyException;
import com.rultor.spi.Tag;
import com.rultor.tools.Exceptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.SyntaxException;

/**
 * Environments automatically badged by tags in log.
 *
 * <p>This wrapper will sniff Xembly tags announced in log before
 * the creating of a new environment and convert them to environment
 * badges. The logic of this mechanism is to be specified in the mapping,
 * for example:
 *
 * <pre>com.rultor.env.Badged(
 *   {
 *     "github:issue": "urn:rultor:github-issue",
 *     "jira:key": "urn:rultor:jira-key"
 *   },
 *   original-environments
 * )
 * </pre>
 *
 * <p>In this example, if tag {@code "github"} is found, its attribute
 * {@code "issue"} will be transformed into {@code "urn:rultor:github-issue"}
 * badge of a new environment.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class Badged implements Environments {

    /**
     * Origin environments.
     */
    private final transient Environments origin;

    /**
     * Mapping.
     */
    private final transient ArrayMap<String, String> mapping;

    /**
     * Public ctor.
     * @param map Mapping
     * @param envs Original envs
     */
    public Badged(
        @NotNull(message = "map can't be NULL") final Map<String, String> map,
        @NotNull(message = "envs can't be NULL") final Environments envs) {
        this.origin = envs;
        this.mapping = new ArrayMap<String, String>(map);
    }

    @Override
    public Environment acquire() throws IOException {
        final Environment env = this.origin.acquire();
        Collection<Tag> tags;
        try {
            tags = new Radar().snapshot().tags();
        } catch (final SyntaxException ex) {
            tags = new ArrayList<Tag>(0);
            Exceptions.warn(this, ex);
        } catch (final XemblyException ex) {
            tags = new ArrayList<Tag>(0);
            Exceptions.warn(this, ex);
        }
        for (final Tag tag : tags) {
            this.attach(env, tag);
        }
        return env;
    }

    @Override
    public Iterator<Environment> iterator() {
        return this.origin.iterator();
    }

    /**
     * Process one tag.
     * @param env Environment to attach to
     * @param tag The tag to process
     */
    private void attach(final Environment env, final Tag tag) {
        for (final Map.Entry<String, String> attr
            : tag.attributes().entrySet()) {
            final String label = String.format(
                "%s:%s", tag.label(), attr.getKey()
            );
            if (this.mapping.containsKey(label)) {
                env.badge(this.mapping.get(label), attr.getValue());
            }
        }
    }

}
