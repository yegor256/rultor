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

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Concat}.
 * @author Vaibhav Paliwal (vaibhavpaliwal99@gmail.com)
 * @version $Id$
 */
public final class ConcatTest {
    /**
     * Concat ArrayList of String with Separator.
     */
    @Test
    public void concatWithSeparator() {
        final List<String> list = new ArrayList<String>();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        final Concat concat = new Concat(
            list, ","
        );
        MatcherAssert.assertThat(
            concat.object(),
            Matchers.equalTo("test1,test2,test3")
        );
        MatcherAssert.assertThat(
            concat.toString(),
            Matchers.equalTo("3 part(s)")
        );
    }

    /**
     * Concat ArrayList of String without Separator.
     */
    @Test
    public void concatWithoutSeparator() {
        final List<String> list = new ArrayList<String>();
        list.add("test4");
        list.add("test5");
        list.add("test6");
        list.add("test7");
        final Concat concat = new Concat(
            list
        );
        MatcherAssert.assertThat(
            concat.object(),
            Matchers.equalTo("test4test5test6test7")
        );
        MatcherAssert.assertThat(
            concat.toString(),
            Matchers.equalTo("4 part(s)")
        );
    }
}
