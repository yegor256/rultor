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
package com.rultor.agents;

import co.stateful.mock.MkSttc;
import com.google.common.collect.Lists;
import com.jcabi.github.Assignees;
import com.jcabi.github.Branches;
import com.jcabi.github.Collaborators;
import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Contents;
import com.jcabi.github.Coordinates;
import com.jcabi.github.DeployKeys;
import com.jcabi.github.Event;
import com.jcabi.github.Forks;
import com.jcabi.github.Gists;
import com.jcabi.github.Git;
import com.jcabi.github.Github;
import com.jcabi.github.Gitignores;
import com.jcabi.github.Hooks;
import com.jcabi.github.Issue;
import com.jcabi.github.IssueEvents;
import com.jcabi.github.IssueLabels;
import com.jcabi.github.Issues;
import com.jcabi.github.Labels;
import com.jcabi.github.Language;
import com.jcabi.github.Limits;
import com.jcabi.github.Markdown;
import com.jcabi.github.Milestones;
import com.jcabi.github.Notifications;
import com.jcabi.github.Organizations;
import com.jcabi.github.Pulls;
import com.jcabi.github.Releases;
import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommits;
import com.jcabi.github.Repos;
import com.jcabi.github.Search;
import com.jcabi.github.Stars;
import com.jcabi.github.Users;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.http.Request;
import com.jcabi.http.request.FakeRequest;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.ssh.SSHD;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Integration test for ${@link Agents}.
 *
 * @author Denys Skalenko (d.skalenko@gmail.com)
 * @version $Id$
 * @since 2.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 line)
 */
@SuppressWarnings("all")
public final class AgentsITCase {

    /**
     * Architect login.
     */
    private static final String ARCHITECT = "architect";

