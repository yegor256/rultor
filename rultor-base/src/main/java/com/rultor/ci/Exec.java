/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.ci;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.rultor.shell.Batch;
import com.rultor.spi.Instance;
import java.io.IOException;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Exec executes underlying batch with given args.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "batch", "map" })
@Loggable(Loggable.DEBUG)
public final class Exec implements Instance {

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Arguments to use in Velocity script.
     */
    private final transient ArrayMap<String, String> map;

    /**
     * Public ctor.
     * @param btch Batch to use
     * @param args Arguments to use in Velocity script
     */
    public Exec(@NotNull(message = "batch can't be NULL")
        final Batch btch,
        @NotNull(message = "Map can't be NULL")
        final Map<String, String> args) {
        this.batch = btch;
        this.map = new ArrayMap<String, String>(args);
    }

    @Override
    @Loggable(
        value = Loggable.DEBUG, limit = Integer.MAX_VALUE,
        ignore = IOException.class
    )
    public void pulse() throws Exception {
        this.batch.exec(this.map, new NullOutputStream());
    }

}
