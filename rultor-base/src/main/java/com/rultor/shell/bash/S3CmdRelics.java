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
package com.rultor.shell.bash;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.log.Logger;
import com.rultor.shell.Sequel;
import com.rultor.shell.Shell;
import com.rultor.spi.Work;
import java.io.IOException;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Collection of S3Cmd relics.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(
    callSuper = false,
    of = { "work", "names", "bucket", "prefix", "key", "secret" }
)
@Loggable(Loggable.DEBUG)
public final class S3CmdRelics implements Sequel {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Name/path map.
     */
    private final transient ArrayMap<String, String> names;

    /**
     * S3 bucket.
     */
    private final transient String bucket;

    /**
     * S3 prefix in the bucket.
     */
    private final transient String prefix;

    /**
     * S3 key.
     */
    private final transient String key;

    /**
     * S3 secret.
     */
    private final transient String secret;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param map Map of names/paths
     * @param bkt Bucket name
     * @param pfx Prefix in S3 bucket
     * @param akey S3 authorization key
     * @param scrt S3 authorization secret
     * @checkstyle ParameterNumber (8 lines)
     */
    public S3CmdRelics(
        @NotNull(message = "map can't be NULL") final Work wrk,
        @NotNull(message = "map can't be NULL") final Map<String, String> map,
        @NotNull(message = "bucket can't be NULL") final String bkt,
        @NotNull(message = "prefix can't be NULL") final String pfx,
        @NotNull(message = "key can't be NULL") final String akey,
        @NotNull(message = "secret can't be NULL") final String scrt) {
        super();
        this.work = wrk;
        this.names = new ArrayMap<String, String>(map);
        this.bucket = bkt;
        Validate.isTrue(
            pfx.isEmpty() || pfx.endsWith("/"),
            "prefix must be empty or must end with forward slash"
        );
        this.prefix = pfx;
        this.key = akey;
        this.secret = scrt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%d relic(s) uploaded by s3cmd to `s3://%s/%s`",
            this.names.size(), this.bucket, this.prefix
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void exec(final Shell shell) throws IOException {
        for (Map.Entry<String, String> entry : this.names.entrySet()) {
            new S3CmdPut(
                entry.getKey(), entry.getValue(),
                this.bucket,
                String.format(
                    "%s%s/%s/%s/%s/", this.prefix,
                    this.work.owner(), this.work.rule(),
                    entry.getKey(), this.work.scheduled()
                ),
                this.key, this.secret
            ).exec(shell);
        }
    }

}
