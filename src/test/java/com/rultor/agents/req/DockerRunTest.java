/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.agents.req;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ${@link DockerRun}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class DockerRunTest {

    /**
     * Space char.
     */
    private static final String SPACE = " ";

    /**
     * DockerRun can fetch environment vars.
     * @throws Exception In case of error.
     */
    @Test
    public void fetchesEnvVars() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new JoinedText(
                    SPACE,
                    "<p><entry key='a'><entry key='env'>",
                    "<item>A=5</item><item>B=f e</item></entry></entry>",
                    "<entry key='b'><entry key='env'>HELLO='1'</entry></entry>",
                    "<entry key='c'><entry key='env'>",
                    "<entry key='MVN'>works</entry></entry></entry></p>"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='a']").envs(
                new ArrayMap<String, String>()
            ),
            Matchers.hasItems("A=5", "B=f e")
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='b']").envs(
                new ArrayMap<String, String>()
            ),
            Matchers.hasItems("HELLO='1'")
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='c']").envs(
                new ArrayMap<String, String>().with("X", "a\"'b")
            ),
            Matchers.hasItems("MVN=works", "X=a\"'b")
        );
    }

    /**
     * DockerRun can fetch script.
     * @throws Exception In case of error.
     */
    @Test
    public void fetchesScript() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new JoinedText(
                    SPACE,
                    "<p><entry key='x'><entry key='script'>",
                    "mvn clean</entry></entry>",
                    "<entry key='y'><entry key='script'>",
                    "<item>pw</item><item>ls</item></entry></entry></p>"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='x']").script(),
            Matchers.hasItems("mvn clean", ";")
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='y']").script(),
            Matchers.hasItems("pw", ";", "ls", ";")
        );
    }

    /**
     * DockerRun can create script with comment inside.
     * @throws Exception In case of error.
     */
    @Test
    public void executesWithComment() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new JoinedText(
                    SPACE,
                    "<p><entry key='z'><entry key='script'>",
                    "<item>echo \"first\"</item>",
                    "<item># some comment</item>",
                    "<item>echo \"# some comment\" more</item>",
                    "<item>echo '# some comment' more</item>",
                    "<item>echo \"second\" # some comment</item>",
                    "<item>echo \"third\" \\# some comment</item>",
                    "<item>echo \"last\"</item>",
                    "</entry></entry>",
                    // @checkstyle MultipleStringLiterals (1 line)
                    "</p>"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='z']").script(),
            Matchers.hasItems(
                "echo \"first\"",
                ";",
                "`# some comment`" ,
                ";",
                "echo \"# some comment\" more" ,
                ";",
                "echo '# some comment' more" ,
                ";",
                "echo \"second\" `# some comment`" ,
                ";",
                "echo \"third\" \\# some comment",
                ";",
                "echo \"last\"",
                ";"
            )
        );
    }

    /**
     * DockerRun can fetch from an empty doc.
     * @throws Exception In case of error.
     */
    @Test
    public void fetchesFromEmptyProfile() throws Exception {
        final Profile profile = new Profile.Fixed(new XMLDocument("<p/>"));
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/absent").envs(
                new ArrayMap<String, String>()
            ),
            Matchers.<String>emptyIterable()
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/doesnt-exist").script(),
            Matchers.<String>emptyIterable()
        );
    }

    /**
     * DockerRun can fetch script.
     * @throws Exception In case of error.
     * @since 1.22
     */
    @Test
    public void fetchesInstallScript() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new JoinedText(
                    SPACE,
                    "<p><entry key='f'><entry key='script'>hi</entry></entry>",
                    "<entry key='install'><item>one</item><item>two</item>",
                    "</entry></p>"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            // @checkstyle MultipleStringLiterals (1 line)
            new DockerRun(profile, "/p/entry[@key='f']").script(),
            Matchers.hasItems("one", ";", "two", ";", "hi", ";")
        );
    }

    /**
     * DockerRun can fetch uninstall script.
     * @throws Exception In case of error.
     * @since 1.22
     */
    @Test
    public void fetchesUninstallScript() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new JoinedText(
                    SPACE,
                    "<p>",
                    "<entry key='uninstall'>",
                    "<item>one</item><item>two</item>",
                    "</entry>",
                    "<entry key='f'><entry key='script'>hi</entry></entry>",
                    "</p>"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            // @checkstyle MultipleStringLiterals (1 line)
            new Brackets(
                new DockerRun(profile, "/p/entry[@key='f']").script()
            ).toString(),
            // @checkstyle LineLength (1 line)
            Matchers.equalTo("( 'function' 'clean_up()' '{' 'one' ';' 'two' ';' '}' ';' 'trap' 'clean_up' 'EXIT' ';' 'hi' ';' )")
        );
    }

    /**
     * DockerRun can fetch environment vars.
     * @throws Exception In case of error.
     * @since 1.22
     */
    @Test
    public void fetchesEnvVarsDefaults() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new JoinedText(
                    SPACE,
                    "<p><entry key='o'><entry key='env'>A=123</entry></entry>",
                    "<entry key='env'>ALPHA=909</entry></p>"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            new DockerRun(profile, "/p/entry[@key='o']").envs(
                new ArrayMap<String, String>()
            ),
            Matchers.hasItems("ALPHA=909", "A=123")
        );
    }

    /**
     * DockerRun can fetch multi-line script.
     * @throws Exception In case of error.
     * @since 1.32.3
     */
    @Test
    public void fetchesMultiLineScript() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                "<p><entry key='script'>How are you,\ndude</entry></p>"
            )
        );
        MatcherAssert.assertThat(
            new Brackets(
                new DockerRun(profile, "/p").script()
            ).toString(),
            Matchers.equalTo("( 'How are you,' ';' 'dude' ';' )")
        );
    }

    /**
     * DockerRun can fetch environment vars from empty list.
     * @throws Exception In case of error.
     */
    @Test
    public void fetchesEnvVarsFromEmptyList() throws Exception {
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                "<p><entry key='ooo'><entry key='env'/></entry></p>"
            )
        );
        MatcherAssert.assertThat(
            new Brackets(
                new DockerRun(profile, "/p/entry[@key='ooo']").envs(
                    new ArrayMap<String, String>()
                )
            ).toString(),
            Matchers.startsWith("(  ")
        );
    }

}
