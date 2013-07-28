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
import com.rultor.shell.Shell;
import com.rultor.shell.Shells;
import com.rultor.spi.Signal;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;

/**
 * Bash batch.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "origin", "map" })
@Loggable(Loggable.DEBUG)
public final class Prerequisites implements Shells {

    /**
     * Shells.
     */
    private final transient Shells origin;

    /**
     * Prerequisites.
     */
    private final transient ArrayMap<String, Object> map;

    /**
     * Public ctor.
     * @param shls Shells
     * @param pres Prerequisites
     */
    public Prerequisites(
        @NotNull(message = "shells can't be NULL") final Shells shls,
        @NotNull(message = "prerequisites can't be NULL")
        final Map<String, Object> pres) {
        this.origin = shls;
        this.map = new ArrayMap<String, Object>(pres);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shell acquire() throws IOException {
        final Shell shell = this.origin.acquire();
        for (Map.Entry<String, Object> pair : this.map.entrySet()) {
            shell.exec(
                String.format("cat > %s", pair.getKey()),
                Prerequisites.toInputStream(pair.getValue()),
                Logger.stream(Level.INFO, this),
                Logger.stream(Level.WARNING, this)
            );
        }
        Signal.log(
            Signal.Mnemo.SUCCESS, "%d bash prerequisite(s) uploaded: %[list]s",
            this.map.size(), this.map.keySet()
        );
        return shell;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s with %d bash prerequisites",
            this.origin,
            this.map.size()
        );
    }

    /**
     * Convert it to input stream.
     * @param object Object
     * @return Input stream
     */
    private static InputStream toInputStream(final Object object) {
        final InputStream stream;
        if (object instanceof InputStream) {
            stream = InputStream.class.cast(object);
        } else {
            stream = IOUtils.toInputStream(object.toString());
        }
        return stream;
    }

}
