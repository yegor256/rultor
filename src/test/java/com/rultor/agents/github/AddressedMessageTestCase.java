/**
 * Copyright (c) 2009-2016, rultor.com
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
package com.rultor.agents.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit tests for {@link AddressedMessage}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 *
 */
public final class AddressedMessageTestCase {

    /**
     * AddressedMessage can address a message to one user.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void addressesMessageToOneUser() throws Exception {
        final Issue issue = AddressedMessageTestCase.issue();
        issue.comments().post("hi there");
        final Comment.Smart com  = new Comment.Smart(issue.comments().get(1));
        final AddressedMessage adm = new AddressedMessage(
            com, "test", Arrays.asList("john")
        );
        final String message = adm.body();
        MatcherAssert.assertThat(
            message,
            Matchers.containsString("@john test")
        );
    }

    /**
     * AddressedMessage can address a message to more users.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void addressesMessageMoreUsers() throws Exception {
        final Issue issue = AddressedMessageTestCase.issue();
        issue.comments().post("hello");
        final Comment.Smart com  = new Comment.Smart(issue.comments().get(1));
        final AddressedMessage adm = new AddressedMessage(
            com, "this is a test", Arrays.asList("amihaiemil", "vlad", "marius")
        );
        final String message = adm.body();
        MatcherAssert.assertThat(
            message,
            Matchers.containsString("@amihaiemil @vlad @marius this is a test")
        );
    }

    /**
     * AddressedMessage throws IllegalArgumentException if no logins are given.
     * @throws Exception If something goes wrong
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnEmptyLogins() throws Exception {
        final Issue issue = AddressedMessageTestCase.issue();
        issue.comments().post("hey");
        final Comment.Smart com  = new Comment.Smart(issue.comments().get(1));
        new AddressedMessage(
            com, "this is just a test", new ArrayList<String>(1)
        );
    }

    /**
     * Make an issue.
     * @return Issue
     * @throws IOException If fails
     */
    private static Issue issue() throws IOException {
        final Repo repo = new MkGithub().randomRepo();
        return repo.issues().create("", "");
    }
}
