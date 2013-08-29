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
import com.jcabi.log.Logger;
import com.rultor.shell.Sequel;
import com.rultor.shell.Shell;
import com.rultor.shell.Terminal;
import com.rultor.snapshot.XemblyLine;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FilenameUtils;
import org.xembly.Directives;

/**
 * Put file(s) using s3cmd command line tool.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="http://s3tools.org/s3cmd">s3cmd</a>
 */
@Immutable
@EqualsAndHashCode(of = { "name", "path", "bucket", "prefix", "key", "secret" })
@Loggable(Loggable.DEBUG)
public final class S3CmdPut implements Sequel {

    /**
     * Product name.
     */
    private final transient String name;

    /**
     * Path to files, like "./alpha/files/*.txt".
     */
    private final transient String path;

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
     * @param label Name of the product to discover
     * @param pth Path to use
     * @param bkt Bucket name
     * @param pfx Prefix in S3 bucket
     * @param akey S3 authorization key
     * @param scrt S3 authorization secret
     * @checkstyle ParameterNumber (8 lines)
     */
    public S3CmdPut(
        @NotNull(message = "name can't be NULL") final String label,
        @NotNull(message = "path can't be NULL") final String pth,
        @NotNull(message = "bucket can't be NULL") final String bkt,
        @NotNull(message = "prefix can't be NULL") final String pfx,
        @NotNull(message = "key can't be NULL") final String akey,
        @NotNull(message = "secret can't be NULL") final String scrt) {
        this.name = label;
        this.bucket = bkt;
        this.path = pth;
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
            "%s uploaded to `s3://%s/%s` by s3cmd from `%s`",
            this.name, this.bucket, this.prefix, this.path
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(final Shell shell) throws IOException {
        final String dir = FilenameUtils.getFullPathNoEndSeparator(this.path);
        final String mask = FilenameUtils.getName(this.path);
        final String url = String.format(
            "http://%s.s3.amazonaws.com/%s", this.bucket, this.prefix
        );
        final int files = new Terminal(shell).exec(
            new StringBuilder()
                .append("CONFIG=`mktemp /tmp/s3cmdput-XXXX`")
                .append(" && cat > $CONFIG")
                .append(" && HEAD=")
                .append(
                    Terminal.escape(
                        String.format("s3://%s/%s", this.bucket, this.prefix)
                    )
                )
                .append(" && cd ")
                .append(Terminal.escape(dir))
                .append(" && FILES=`find ")
                .append(mask)
                // @checkstyle LineLength (1 line)
                .append(" -type f` && for f in $FILES; do s3cmd --config=$CONFIG put $f \"$HEAD$f\" > /dev/null; echo $f; done")
                .toString(),
            new StringBuilder()
                .append("[default]\n")
                .append("access_key=").append(this.key).append('\n')
                .append("secret_key=").append(this.secret).append('\n')
                .toString()
        ).split("\n").length;
        final String markdown;
        if (mask.contains("*")) {
            markdown = String.format("[%d files](%sindex.html)", files, url);
        } else {
            markdown = String.format("[%s](%s%1$s)", mask, url);
        }
        new XemblyLine(
            new Directives()
                .xpath("/snapshot")
                .addIfAbsent("products")
                .strict(1)
                .add("product")
                .add("name")
                .set(this.name)
                .up()
                .add("markdown")
                .set(markdown)
        ).log();
    }

}
