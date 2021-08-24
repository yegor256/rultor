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
    <xsl:template match="/page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="description" content="DevOps team assistant that helps to automate merge, deploy and release operations, mostly for Github projects"/>
                <meta name="keywords" content="continuous integration, continuous delivery, DevOps"/>
                <meta name="author" content="rultor.com"/>
                <meta property="twitter:account_id" content="4503599630178231"/>
                <link rel="stylesheet" type="text/css" media="all" href="/css/style.css?{version/revision}"/>
                <link rel="stylesheet" type="text/css" media="all" href="//doc.rultor.com/css/layout.css?{version/revision}"/>
                <link rel="icon" type="image/gif" href="//doc.rultor.com/favicon.ico?{version/revision}"/>
                <script type="text/javascript" src="//code.jquery.com/jquery-2.1.1-rc1.min.js">
                    <xsl:text> </xsl:text>
                </script>
                <script type="text/javascript" src="/js/home.js?{version/revision}">
                    <xsl:text> </xsl:text>
                </script>
                <xsl:apply-templates select="." mode="head"/>
                <script>//<![CDATA[
                    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
                    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
                    ga('create', 'UA-1963507-28', 'auto');
                    ga('send', 'pageview');
                //]]></script>
            </head>
            <body>
                <a href="https://github.com/yegor256/rultor">
                    <img src="//doc.rultor.com/images/fork-me.svg" class="fork-me" alt="fork me in github"/>
                </a>
                <section itemscope="" itemtype="http://schema.org/WebApplication">
                    <nav role="navigation" class="menu">
                        <xsl:if test="not(identity)">
                            <span>
                                <a href="{links/link[@rel='takes:github']/@href}" title="login via Github">
                                    <xsl:text>login</xsl:text>
                                </a>
                            </span>
                        </xsl:if>
                        <xsl:apply-templates select="identity"/>
                        <xsl:apply-templates select="version"/>
                        <xsl:apply-templates select="toggles"/>
                        <xsl:apply-templates select="flash"/>
                    </nav>
                    <xsl:apply-templates select="." mode="body"/>
                </section>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="toggles">
        <xsl:variable name="label">
            <xsl:choose>
                <xsl:when test="read-only='true'">
                    <span style="color:red" title="read-only inactive mode">
                        <xsl:text>ro</xsl:text>
                    </span>
                </xsl:when>
                <xsl:otherwise>
                    <span style="color:#348C62" title="read-write, normal operations">
                        <xsl:text>rw</xsl:text>
                    </span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="links/link[@rel='sw:read-only']">
                <a href="{links/link[@rel='sw:read-only']/@href}">
                    <xsl:copy-of select="$label"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$label"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="version">
        <span title="currently deployed version is {name}"
            itemprop="softwareVersion">
            <xsl:value-of select="name"/>
        </span>
        <xsl:if test="revision != 'BUILD'">
            <span>
                <a href="https://github.com/yegor256/rultor/commit/{revision}"
                    title="Github revision deployed is {revision}">
                    <xsl:value-of select="substring(revision,1,3)"/>
                </a>
            </span>
        </xsl:if>
        <span title="server time to build this page">
            <xsl:attribute name="style">
                <xsl:text>color:</xsl:text>
                <xsl:choose>
                    <xsl:when test="/page/millis &gt; 5000">
                        <xsl:text>red</xsl:text>
                    </xsl:when>
                    <xsl:when test="/page/millis &gt; 1000">
                        <xsl:text>orange</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>inherit</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:call-template name="millis">
                <xsl:with-param name="millis" select="/page/millis"/>
            </xsl:call-template>
        </span>
        <span title="server load average">
            <xsl:attribute name="style">
                <xsl:text>color:</xsl:text>
                <xsl:choose>
                    <xsl:when test="/page/@sla &gt; 6">
                        <xsl:text>red</xsl:text>
                    </xsl:when>
                    <xsl:when test="/page/@sla &gt; 3">
                        <xsl:text>orange</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>inherit</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="/page/@sla"/>
        </span>
    </xsl:template>
    <xsl:template match="flash">
        <div>
            <xsl:attribute name="style">
                <xsl:text>color:</xsl:text>
                <xsl:choose>
                    <xsl:when test="level = 'INFO'">
                        <xsl:text>#348C62</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'WARNING'">
                        <xsl:text>orange</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'SEVERE'">
                        <xsl:text>red</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>inherit</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="message"/>
            <xsl:if test="msec &gt; 0">
                <xsl:text> (in </xsl:text>
                <xsl:call-template name="millis">
                    <xsl:with-param name="millis" select="msec"/>
                </xsl:call-template>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </div>
    </xsl:template>
    <xsl:template match="identity">
        <span title="Github account logged in: {urn}">
            <xsl:value-of select="login"/>
        </span>
        <span>
            <a title="log out" href="{/page/links/link[@rel='takes:logout']/@href}">
                <xsl:text>logout</xsl:text>
            </a>
        </span>
    </xsl:template>
    <xsl:template name="millis">
        <xsl:param name="millis"/>
        <xsl:choose>
            <xsl:when test="not($millis)">
                <xsl:text>?</xsl:text>
            </xsl:when>
            <xsl:when test="$millis &gt; 60000">
                <xsl:value-of select="format-number($millis div 60000, '0')"/>
                <xsl:text>min</xsl:text>
            </xsl:when>
            <xsl:when test="$millis &gt; 1000">
                <xsl:value-of select="format-number($millis div 1000, '0.0')"/>
                <xsl:text>s</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($millis, '0')"/>
                <xsl:text>ms</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
