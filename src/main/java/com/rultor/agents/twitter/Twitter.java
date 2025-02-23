/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.twitter;

import com.jcabi.aspects.Immutable;
import java.io.IOException;

/**
 * Twitter abstraction.
 *
 * @since 1.30
 */
@Immutable
public interface Twitter {

    /**
     * Post a message.
     * @param msg Message
     * @throws IOException If it fails
     */
    void post(String msg) throws IOException;

}
