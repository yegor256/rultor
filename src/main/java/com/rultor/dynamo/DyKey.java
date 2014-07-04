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
package com.rultor.dynamo;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Item;
import com.rultor.spi.Key;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Key in Dynamo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class DyKey implements Key {

    /**
     * Item.
     */
    private final transient Item item;

    /**
     * Name of the key.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param itm Item
     * @param key The name
     */
    DyKey(final Item itm, final String key) {
        this.item = itm;
        this.name = key;
    }

    @Override
    public boolean exists() throws IOException {
        return this.json().containsKey(this.name);
    }

    @Override
    public String value() throws IOException {
        return this.json().getString(this.name);
    }

    @Override
    public void put(final String value) throws IOException {
        final JsonObject json = this.json();
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        for (final Map.Entry<String, JsonValue> entry : json.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        builder.add(this.name, value);
        final StringWriter writer = new StringWriter();
        Json.createWriter(writer).writeObject(builder.build());
        this.item.put(
            DyRepos.ATTR_STATE,
            new AttributeValueUpdate().withValue(
                new AttributeValue().withS(writer.toString())
            )
        );
    }

    /**
     * Get JSON.
     * @return Json
     */
    private JsonObject json() throws IOException {
        return Json.createReader(
            new StringReader(this.item.get(DyRepos.ATTR_STATE).getS())
        ).readObject();
    }

}
