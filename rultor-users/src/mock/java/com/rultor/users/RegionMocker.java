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

import com.amazonaws.AmazonServiceException;
import com.jcabi.dynamo.Frame;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.dynamo.Valve;
import java.util.Iterator;
import org.hamcrest.Matchers;
import org.mockito.Mockito;

/**
 * Mocker for {@link Region}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class RegionMocker {
    /**
     * Create a mocked region that throws exception in case of wrong arguments.
     * @return Mocked region.
     */
    @SuppressWarnings("unchecked")
    public Region mock() {
        final Region region = Mockito.mock(Region.class);
        final Table table = Mockito.mock(Table.class);
        final Frame frame = Mockito.mock(Frame.class);
        Mockito.when(region.table(Mockito.anyString())).thenReturn(table);
        Mockito.when(table.frame()).thenReturn(frame);
        Mockito.when(frame.isEmpty()).thenReturn(false);
        final Iterator<Item> iterator = Mockito.mock(Iterator.class);
        Mockito.when(frame.iterator()).thenReturn(iterator);
        Mockito.when(iterator.next()).thenReturn(null);
        Mockito.when(frame.through(Mockito.any(Valve.class))).thenReturn(frame);
        Mockito.when(
            frame.where(
                Mockito.anyString(),
                Mockito.argThat(Matchers.not(Matchers.isEmptyString()))
            )
        ).thenReturn(frame);
        Mockito.when(
            frame.where(
                Mockito.anyString(),
                Mockito.argThat(Matchers.isEmptyString())
            )
        ).thenThrow(new AmazonServiceException(""));
        return region;
    }
}
