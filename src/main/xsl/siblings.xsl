<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2021 Yegor Bugayenko
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes" />
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:value-of select="repo"/>
        </title>
        <script type="text/javascript" src="/js/siblings.js?{version/revision}">
            <xsl:text> </xsl:text>
        </script>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <div class="wrapper">
            <header class="header">
                <a href="{links/link[@rel='home']/@href}">
                    <img src="//doc.rultor.com/images/logo.svg" class="logo" alt="logo"/>
                </a>
            </header>
            <div class="main">
                <p>
                    <a href="https://www.rultor.com">Rultor.com</a>
                    <xsl:text> manages basic DevOps operations (merge, release and deploy) for </xsl:text>
                    <strong><xsl:value-of select="repo"/></strong>
                    <xsl:text> project. Want to try Rultor in your own project? </xsl:text>
                    <xsl:text>Read this </xsl:text>
                    <a href="https://doc.rultor.com/basics.html">quick intro</a>
                    <xsl:text>.</xsl:text>
                </p>
                <div id="talks" data-more="{links/link[@rel='more']/@href}">
                    <xsl:apply-templates select="siblings/talk"/>
                </div>
                <div id="tail">
                    <xsl:text>loading...</xsl:text>
                </div>
            </div>
            <footer class="footer">
                <p>
                    <a href="{links/link[@rel='home']/@href}">rultor.com</a>
                </p>
            </footer>
        </div>
    </xsl:template>
    <xsl:template match="talk">
        <div class="talk">
            <a class="entry" href="{href}">
                <xsl:value-of select="name"/>
            </a>
            <span class="timeago">
                <xsl:value-of select="timeago"/>
            </span>
            <xsl:if test="archive/log">
                <ul>
                    <xsl:apply-templates select="archive/log">
                        <xsl:sort select="id" order="ascending" />
                    </xsl:apply-templates>
                </ul>
            </xsl:if>
        </div>
    </xsl:template>
    <xsl:template match="archive/log">
        <li>
            <a href="{href}">
                <xsl:value-of select="id"/>
            </a>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="title"/>
        </li>
    </xsl:template>
</xsl:stylesheet>
