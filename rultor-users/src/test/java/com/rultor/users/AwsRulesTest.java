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
package com.rultor.users;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import com.rultor.spi.Spec;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AwsRules}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class AwsRulesTest {

    /**
     * Check that null constrain is enforced for normal contains call.
     */
    @Test
    public void containsNotEmpty() {
        MatcherAssert.assertThat(
            new AwsRules(
                new RegionMocker().mock(),
                Mockito.mock(SQSClient.class),
                new URN()
            ).contains("test"),
            Matchers.is(true)
        );
    }

    /**
     * Check that null constrain is enforced for blank contains call.
     */
    @Test(expected = ConstraintViolationException.class)
    public void containsBlank() {
        new AwsRules(
            new RegionMocker().mock(), Mockito.mock(SQSClient.class), new URN()
        ).contains("");
    }

    /**
     * Check that null constrain is enforced for null contains call.
     */
    @Test(expected = ConstraintViolationException.class)
    public void containsNull() {
        new AwsRules(
            new RegionMocker().mock(),
            Mockito.mock(SQSClient.class),
            new URN()
        ).contains(null);
    }

    /**
     * Check that null constrain is enforced for normal get call.
     */
    @Test
    public void retrievesElementByGetCall() {
        MatcherAssert.assertThat(
            new AwsRules(
                new RegionMocker().with(Mockito.mock(Item.class)).mock(),
                Mockito.mock(SQSClient.class),
                new URN()
            ).get("other"),
            Matchers.notNullValue()
        );
    }

    /**
     * Check that null constrain is enforced for null get call.
     */
    @Test(expected = ConstraintViolationException.class)
    public void getNull() {
        new AwsRules(
            new RegionMocker().mock(),
            Mockito.mock(SQSClient.class),
            new URN()
        ).get(null);
    }

    /**
     * Check that null constrain is enforced for blank get call.
     */
    @Test(expected = ConstraintViolationException.class)
    public void getBlank() {
        new AwsRules(
            new RegionMocker().mock(),
            Mockito.mock(SQSClient.class),
            new URN()
        ).get("");
    }

    /**
     * AwsRules can cache and flush.
     */
    @Test
    public void cachesAndFlushesListOfRules() {
        final Item item = Mockito.mock(Item.class);
        final String name = "rule-name-test";
        Mockito.doReturn(new AttributeValue(name))
            .when(item).get(Mockito.anyString());
        final Region region = new RegionMocker().with(item).mock();
        final Rules rules = new AwsRules(
            region,
            Mockito.mock(SQSClient.class),
            new URN()
        );
        final Table table = region.table("");
        final Rule rule = rules.iterator().next();
        MatcherAssert.assertThat(rule.name(), Matchers.equalTo(name));
        rule.update(new Spec.Simple(), new Spec.Simple());
        rules.iterator();
        rules.iterator();
        Mockito.verify(table, Mockito.times(2)).frame();
    }

    /**
     * AwsRules can cache results of get() and iterator() as same rules.
     */
    @Test
    public void returnsGetAndIteratorAsSameRules() {
        final Item item = Mockito.mock(Item.class);
        final String name = "rule-name-foo";
        Mockito.doReturn(new AttributeValue(name))
            .when(item).get(Mockito.anyString());
        final Region region = new RegionMocker().with(item).mock();
        final Rules rules = new AwsRules(
            region,
            Mockito.mock(SQSClient.class),
            new URN()
        );
        final Table table = region.table("");
        MatcherAssert.assertThat(
            rules.get(name).name(),
            Matchers.equalTo(name)
        );
        MatcherAssert.assertThat(
            rules.iterator().next().name(),
            Matchers.equalTo(name)
        );
        Mockito.verify(table, Mockito.times(1)).frame();
    }

}
