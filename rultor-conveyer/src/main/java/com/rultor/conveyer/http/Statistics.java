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
package com.rultor.conveyer.http;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MonitorInfo;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharUtils;

/**
 * JVM statistics to render in HTTP.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
final class Statistics {

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder()
            .append(this.jvm())
            .append(CharUtils.LF)
            .append(this.runtime())
            .append(CharUtils.LF)
            .append(this.system())
            .append(CharUtils.LF).append(CharUtils.LF)
            .append(this.memory())
            .append(CharUtils.LF).append(CharUtils.LF)
            .append(this.locks())
            .append(CharUtils.LF).append(CharUtils.LF)
            .append(this.deadlocks())
            .append(CharUtils.LF).append(CharUtils.LF)
            .append(this.threads())
            .toString();
    }

    /**
     * All details about JVM.
     * @return Plain text to render
     */
    @NotNull
    private String jvm() {
        return new StringBuilder()
            .append("Java version: ")
            .append(System.getProperty("java.version"))
            .toString();
    }

    /**
     * All details about runtime.
     * @return Plain text to render
     */
    @NotNull
    private String runtime() {
        return new StringBuilder()
            .append("Available Processors: ")
            .append(Runtime.getRuntime().availableProcessors())
            .append(CharUtils.LF)
            .append("Free Memory: ")
            .append(
                FileUtils.byteCountToDisplaySize(
                    Runtime.getRuntime().freeMemory()
                )
            )
            .append(CharUtils.LF)
            .append("Max Memory: ")
            .append(
                FileUtils.byteCountToDisplaySize(
                    Runtime.getRuntime().maxMemory()
                )
            )
            .append(CharUtils.LF)
            .append("Total Memory: ")
            .append(
                FileUtils.byteCountToDisplaySize(
                    Runtime.getRuntime().totalMemory()
                )
            )
            .toString();
    }

    /**
     * All details about operating system.
     * @return Plain text to render
     */
    @NotNull
    private String system() {
        final OperatingSystemMXBean bean =
            ManagementFactory.getOperatingSystemMXBean();
        return new StringBuilder()
            .append(CharUtils.LF)
            .append("Architecture: ")
            .append(bean.getArch())
            .append(CharUtils.LF)
            .append("Operating System: ")
            .append(bean.getName())
            .append(CharUtils.LF)
            .append("OS Version: ")
            .append(bean.getVersion())
            .append(CharUtils.LF)
            .append("Load Average: ")
            .append(bean.getSystemLoadAverage())
            .toString();
    }

    /**
     * All details about memory.
     * @return Plain text to render
     */
    @NotNull
    private String memory() {
        final MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        return new StringBuilder()
            .append("Heap Memory Usage: ")
            .append(bean.getHeapMemoryUsage())
            .append(CharUtils.LF)
            .append("Non-Heap Memory Usage: ")
            .append(bean.getNonHeapMemoryUsage())
            .append(CharUtils.LF)
            .append("Objects Pending Finalization: ")
            .append(bean.getObjectPendingFinalizationCount())
            .toString();
    }

    /**
     * Shows all locks.
     * @return Plain text to render
     */
    @NotNull
    private String locks() {
        final StringBuilder text = new StringBuilder();
        final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        for (ThreadInfo info
            : mbean.getThreadInfo(mbean.getAllThreadIds(), true, true)) {
            if (text.length() > 0) {
                text.append(CharUtils.LF);
            }
            text.append(String.format("%s: ", info.getThreadName()));
            for (LockInfo lock : info.getLockedSynchronizers()) {
                text.append(
                    String.format(
                        "%s/%x ",
                        lock.getClassName(),
                        lock.getIdentityHashCode()
                    )
                );
            }
            for (MonitorInfo monitor : info.getLockedMonitors()) {
                text.append(
                    String.format(
                        "\n   %s#%s[%d]",
                        monitor.getLockedStackFrame().getClassName(),
                        monitor.getLockedStackFrame().getMethodName(),
                        monitor.getLockedStackFrame().getLineNumber()
                    )
                );
            }
        }
        return text.toString();
    }

    /**
     * Shows all deadlocks.
     * @return Plain text to render
     */
    @NotNull
    private String deadlocks() {
        final StringBuilder text = new StringBuilder();
        final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long[] deadlocks = mbean.findDeadlockedThreads();
        if (deadlocks == null) {
            deadlocks = new long[] {};
        }
        for (long thread : deadlocks) {
            if (text.length() > 0) {
                text.append(CharUtils.LF);
            }
            final ThreadInfo info = mbean.getThreadInfo(thread);
            text.append(
                String.format(
                    "%s %s %s",
                    info.getThreadName(),
                    info.getLockName(),
                    info.getLockOwnerName(),
                    info.getLockInfo().getClassName()
                )
            );
            for (MonitorInfo monitor : info.getLockedMonitors()) {
                text.append(
                    String.format(
                        "\n  %d %s#%s[%d]",
                        monitor.getLockedStackDepth(),
                        monitor.getLockedStackFrame().getClassName(),
                        monitor.getLockedStackFrame().getMethodName(),
                        monitor.getLockedStackFrame().getLineNumber()
                    )
                );
            }
            for (LockInfo lock : info.getLockedSynchronizers()) {
                text.append(
                    String.format(
                        "\n  %s %x",
                        lock.getClassName(),
                        lock.getIdentityHashCode()
                    )
                );
            }
        }
        return text.toString();
    }

    /**
     * All details about threads.
     * @return Plain text to render
     */
    @NotNull
    @SuppressWarnings("PMD.DoNotUseThreads")
    private String threads() {
        final StringBuilder text = new StringBuilder();
        for (Map.Entry<Thread, StackTraceElement[]> entry
            : Thread.getAllStackTraces().entrySet()) {
            if (text.length() > 0) {
                text.append(CharUtils.LF);
            }
            final Thread thread = entry.getKey();
            text.append(
                String.format(
                    "%s %s %s alive=%B daemon=%B interrupted=%B",
                    thread.getName(),
                    thread.getState(),
                    thread.getClass().getName(),
                    thread.isAlive(),
                    thread.isDaemon(),
                    thread.isInterrupted()
                )
            );
            if (thread.getState().equals(Thread.State.BLOCKED)) {
                for (StackTraceElement line : entry.getValue()) {
                    text.append(
                        String.format(
                            "\n  %s#%s[%d]",
                            line.getClassName(),
                            line.getMethodName(),
                            line.getLineNumber()
                        )
                    );
                }
            }
        }
        return text.toString();
    }

}