    /**
     * Simple user login.
     */
    private static final String USER = "user";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Agents can process a merge.
     * @throws Exception In case of error.
     */
    @Test
    public void processesMerge() throws Exception {
        final AgentsITCase.MockGithub github =
            // @checkstyle MultipleStringLiterals (500 line)
            new AgentsITCase.MockGithub(new MkGithub("rultor"));
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().get(
            // @checkstyle MultipleStringLiterals (1 line)
            repo.pulls().create("title", "head", "base").number()
        );
        final MkBranches branches = (MkBranches) repo.branches();
        // @checkstyle MultipleStringLiterals (1 line)
        branches.create("head", "sha");
        // @checkstyle MultipleStringLiterals (1 line)
        branches.create("base", "sha");
        github.relogin(AgentsITCase.ARCHITECT)
            .repos().get(repo.coordinates()).issues()
            // @checkstyle MultipleStringLiterals (1 line)
            .get(issue.number()).comments().post("@rultor merge");
        final Talk talk = this.talk(issue);
        final Profile profile = AgentsITCase.profile();
        new Agents(github, new MkSttc()).agent(talk, profile).execute(talk);
        MatcherAssert.assertThat(
            issue.comments().iterate(),
            Matchers.<Comment>iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                String.format(
                    AgentsITCase.PHRASES.getString("QnMerge.start"),
                    "http://www.rultor.com/t/1-1"
                )
            )
        );
    }

    /**
     * Rultor should not ask architect to confirm merge on a closed PR.
     * (see #917)
     * @throws Exception In case of error.
     */
    @Test
    public void processesMergeClosedPullRequest() throws Exception {
        final AgentsITCase.MockGithub github =
            new AgentsITCase.MockGithub(new MkGithub("rultor"));
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().get(
            repo.pulls().create("title", "head", "base").number()
        );
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "sha");
        branches.create("base", "sha");
        github.relogin(AgentsITCase.USER).repos()
            .get(repo.coordinates()).issues()
            .get(issue.number()).comments().post("@rultor merge");
        new Issue.Smart(issue).close();
        final Talk talk = this.talk(issue);
        final Profile profile = AgentsITCase.profile();
        new Agents(github, new MkSttc()).agent(talk, profile).execute(talk);
        MatcherAssert.assertThat(
            issue.comments().iterate(),
            Matchers.<Comment>iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                AgentsITCase.PHRASES
                    .getString("QnIfPull.already-closed")
            )
        );
    }

    /**
     * Make profile.
     * @return Profile
     * @throws IOException If fails
     */
    private static Profile profile() throws IOException {
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doReturn(
            new XMLDocument(
                String.format(
                    "<p><entry key='architect'><item>%s</item></entry></p>",
                    AgentsITCase.ARCHITECT
                )
            )
        ).when(profile).read();
        Mockito.doReturn(
            new ArrayMap<String, InputStream>().with(
                "file.bin",
                new ByteArrayInputStream(
                    // @checkstyle MagicNumber (1 line)
                    new byte[]{0, 1, 7, 8, 9, 10, 13, 20}
                )
            )
        ).when(profile).assets();
        return profile;
    }

    /**
     * Make talk from issue.
     * @param issue The issue
     * @return Talk
     * @throws IOException If fails
     */
    private Talk talk(final Issue issue) throws IOException {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);
        final SSHD sshd = new SSHD(this.temp.newFolder());
        final int port = sshd.port();
        final String executor;
        if (SystemUtils.IS_OS_LINUX) {
            executor = "md5sum";
        } else {
            executor = "md5";
        }
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                // @checkstyle MultipleStringLiterals (1 line)
                .add("shell").attr("id", "abcdef")
                .add("host").set("localhost").up()
                .add("port").set(Integer.toString(port)).up()
                .add("login").set(sshd.login()).up()
                .add("key").set(sshd.key()).up().up()
                .add("daemon").attr("id", "fedcba")
                .add("title").set("some operation").up()
                .add("script")
                .set(
                    String.format("ls -al; %s file.bin; sleep 50000", executor)
                )
                .up()
                .up()
                .attr("later", "true")
                .add("wire")
                .add("href").set("http://test2").up()
                .add("github-repo").set(issue.repo().coordinates().toString())
                .up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
        );
        return talk;
    }

    /**
     * Mock github.
     */
    private static final class MockGithub implements Github {

        /**
         * Origin github.
         */
        private final transient MkGithub origin;

        /**
         * Ctor.
         * @param github Origin github
         */
        MockGithub(final MkGithub github) {
            this.origin = github;
        }

        @Override
        public Request entry() {
            return new FakeRequest()
                .withBody("{}")
                .withStatus(HttpURLConnection.HTTP_NO_CONTENT);
        }

        @Override
        public Repos repos() {
            return new AgentsITCase.MockRepos(this.origin.repos());
        }

        @Override
        public Gists gists() {
            return this.origin.gists();
        }

        @Override
        public Users users() {
            return this.origin.users();
        }

        @Override
        public Organizations organizations() {
            return this.origin.organizations();
        }

        @Override
        public Markdown markdown() {
            return this.origin.markdown();
        }

        @Override
        public Limits limits() {
            return this.origin.limits();
        }

        @Override
        public Search search() {
            return this.origin.search();
        }

        @Override
        public Gitignores gitignores() throws IOException {
            return this.origin.gitignores();
        }

        @Override
        public JsonObject meta() throws IOException {
            return this.origin.meta();
        }

        @Override
        public JsonObject emojis() throws IOException {
            return this.origin.emojis();
        }

        /**
         * Create repo with random name.
         * @return Repo
         * @throws IOException If fails
         */
        private Repo randomRepo() throws IOException {
            return new MockRepo(this.origin.randomRepo());
        }

        /**
         * Relogin.
         * @param login User to login
         * @return Github
         * @throws IOException If there is any I/O problem
         */
        private Github relogin(final String login) throws IOException {
            return new AgentsITCase.MockGithub(
                (MkGithub) this.origin.relogin(login)
            );
        }
    }

    /**
     * Mock repo.
     */
    private static final class MockRepo implements Repo {

        /**
         * Origin repo.
         */
        private final transient Repo origin;

        /**
         * Ctor.
         * @param repo Origin repo
         */
        MockRepo(final Repo repo) {
            this.origin = repo;
        }

        @Override
        public Github github() {
            return new AgentsITCase.MockGithub((MkGithub) this.origin.github());
        }

        @Override
        public Coordinates coordinates() {
            return this.origin.coordinates();
        }

        @Override
        public Issues issues() {
            return new MockIssues(this.origin.issues());
        }

        @Override
        public Milestones milestones() {
            return this.origin.milestones();
        }

        @Override
        public Pulls pulls() {
            return this.origin.pulls();
        }

        @Override
        public Hooks hooks() {
            return this.origin.hooks();
        }

        @Override
        public IssueEvents issueEvents() {
            return this.origin.issueEvents();
        }

        @Override
        public Labels labels() {
            return this.origin.labels();
        }

        @Override
        public Assignees assignees() {
            return this.origin.assignees();
        }

        @Override
        public Releases releases() {
            return this.origin.releases();
        }

        @Override
        public DeployKeys keys() {
            return this.origin.keys();
        }

        @Override
        public Forks forks() {
            return this.origin.forks();
        }

        @Override
        public RepoCommits commits() {
            return this.origin.commits();
        }

        @Override
        public Branches branches() {
            return this.origin.branches();
        }

        @Override
        public Contents contents() {
            return this.origin.contents();
        }

        @Override
        public Collaborators collaborators() {
            return this.origin.collaborators();
        }

        @Override
        public Git git() {
            return this.origin.git();
        }

        @Override
        public Stars stars() {
            return this.origin.stars();
        }

        @Override
        public Notifications notifications() {
            return this.origin.notifications();
        }

        @Override
        public Iterable<Language> languages() throws IOException {
            return this.origin.languages();
        }

        @Override
        public int compareTo(final Repo repo) {
            return this.origin.compareTo(repo);
        }

        @Override
        public void patch(final JsonObject json) throws IOException {
            this.origin.patch(json);
        }

        @Override
        public JsonObject json() throws IOException {
            return this.origin.json();
        }
    }

    /**
     * Mock issue.
     */
    private static final class MockIssues implements Issues {

        /**
         * Origin issues.
         */
        private final transient Issues origin;

        /**
         * Ctor.
         * @param issues Origin issues
         */
        MockIssues(final Issues issues) {
            this.origin = issues;
        }

        @Override
        public Repo repo() {
            return new MockRepo(this.origin.repo());
        }

        @Override
        public Issue get(final int number) {
            return new AgentsITCase.MockIssue(this.origin.get(number));
        }

        @Override
        public Issue create(final String title, final String body)
            throws IOException {
            return new AgentsITCase.MockIssue(this.origin.create(title, body));
        }

        @Override
        public Iterable<Issue> iterate(final Map<String, String> params) {
            return this.origin.iterate(params);
        }

        @Override
        public Iterable<Issue> search(final Sort sort,
            final Search.Order direction,
            final EnumMap<Qualifier, String> qualifiers) throws IOException {
            return this.origin.search(sort, direction, qualifiers);
        }
    }

    /**
     * Mock repos.
     */
    private static final class MockRepos implements Repos {

        /**
         * Origin repos.
         */
        private final transient Repos origin;

        /**
         * Ctor.
         * @param repos Origin repos
         */
        MockRepos(final Repos repos) {
            this.origin = repos;
        }

        @Override
        public Repo get(final Coordinates coords) {
            return new AgentsITCase.MockRepo(this.origin.get(coords));
        }

        @Override
        public Github github() {
            return new AgentsITCase.MockGithub((MkGithub) this.origin.github());
        }

        @Override
        public Repo create(final RepoCreate settings) throws IOException {
            return this.origin.create(settings);
        }

        @Override
        public void remove(final Coordinates coords) throws IOException {
            this.origin.remove(coords);
        }

        @Override
        public Iterable<Repo> iterate(final String identifier) {
            return this.origin.iterate(identifier);
        }
    }

    /**
     * Mock comment.
     */
    private static final class MockComment implements Comment {

        /**
         * Origin comment.
         */
        private final transient Comment origin;

        /**
         * Ctor.
         * @param comment Origin comment
         */
        MockComment(final Comment comment) {
            this.origin = comment;
        }

        @Override
        public Issue issue() {
            return new AgentsITCase.MockIssue(this.origin.issue());
        }

        @Override
        public int number() {
            return this.origin.number();
        }

        @Override
        public void remove() throws IOException {
            this.origin.remove();
        }

        @Override
        public int compareTo(final Comment comment) {
            return this.origin.compareTo(comment);
        }

        @Override
        public void patch(final JsonObject json) throws IOException {
            this.origin.patch(json);
        }

        @Override
        public JsonObject json() throws IOException {
            return this.origin.json();
        }
    }

    /**
     * Mock comments.
     */
    private static final class MockComments implements Comments {

        /**
         * Origin comments.
         */
        private final transient Comments origin;

        /**
         * Ctor.
         * @param comments Origin comments
         */
        MockComments(final Comments comments) {
            this.origin = comments;
        }

        @Override
        public Issue issue() {
            return new AgentsITCase.MockIssue(this.origin.issue());
        }

        @Override
        public Comment get(final int number) {
            return new AgentsITCase.MockComment(this.origin.get(number));
        }

        @Override
        public Iterable<Comment> iterate() {
            final List<Comment> comments = Lists.newArrayList();
            for (final Comment comment : this.origin.iterate()) {
                comments.add(new AgentsITCase.MockComment(comment));
            }
            return comments;
        }

        @Override
        public Comment post(final String text) throws IOException {
            return new AgentsITCase.MockComment(this.origin.post(text));
        }
    }

    /**
     * Mock issue.
     */
    private static final class MockIssue implements Issue {

        /**
         * Origin issue.
         */
        private final transient Issue origin;

        /**
         * Ctor.
         * @param issue Origin issue
         */
        MockIssue(final Issue issue) {
            this.origin = issue;
        }

        @Override
        public JsonObject json() throws IOException {
            final JsonObjectBuilder json = Json.createObjectBuilder();
            for (final Map.Entry<String, JsonValue> val
                : this.origin.json().entrySet()) {
                json.add(val.getKey(), val.getValue());
            }
            return json
                .add(
                    "pull_request",
                    Json.createObjectBuilder()
                        .add("html_url", "http://test").build()
                )
                .build();
        }

        @Override
        public Repo repo() {
            return new MockRepo(this.origin.repo());
        }

        @Override
        public int number() {
            return this.origin.number();
        }

        @Override
        public Comments comments() {
            return new MockComments(this.origin.comments());
        }

        @Override
        public IssueLabels labels() {
            return this.origin.labels();
        }

        @Override
        public Iterable<Event> events() throws IOException {
            return this.origin.events();
        }

        @Override
        public boolean exists() throws IOException {
            return this.origin.exists();
        }

        @Override
        public int compareTo(final Issue issue) {
            return this.origin.compareTo(issue);
        }

        @Override
        public void patch(final JsonObject json) throws IOException {
            this.origin.patch(json);
        }
    }
}
