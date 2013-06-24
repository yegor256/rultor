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
package com.rultor.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.urn.URN;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Pulse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Cache}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class CacheTest {

    /**
     * Cache can collect log lines and turn them into stream.
     * @throws Exception If some problem inside
     */
    @Test
    public void collectsLogLinesAndTurnsThemIntoStream() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        final ObjectListing listing = Mockito.mock(ObjectListing.class);
        Mockito.doReturn(new ArrayList<S3ObjectSummary>(0))
            .when(listing).getObjectSummaries();
        Mockito.doReturn(listing).when(aws)
            .listObjects(Mockito.anyString(), Mockito.anyString());
        final S3Client client = Mockito.mock(S3Client.class);
        Mockito.doReturn(aws).when(client).get();
        final Key key = new Key(client, new URN("urn:facebook:5"), "test", 1);
        final Cache cache = new Cache(key);
        cache.append(new Conveyer.Line.Simple(1, Level.INFO, "msg"));
        cache.flush();
        MatcherAssert.assertThat(
            IOUtils.toString(cache.read(), CharEncoding.UTF_8),
            Matchers.endsWith("0:00 INFO msg\n")
        );
        MatcherAssert.assertThat(cache.age(), Matchers.greaterThan(0L));
    }

    /**
     * Cache can save log data to S3.
     * @throws Exception If some problem inside
     */
    @Test
    public void savesLogStreamToAmazon() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        final ObjectListing listing = Mockito.mock(ObjectListing.class);
        Mockito.doReturn(new ArrayList<S3ObjectSummary>(0))
            .when(listing).getObjectSummaries();
        Mockito.doReturn(listing).when(aws)
            .listObjects(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(new PutObjectResult()).when(aws).putObject(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(InputStream.class),
            Mockito.any(ObjectMetadata.class)
        );
        final S3Client client = Mockito.mock(S3Client.class);
        Mockito.doReturn(aws).when(client).get();
        final Key key = new Key(client, new URN("urn:facebook:7"), "tes", 1);
        final Cache cache = new Cache(key);
        cache.append(
            new Conveyer.Line.Simple(
                1,
                Level.INFO,
                new Pulse.Signal(Pulse.Signal.STAGE, "a").toString()
            )
        );
        MatcherAssert.assertThat(cache.age(), Matchers.greaterThan(0L));
        cache.flush();
        Mockito.verify(aws).putObject(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(InputStream.class),
            Mockito.any(ObjectMetadata.class)
        );
    }

}
