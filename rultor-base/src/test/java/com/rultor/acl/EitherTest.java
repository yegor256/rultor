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
package com.rultor.acl;

import com.jcabi.urn.URN;
import com.rultor.spi.ACL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Either}.
 * @author Gangababu Tirumalanadhuni (gangababu.t@gmail.com)
 * @version $Id$
 */
public final class EitherTest {

    /**
     * Either should not have null ACLs list.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void aclsCantBeNull() throws Exception {
        new Either(null);
    }

    /**
     * Either can view when any URN matches.
     * @throws Exception If some problem inside
     */
    @Test
    public void canViewWhenEitherACLMatches() throws Exception {
        final String urn = "urn:github:555";
        final List<ACL> acls = new ArrayList<ACL>(0);
        acls.add(new Prohibited());
        acls.add(new OpenView());
        acls.add(new WhiteList(Arrays.asList(urn)));
        MatcherAssert.assertThat(
            new Either(acls).canView(URN.create(urn)), Matchers.equalTo(true)
        );
    }

    /**
     * Either does not allow to view when no URN matches.
     * @throws Exception If some problem inside
     */
    @Test
    public void cantViewWhenAnyACLDoesNotMatch() throws Exception {
        final String test = "urn:test:2";
        final List<ACL> acls = new ArrayList<ACL>(0);
        acls.add(new Prohibited());
        acls.add(new WhiteList(Arrays.asList("urn:test:1")));
        MatcherAssert.assertThat(
            new Either(acls).canView(URN.create(test)), Matchers.equalTo(false)
        );
    }

    /**
     * Either can view always with at least one OpenView ACL.
     * @throws Exception If some problem inside
     */
    @Test
    public void canViewWhenAtleastOneOpenViewACL() throws Exception {
        final String test = "urn:test:4";
        final List<ACL> acls = new ArrayList<ACL>(0);
        acls.add(new Prohibited());
        acls.add(new WhiteList(Arrays.asList("urn:test:3")));
        acls.add(new MD5Keyed("a64cad1db8be410c666716f680e8a1234"));
        acls.add(new OpenView());
        acls.add(new Prohibited());
        MatcherAssert.assertThat(
            new Either(acls).canView(URN.create(test)), Matchers.equalTo(true)
        );
    }

    /**
     * Either can post when any key matches.
     * @throws Exception If some problem inside
     */
    @Test
    public void canPostWhenEitherACLMatches() throws Exception {
        final String urn = "urn:github:999";
        final List<ACL> acls = new ArrayList<ACL>(0);
        acls.add(new Prohibited());
        acls.add(new OpenView());
        acls.add(new MD5Keyed("a64cad1db8be410c666716f680e8a135"));
        acls.add(new WhiteList(Arrays.asList(urn)));
        MatcherAssert.assertThat(
            new Either(acls).canPost("valid-key"), Matchers.equalTo(true)
        );
    }

    /**
     * Either does not allow to post when no URN matches.
     * @throws Exception If some problem inside
     */
    @Test
    public void cannotPostWhenAnyACLDoesNotMatch() throws Exception {
        final List<ACL> acls = new ArrayList<ACL>(0);
        acls.add(new Prohibited());
        acls.add(new MD5Keyed("a64cad1db8be410c666716f680e8a1345"));
        acls.add(new WhiteList(Arrays.asList("urn:test:5")));
        MatcherAssert.assertThat(
            new Either(acls).canPost("invalid-key"), Matchers.equalTo(false)
        );
    }
}
