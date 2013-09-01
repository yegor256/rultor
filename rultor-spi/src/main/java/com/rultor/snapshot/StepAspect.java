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
package com.rultor.snapshot;

import com.google.common.collect.ImmutableMap;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import com.rultor.tools.Vext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.xembly.Directives;

/**
 * Step AspectJ aspect.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Aspect
public final class StepAspect {

    /**
     * Random generator.
     */
    private static final Random RND = new SecureRandom();

    /**
     * Track execution of a method annotated with {@link Step}.
     * @param point Join point
     * @return Result of the method
     * @throws Throwable If fails
     * @checkstyle IllegalThrow (4 lines)
     */
    @Around("execution(* * (..)) && @annotation(com.rultor.snapshot.Step)")
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object track(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            MethodSignature.class.cast(point.getSignature()).getMethod();
        final Step step = method.getAnnotation(Step.class);
        final String label = Integer.toString(
            Math.abs(StepAspect.RND.nextInt())
        );
        final ImmutableMap.Builder<String, Object> args =
            new ImmutableMap.Builder<String, Object>()
                .put("this", new StepAspect.Open(point.getThis()))
                .put("args", point.getArgs());
        final String before;
        if (step.before().isEmpty()) {
            before = String.format(
                "%s#%s()",
                method.getDeclaringClass().getCanonicalName(), method.getName()
            );
        } else {
            before = new Vext(step.before()).print(args.build());
        }
        new XemblyLine(
            new Directives()
                .xpath("/snapshot")
                .addIfAbsent("steps").strict(1)
                .add("step").strict(1)
                .attr("id", label)
                // @checkstyle MultipleStringLiterals (1 line)
                .attr("class", method.getDeclaringClass().getCanonicalName())
                .attr("method", method.getName())
                .add("start").set(new Time().toString()).up()
                .add("summary")
                .set(before)
        ).log();
        final long start = System.currentTimeMillis();
        try {
            final Object result = point.proceed();
            if (result != null) {
                args.put("result", result);
            }
            this.mark(label, Level.INFO);
            new XemblyLine(
                new Directives()
                    .xpath(String.format("//step[@id=%s]/summary", label))
                    .strict(1)
                    .set(new Vext(step.value()).print(args.build()))
            ).log();
            return result;
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            this.mark(label, Level.SEVERE);
            new XemblyLine(
                new Directives()
                    // @checkstyle MultipleStringLiterals (1 line)
                    .xpath(String.format("/snapshot/steps/step[@id=%s]", label))
                    .add("exception")
                    .add("class")
                    .set(ex.getClass().getCanonicalName()).up()
                    .add("stacktrace")
                    .set(Exceptions.stacktrace(ex)).up()
                    .add("cause").set(Exceptions.message(ex))
            ).log();
            throw ex;
        } finally {
            new XemblyLine(
                new Directives()
                    .xpath(String.format("/snapshot/steps/step[@id=%s]", label))
                    .strict(1)
                    .add("finish").set(new Time().toString()).up()
                    .add("duration")
                    .set(Long.toString(System.currentTimeMillis() - start))
            ).log();
        }
    }

    /**
     * Set log level to the given step (by ID).
     * @param label Label of the step
     * @param level Level to set
     */
    private void mark(final String label, final Level level) {
        new XemblyLine(
            new Directives()
                .xpath(String.format("/snapshot/steps/step[@id=%s]", label))
                .strict(1)
                .add("level").set(level.toString())
        ).log();
    }

    /**
     * Open object for velocity rendering of all private properties.
     */
    @EqualsAndHashCode(of = "subject")
    public static final class Open {
        /**
         * The subject.
         */
        private final transient Object subject;
        /**
         * Protected ctor.
         * @param subj The subject to open
         */
        protected Open(final Object subj) {
            this.subject = subj;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.subject.toString();
        }
        /**
         * Get property.
         * @param name Name of it
         * @return The object or exception if it's absent
         * @throws Exception If fails
         */
        public Object get(final String name) throws Exception {
            final Field field = this.subject.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(this.subject);
        }
    }

}

