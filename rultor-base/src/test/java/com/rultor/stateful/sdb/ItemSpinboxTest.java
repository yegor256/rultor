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

package com.rultor.stateful.sdb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.jcabi.aspects.Tv;
import com.rultor.aws.SDBClient;
import com.rultor.spi.Wallet;
import java.util.ArrayList;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ItemSpinbox}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
public final class ItemSpinboxTest {

    /**
     * ItemSpinbox can add to empty wallet.
     */
    @Test
    public void canAddToEmptyWallet() {
        final SDBClient client = Mockito.mock(SDBClient.class);
        final AmazonSimpleDB aws = Mockito.mock(AmazonSimpleDB.class);
        Mockito.doReturn(aws).when(client).get();
        final GetAttributesResult gar = Mockito.mock(GetAttributesResult.class);
        Mockito.doReturn(gar).when(aws).getAttributes(
            Mockito.any(GetAttributesRequest.class)
        );
        Mockito.doReturn(new ArrayList<Attribute>(0)).when(gar).getAttributes();
        Mockito.doNothing().when(aws).putAttributes(
            Mockito.any(
                PutAttributesRequest.class
            )
        );
        final Wallet wlt = new Wallet.Empty();
        final ItemSpinbox box = new ItemSpinbox(wlt, "test", client);
        final long deposit = Tv.EIGHT;
        final long balance = box.add(deposit);
        Mockito.verify(aws).getAttributes(
            Mockito.any(
                GetAttributesRequest.class
            )
        );
        Mockito.verify(aws).putAttributes(
            Mockito.any(
                PutAttributesRequest.class
            )
        );
        MatcherAssert.assertThat(balance, Matchers.equalTo(deposit));
    }

    /**
     * ItemSpinbox can add to wallet.
     */
    @Test
    public void canAddToWallet() {
        final SDBClient client = Mockito.mock(SDBClient.class);
        final AmazonSimpleDB aws = Mockito.mock(AmazonSimpleDB.class);
        Mockito.doReturn(aws).when(client).get();
        final GetAttributesResult gar = Mockito.mock(GetAttributesResult.class);
        Mockito.doReturn(gar).when(aws).getAttributes(
            Mockito.any(GetAttributesRequest.class)
        );
        final Attribute atr = Mockito.mock(Attribute.class);
        Mockito.doReturn(Arrays.asList(atr)).when(gar).getAttributes();
        Mockito.doNothing().when(aws).putAttributes(
            Mockito.any(
                PutAttributesRequest.class
            )
        );
        final Wallet wlt = new Wallet.Empty();
        final ItemSpinbox box = new ItemSpinbox(wlt, "testac", client);
        Mockito.doReturn("10").when(atr).getValue();
        final long deposit = Tv.FIVE;
        final long balance = box.add(deposit);
        Mockito.verify(aws).getAttributes(
            Mockito.any(
                GetAttributesRequest.class
            )
        );
        Mockito.verify(aws).putAttributes(
            Mockito.any(
                PutAttributesRequest.class
            )
        );
        final long total = Tv.FIFTEEN;
        MatcherAssert.assertThat(balance, Matchers.equalTo(total));
    }
}
