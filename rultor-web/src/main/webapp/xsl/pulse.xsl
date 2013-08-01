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
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:include href="/xsl/snapshot.xsl"/>
    <xsl:template name="head">
        <title>
            <xsl:value-of select="/page/pulse"/>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <div>
            <ul class="inline">
                <li>
                    <a title="back to drain">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='drain']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="/page/unit"/>
                    </a>
                </li>
                <li>
                    <xsl:value-of select="/page/date"/>
                </li>
                <li class="muted">
                    <xsl:value-of select="/page/when"/>
                </li>
                <li>
                    <a title="see log">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='stream']/@href"/>
                        </xsl:attribute>
                        <i class="icon-tint"><xsl:comment>see full log</xsl:comment></i>
                    </a>
                </li>
            </ul>
        </div>
        <xsl:apply-templates select="/page/snapshot"/>
    </xsl:template>
</xsl:stylesheet>
