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
package com.rultor.tools;

import com.jcabi.log.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Exception logging utility class.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class Exceptions {

    /**
     * This is utility class.
     */
    private Exceptions() {
        // intentionally empty
    }

    /**
     * Log exception in a compact syslog-friendly format with INFO log level.
     * @param source Source of exception.
     * @param exception Throwable to log.
     */
    public static void info(final Object source, final Throwable exception) {
        Logger.info(source, ExceptionUtils.getRootCauseMessage(exception));
    }

    /**
     * Log exception in a compact syslog-friendly format with WARN log level.
     * @param source Source of exception.
     * @param exception Throwable to log.
     */
    public static void warn(final Object source, final Throwable exception) {
        Logger.warn(source, ExceptionUtils.getRootCauseMessage(exception));
    }

    /**
     * Log exception in a compact syslog-friendly format with ERROR log level.
     * @param source Source of exception.
     * @param exception Throwable to log.
     */
    public static void error(final Object source, final Throwable exception) {
        Logger.error(source, ExceptionUtils.getRootCauseMessage(exception));
    }

    /**
     * Get exception message by composing messages of chained exceptions.
     * @param exception Throwable from which to retrieve messages.
     * @return Composed message.
     */
    public static String message(final Throwable exception) {
        Throwable cause = exception;
        final List<String> messages = new ArrayList<String>(0);
        while (cause != null) {
            messages.add(ExceptionUtils.getMessage(cause));
            cause = cause.getCause();
        }
        return Exceptions.escape(
            StringUtils.join(messages, SystemUtils.LINE_SEPARATOR)
        );
    }

    /**
     * Get clean version of exception stacktrace.
     * @param exception Throwable for stacktrace cleanup.
     * @return Clean one
     */
    public static String stacktrace(final Throwable exception) {
        final Collection<String> lines = new LinkedList<String>();
        final String[] markers = new String[] {
            "org.aspectj.runtime",
            "com.jcabi.aspects.aj",
            "_aroundBody",
            "AjcClosure",
        };
        for (String line : ExceptionUtils.getStackTrace(exception)
            .split(SystemUtils.LINE_SEPARATOR)) {
            boolean found = false;
            for (String marker : markers) {
                if (line.contains(marker)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                lines.add(line);
            }
        }
        return Exceptions.escape(
            StringUtils.join(lines, SystemUtils.LINE_SEPARATOR)
        );
    }

    /**
     * Escape all toxic characters (they are not suitable for XML documents).
     * @param text Text to escape
     * @return Clean one
     */
    public static String escape(final String text) {
        final StringBuilder output = new StringBuilder();
        for (char chr : text.toCharArray()) {
            output.append(Exceptions.escape(chr));
        }
        return output.toString();
    }

    /**
     * Escape one character.
     * @param chr Char to escape
     * @return Clean one
     * @see <a href="http://www.w3.org/TR/2004/REC-xml11-20040204/#charsets">restricted XML chars</a>
     */
    public static char escape(final char chr) {
        final char output;
        // @checkstyle BooleanExpressionComplexity (5 lines)
        if (chr < '\u0009' || (chr >= '\u000b' && chr <= '\u000c')
            || (chr >= '\u000e' && chr <= '\u001f')
            || (chr >= '\u007f' && chr <= '\u0084')
            || (chr >= '\u0086' && chr <= '\u009f')) {
            output = '?';
        } else {
            output = chr;
        }
        return output;
    }

}
