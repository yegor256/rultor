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
import com.rultor.tools.Time;
import com.rultor.tools.Vext;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.xembly.XemblyBuilder;

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
        final String label = this.label(method);
        final ImmutableMap.Builder<String, Object> args =
            new ImmutableMap.Builder<String, Object>()
                .put("this", point.getThis())
                .put("args", point.getArgs());
        XemblyLine.log(
            new XemblyBuilder()
                .xpath("/spanshot")
                .addIfAbsent("steps").strict(1)
                .add("step").strict(1)
                .attr("id", label)
                .add("start").set(new Time().toString()).up()
                .add("summary")
                .set(new Vext(step.before()).print(args.build()))
        );
        try {
            final Object result = point.proceed();
            if (result != null) {
                args.put("result", result);
            }
            XemblyLine.log(
                new XemblyBuilder()
                    .xpath(String.format("//step[@id='%s']/summary", label))
                    .strict(1)
                    .set(new Vext(step.value()).print(args.build()))
                    .up()
                    // @checkstyle MultipleStringLiterals (1 line)
                    .add("level").set(Level.INFO.toString())
            );
            return result;
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            XemblyLine.log(
                new XemblyBuilder()
                    .xpath(String.format("//step[@id = '%s']", label))
                    .strict(1)
                    .add("level").set(Level.SEVERE.toString())
            );
            throw ex;
        } finally {
            XemblyLine.log(
                new XemblyBuilder()
                    .xpath(String.format("//step[@id='%s']", label))
                    .strict(1)
                    .add("finish").set(new Time().toString())
            );
        }
    }

    /**
     * Make a unique label for the given method.
     * @param method The method
     * @return Unique label
     */
    private String label(final Method method) {
        return String.format(
            "%s-%d", method.getName(), Math.abs(new Random().nextInt())
        );
    }

}

