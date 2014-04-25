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

import com.fasterxml.jackson.core.JsonParseException;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * Norm JSON.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(of = "schema")
@Loggable(Loggable.DEBUG)
public final class NormJson {

    /**
     * Schema factory.
     */
    private static final JsonSchemaFactory FACTORY =
        JsonSchemaFactory.byDefault();

    /**
     * Schema.
     */
    private final transient String schema;

    /**
     * Public ctor.
     * @param json Schema in JSON format
     */
    public NormJson(final String json) {
        this.schema = json;
    }

    /**
     * Public ctor.
     * @param json Schema in JSON format
     */
    public NormJson(final InputStream json) {
        try {
            this.schema = IOUtils.toString(json, CharEncoding.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Read JSON and return it as an object.
     * @param json JSON to parse
     * @return Object found there
     * @throws NormJson.JsonException If fails
     */
    @Loggable(value = Loggable.DEBUG, ignore = NormJson.JsonException.class)
    public JsonObject readObject(final String json)
        throws NormJson.JsonException {
        final ProcessingReport report;
        try {
            report = NormJson.FACTORY
                .getJsonSchema(JsonLoader.fromString(this.schema))
                .validate(JsonLoader.fromString(json));
        } catch (final ProcessingException ex) {
            throw new NormJson.JsonException(ex);
        } catch (final JsonParseException ex) {
            throw new NormJson.JsonException(ex);
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (!report.isSuccess()) {
            throw new NormJson.JsonException(StringUtils.join(report, ";"));
        }
        try {
            return Json.createReader(new StringReader(json)).readObject();
        } catch (final javax.json.JsonException ex) {
            throw new NormJson.JsonException(ex);
        }
    }

    /**
     * When fails to parse.
     */
    public static final class JsonException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x987ef4afeb3ef907L;
        /**
         * Ctor.
         * @param cause Cause of it
         */
        private JsonException(final Throwable cause) {
            super(cause);
        }
        /**
         * Ctor.
         * @param cause Cause of it
         */
        private JsonException(final String cause) {
            super(cause);
        }
    }

}
