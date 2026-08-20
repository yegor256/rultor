/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Connect to the log.
 * @since 1.1
 */
@Immutable
@FunctionalInterface
interface Connect {

    /**
     * Read it.
     * @return Stream
     * @throws IOException If fails
     */
    InputStream read() throws IOException;
}
