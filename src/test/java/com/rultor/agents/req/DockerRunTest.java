/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link DockerRun}.
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class DockerRunTest {

    /**
     * Space char.
     */
    private static final String SPACE = " ";

    /**
     * DockerRun can fetch environment vars.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesEnvVarsMultiple() throws Exception {
        MatcherAssert.assertThat(
            "Multiple env items should be saved",
            DockerRun.byXpath(
                DockerRunTest.envsProfile(), "/p/entry[@key='a']"
            ).envs(
                new ArrayMap<>()
            ),
            Matchers.hasItems("A=5", "B=f e")
        );
    }

    /**
     * DockerRun can fetch a single env item.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesEnvVarsSingle() throws Exception {
        MatcherAssert.assertThat(
            "Single even item should be saved",
            DockerRun.byXpath(
                DockerRunTest.envsProfile(), "/p/entry[@key='b']"
            ).envs(
                new ArrayMap<>()
            ),
            Matchers.hasItems("HELLO='1'")
        );
    }

    /**
     * DockerRun can append extra env vars from the envs parameter.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesEnvVarsWithExtras() throws Exception {
        MatcherAssert.assertThat(
            "Additional value should be saved from envs parameter",
            DockerRun.byXpath(
                DockerRunTest.envsProfile(), "/p/entry[@key='c']"
            ).envs(
                new ArrayMap<String, String>().with("X", "a\"'b")
            ),
            Matchers.hasItems("MVN=works", "X=a\"'b")
        );
    }

    /**
     * DockerRun can fetch a single-line script from a profile.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesScript() throws Exception {
        final Profile profile = DockerRunTest.scriptProfile();
        MatcherAssert.assertThat(
            "Script should be read from profile",
            DockerRun.byXpath(profile, "/p/entry[@key='x']").script(),
            Matchers.hasItems("mvn clean", ";")
        );
    }

    /**
     * DockerRun can fetch a multi-item script with separators.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesScriptWithMultipleItems() throws Exception {
        final Profile profile = DockerRunTest.scriptProfile();
        MatcherAssert.assertThat(
            "Script should be read from several items with ;",
            DockerRun.byXpath(profile, "/p/entry[@key='y']").script(),
            Matchers.hasItems("pw", ";", "ls", ";")
        );
    }

    /**
     * DockerRun can create script with comment inside.
     * @throws Exception In case of error.
     */
    @Test
    void executesWithComment() throws Exception {
        MatcherAssert.assertThat(
            "All items from xpath should be in the script even with comment",
            DockerRun.byXpath(
                new Profile.Fixed(
                    new XMLDocument(
                        new Joined(
                            DockerRunTest.SPACE,
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
                ),
                "/p/entry[@key='z']"
            ).script(),
            Matchers.hasItems(
                "echo \"first\"",
                ";",
                "`# some comment`",
                ";",
                "echo \"# some comment\" more",
                ";",
                "echo '# some comment' more",
                ";",
                "echo \"second\" `# some comment`",
                ";",
                "echo \"third\" \\# some comment",
                ";",
                "echo \"last\"",
                ";"
            )
        );
    }

    /**
     * DockerRun can fetch script.
     * @throws Exception In case of error.
     * @since 1.22
     */
    @Test
    void fetchesInstallScript() throws Exception {
        MatcherAssert.assertThat(
            "install should be also in the script",
            DockerRun.byXpath(
                new Profile.Fixed(
                    new XMLDocument(
                        new Joined(
                            DockerRunTest.SPACE,
                            "<p><entry key='f'><entry key='script'>hi</entry></entry>",
                            "<entry key='install'><item>one</item><item>two</item>",
                            "</entry></p>"
                        ).asString()
                    )
                ),
                "/p/entry[@key='f']"
            ).script(),
            Matchers.hasItems("one", ";", "two", ";", "hi", ";")
        );
    }

    /**
     * DockerRun can fetch uninstall script.
     * @throws Exception In case of error.
     * @since 1.22
     */
    @Test
    void fetchesUninstallScript() throws Exception {
        MatcherAssert.assertThat(
            "uninstall should be placed in the script",
            // @checkstyle MultipleStringLiterals (1 line)
            new Brackets(
                DockerRun.byXpath(
                    new Profile.Fixed(
                        new XMLDocument(
                            new Joined(
                                DockerRunTest.SPACE,
                                "<p>",
                                "<entry key='uninstall'>",
                                "<item>one</item><item>two</item>",
                                "</entry>",
                                "<entry key='f'><entry key='script'>hi</entry></entry>",
                                "</p>"
                            ).asString()
                        )
                    ),
                    "/p/entry[@key='f']"
                ).script()
            ).toString(),
            // @checkstyle LineLengthCheck (3 line)
            Matchers.equalTo(
                "( 'function' 'clean_up()' '{' 'one' ';' 'two' ';' '}' ';' 'trap' 'clean_up' 'EXIT' ';' 'hi' ';' )"
            )
        );
    }

    /**
     * DockerRun can fetch environment vars.
     * @throws Exception In case of error.
     * @since 1.22
     */
    @Test
    void fetchesEnvVarsDefaults() throws Exception {
        MatcherAssert.assertThat(
            "Env variables should be read from the xpath",
            DockerRun.byXpath(
                new Profile.Fixed(
                    new XMLDocument(
                        new Joined(
                            DockerRunTest.SPACE,
                            "<p><entry key='o'><entry key='env'>A=123</entry></entry>",
                            "<entry key='env'>ALPHA=909</entry></p>"
                        ).asString()
                    )
                ),
                "/p/entry[@key='o']"
            ).envs(
                new ArrayMap<>()
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
    void fetchesMultiLineScript() throws Exception {
        MatcherAssert.assertThat(
            "multiline script should be place in script as two commands",
            new Brackets(
                DockerRun.byXpath(
                    new Profile.Fixed(
                        new XMLDocument(
                            String.format("<p><entry key='script'>How are you,%ndude</entry></p>")
                        )
                    ),
                    "/p"
                ).script()
            ).toString(),
            Matchers.equalTo("( 'How are you,' ';' 'dude' ';' )")
        );
    }

    /**
     * DockerRun can skip empty lines in a multi-line script.
     * @throws Exception In case of error.
     * @since 1.32.3
     */
    @Test
    void skipsEmptyLinesInMultiLineScript() throws Exception {
        MatcherAssert.assertThat(
            "empty lines should be skipped",
            new Brackets(
                DockerRun.byXpath(
                    new Profile.Fixed(
                        new XMLDocument(
                            String.format(
                                "<p><entry key='script'>echo 1%necho 2%n%n%necho 3</entry></p>"
                            )
                        )
                    ),
                    "/p"
                ).script()
            ).toString(),
            Matchers.equalTo("( 'echo 1' ';' 'echo 2' ';' 'echo 3' ';' )")
        );
    }

    /**
     * DockerRun can fetch environment vars from empty list.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesEnvVarsFromEmptyList() throws Exception {
        MatcherAssert.assertThat(
            "Empty env variables are allowed",
            new Brackets(
                DockerRun.byXpath(
                    new Profile.Fixed(
                        new XMLDocument(
                            "<p><entry key='ooo'><entry key='env'/></entry></p>"
                        )
                    ),
                    "/p/entry[@key='ooo']"
                ).envs(
                    new ArrayMap<>()
                )
            ).toString(),
            Matchers.startsWith("(  ")
        );
    }
    /**
     * Build a fixed profile with env-related entries.
     * @return Profile
     * @throws Exception In case of error.
     */
    private static Profile envsProfile() throws Exception {
        return new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    DockerRunTest.SPACE,
                    "<p><entry key='a'><entry key='env'>",
                    "<item>A=5</item><item>B=f e</item></entry></entry>",
                    "<entry key='b'><entry key='env'>HELLO='1'</entry></entry>",
                    "<entry key='c'><entry key='env'>",
                    "<entry key='MVN'>works</entry></entry></entry></p>"
                ).asString()
            )
        );
    }

    /**
     * Build a fixed profile with script-related entries.
     * @return Profile
     * @throws Exception In case of error.
     */
    private static Profile scriptProfile() throws Exception {
        return new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    DockerRunTest.SPACE,
                    "<p><entry key='x'><entry key='script'>",
                    "mvn clean</entry></entry>",
                    "<entry key='y'><entry key='script'>",
                    "<item>pw</item><item>ls</item></entry></entry></p>"
                ).asString()
            )
        );
    }
}
