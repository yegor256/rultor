/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.xembly.Directives;

/**
 * Adds index to all the requests received.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class IndexesRequests implements SuperAgent {
    @Override
    public void execute(final Talks talks) throws IOException {
        if (talks == null) {
            return;
        }
        for (final Talk talk : talks.active()) {
            final List<String> requests = talk.read().xpath("//request");

            if (requests.size() == 0)
            {
                boolean hasLogs = false;
                int indexValue = 0;

                final List<XML> logs = talk.read().nodes("//archive/log");
                System.out.println("logs: " + logs.size());

                if (logs.isEmpty()) {
                    indexValue = 1;
                } else {
                    // extract index from logs
                    int maxIndex = 0;
                    for (XML curLog : logs) {
                        int curIndex = 0;
                        final List<String> indexTexts = curLog.xpath("@index");
                        if (indexTexts.size() == 1) {
                            final String indexText = indexTexts.get(0);
                            try {
                                maxIndex = Integer.parseInt(indexText);
                            } catch (final NumberFormatException exception) {
                                Logger.error(this,
                                    String.format(
                                        "Invalid index number '%s'",
                                        indexText
                                    )
                                );
                            }
                            if (curIndex > maxIndex) {
                                maxIndex = curIndex;
                            }
                        }

                        indexValue = maxIndex + 1;
                    }
                }
                addIndex(talk, indexValue);
            }
        }
    }

    private void addIndex(final Talk talk, final int index) throws IOException {
        talk.modify(
            new Directives().xpath("//talk").add("request")
            .attr("index", Integer.toString(index))
            .attr("id", createRequestId())
        .add("type").set("index")
        .up()
        .add("args"));
    }

    private String createRequestId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
