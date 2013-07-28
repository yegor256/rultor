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
package com.rultor.shell;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import com.rultor.timeline.Timeline;
import com.rultor.tools.Vext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;

/**
 * Batch that resonates to a {@link Timeline}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "origin", "timeline", "success", "failure" })
@Loggable(Loggable.DEBUG)
public final class Resonant implements Batch {

    /**
     * Pattern we're expecting in output stream.
     */
    private static final Pattern LINE = Pattern.compile(
        ".*RULTOR-PRODUCT(?:\\s([A-Za-z0-9=/\\+]+)){2}"
    );

    /**
     * Original batch.
     */
    private final transient Batch origin;

    /**
     * Timeline to resonate to.
     */
    private final transient Timeline timeline;

    /**
     * Text on success.
     */
    private final transient Vext success;

    /**
     * Text on failure.
     */
    private final transient Vext failure;

    /**
     * Public ctor.
     * @param batch Original batch
     * @param tmln Timeline to resonate to
     * @param good Message on success
     * @param bad Message on failure
     * @checkstyle ParameterNumber (8 lines)
     */
    public Resonant(
        @NotNull(message = "batch can't be NULL") final Batch batch,
        @NotNull(message = "script can't be NULL") final Timeline tmln,
        @NotNull(message = "good can't be NULL") final String good,
        @NotNull(message = "bad can't be NULL") final String bad) {
        this.origin = batch;
        this.timeline = tmln;
        this.success = new Vext(good);
        this.failure = new Vext(bad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(final Map<String, Object> args, final OutputStream output)
        throws IOException {
        final long start = System.currentTimeMillis();
        final Collection<Product> products =
            new CopyOnWriteArrayList<Product>();
        final StringBuffer line = new StringBuffer();
        final int code = this.origin.exec(
            args,
            new OutputStream() {
                @Override
                public void write(final int chr) throws IOException {
                    output.write(chr);
                    line.append((char) chr);
                    if (chr == '\n') {
                        final Matcher matcher =
                            Resonant.LINE.matcher(line.toString().trim());
                        if (matcher.matches()) {
                            products.add(
                                new Product.Simple(
                                    new String(
                                        Base64.decodeBase64(matcher.group(1)),
                                        CharEncoding.UTF_8
                                    ),
                                    new String(
                                        Base64.decodeBase64(matcher.group(2)),
                                        CharEncoding.UTF_8
                                    )
                                )
                            );
                        }
                        line.setLength(0);
                    }
                }
            }
        );
        products.add(
            new Product.Simple(
                "elapsed time",
                Logger.format("%[ms]s", System.currentTimeMillis() - start)
            )
        );
        if (code == 0) {
            this.timeline.submit(
                this.success.print(args),
                Arrays.<Tag>asList(new Tag.Simple("success", Level.FINE)),
                products
            );
        } else {
            this.timeline.submit(
                this.failure.print(args),
                Arrays.<Tag>asList(new Tag.Simple("failure", Level.SEVERE)),
                products
            );
        }
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s resonated to %s",
            this.origin,
            this.timeline
        );
    }

    /**
     * Encode product into log line.
     * @param product Product to encode
     * @return Line to log
     * @throws UnsupportedEncodingException If can't encode
     */
    public static String encode(final Product product)
        throws UnsupportedEncodingException {
        return String.format(
            "RULTOR-PRODUCT %s %s",
            Base64.encodeBase64String(
                product.name().getBytes(CharEncoding.UTF_8)
            ),
            Base64.encodeBase64String(
                product.markdown().getBytes(CharEncoding.UTF_8)
            )
        );
    }

}
