<?xml version="1.0"?>
<!--
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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml" version="2.0" exclude-result-prefixes="xs">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:include href="./snapshot.xsl"/>
    <xsl:template match="pulse[snapshot or error]">
        <div class="panel panel-default">
            <xsl:attribute name="data-fetch-url">
                <xsl:value-of select="links/link[@rel='fetch']/@href"/>
            </xsl:attribute>
            <div class="panel-heading">
                <ul class="list-inline">
                    <li>
                        <xsl:value-of select="coordinates/rule"/>
                    </li>
                    <li>
                        <xsl:value-of select="coordinates/scheduled"/>
                    </li>
                    <li class="heart text-muted icon" title="click to stop fetching">
                        <i class="icon-cloud-download"><xsl:comment>heart</xsl:comment></i>
                    </li>
                    <li class="pull-right icon">
                        <a title="close and stop fetching" class="close">
                            <xsl:attribute name="href">
                                <xsl:value-of select="links/link[@rel='close']/@href"/>
                            </xsl:attribute>
                            <xsl:text>&#215;</xsl:text>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="panel-body snapshot">
                <xsl:if test="error">
                    <pre class="text-danger"><xsl:value-of select="error"/></pre>
                </xsl:if>
                <xsl:apply-templates select="snapshot"/>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="pulse[not(snapshot) and not(error)]">
        <div class="spacious">
            <ul class="list-inline spacious-inline-list">
                <li>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='open']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="coordinates/rule"/>
                    </a>
                </li>
                <xsl:apply-templates select="tags/tag"/>
                <li class="text-muted">
                    <span class="timeago"><xsl:value-of select="coordinates/scheduled"/></span>
                </li>
            </ul>
            <xsl:if test="tags/tag[markdown != '']">
                <ul class="list-unstyled tag-detailed-list">
                    <xsl:apply-templates select="tags/tag[markdown != '']" mode="detailed"/>
                </ul>
            </xsl:if>
        </div>
    </xsl:template>
</xsl:stylesheet>
