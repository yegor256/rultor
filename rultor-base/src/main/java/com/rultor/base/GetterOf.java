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
package com.rultor.base;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Proxy;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Retrieves a string property from an object.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "source", "property" })
@Loggable(Loggable.DEBUG)
public final class GetterOf implements Proxy<Object> {

    /**
     * Object from which to get property.
     */
    private final transient Object source;

    /**
     * Name of the property to retrieve.
     */
    private final transient String property;

    /**
     * Public ctor.
     * @param src Source of the property.
     * @param prop Property name.
     */
    public GetterOf(
        @NotNull(message = "object can't be null") final Object src,
        @NotNull(message = "property can't be null") final String prop
    ) {
        this.source = src;
        this.property = prop;
    }

    /**
     * Find method with getter.
     * @param info Bean info.
     * @return Found method.
     */
    private Method find(final BeanInfo info) {
        Method found = null;
        for (PropertyDescriptor descr : info.getPropertyDescriptors()) {
            if (descr.getName().equals(this.property)
                && (descr.getReadMethod() != null)) {
                found = descr.getReadMethod();
                break;
            }
        }
        if (found == null) {
            for (MethodDescriptor descr : info.getMethodDescriptors()) {
                if (descr.getName().equals(this.property)
                    && (descr.getMethod().getParameterTypes().length == 0)) {
                    found = descr.getMethod();
                    break;
                }
            }
        }
        if (found == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Object should have a getter for property '%s'",
                    this.property
                )
            );
        }
        return found;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object object() {
        try {
            final Method method =
                this.find(Introspector.getBeanInfo(this.source.getClass()));
            method.setAccessible(true);
            return method.invoke(this.source);
        } catch (IntrospectionException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Gets '%s' property from %s", this.property, this.source
        );
    }
}
