<?xml version="1.0"?>
<!--
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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="2.0">
    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="description" content="Coding Team Assistant"/>
                <meta name="keywords" content="continuous integration, continuous delivery, revision control"/>
                <meta name="author" content="rultor.com"/>
                <link rel="stylesheet" type="text/css" media="all" href="/css/style.css?{version/revision}"/>
                <link rel="stylesheet" type="text/css" media="all" href="//doc.rultor.com/css/layout.css?{version/revision}"/>
                <link rel="icon" type="image/gif" href="//img.rultor.com/favicon.ico?{version/revision}"/>
                <xsl:apply-templates select="." mode="head"/>
            </head>
            <body>
                <div class="menu">
                    <xsl:if test="not(identity)">
                        <span>
                            <a href="{links/link[@rel='rexsl:github']/@href}">
                                <xsl:text>login</xsl:text>
                            </a>
                        </span>
                    </xsl:if>
                    <xsl:apply-templates select="identity"/>
                    <xsl:apply-templates select="version"/>
                    <xsl:apply-templates select="toggles"/>
                    <xsl:apply-templates select="flash"/>
                </div>
                <xsl:apply-templates select="." mode="body"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="toggles">
        <xsl:variable name="label">
            <xsl:choose>
                <xsl:when test="read-only='true'">
                    <span style="color:red"><xsl:text>ro</xsl:text></span>
                </xsl:when>
                <xsl:otherwise>
                    <span style="color:green"><xsl:text>rw</xsl:text></span>
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
        <span>
            <xsl:value-of select="name"/>
        </span>
        <span>
            <a href="https://github.com/rultor/rultor/commit/{revision}"
                title="{revision}">
                <xsl:value-of select="substring(revision,1,3)"/>
            </a>
        </span>
        <span>
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
        <span>
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
                        <xsl:text>green</xsl:text>
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
        <span>
            <xsl:value-of select="name"/>
        </span>
        <span>
            <a title="log out" href="{/page/links/link[@rel='rexsl:logout']/@href}">
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
                <xsl:value-of select="format-number($millis, '#')"/>
                <xsl:text>ms</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
