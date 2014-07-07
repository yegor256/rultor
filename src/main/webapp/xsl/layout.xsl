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
                <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css"/>
                <link rel="stylesheet" href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.css"/>
                <link rel="stylesheet" type="text/css" media="all" href="/css/style.css?{version/revision}"/>
                <link rel="icon" type="image/gif" href="//img.rultor.com/favicon.ico?{version/revision}"/>
                <xsl:apply-templates select="." mode="head"/>
            </head>
            <body>
                <xsl:if test="not(identity)">
                    <a href="{links/link[@rel='rexsl:github']/@href}">
                        <xsl:text>login</xsl:text>
                    </a>
                </xsl:if>
                <xsl:apply-templates select="identity"/>
                <xsl:apply-templates select="flash"/>
                <article>
                    <xsl:apply-templates select="." mode="body"/>
                </article>
                <xsl:apply-templates select="version"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="version">
        <aside class="version hidden-xs hidden-sm" style="padding: 0.2em 0.5em;">
            <ul class="list-inline">
                <li>
                    <xsl:attribute name="class">
                        <xsl:if test="contains(name, 'SNAPSHOT')">
                            <xsl:text> text-danger</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:value-of select="name"/>
                </li>
                <li>
                    <a href="https://github.com/rultor/rultor/commit/{revision}"
                        title="{revision}">
                        <i class="icon-github">
                            <xsl:comment>github icon</xsl:comment>
                        </i>
                    </a>
                </li>
                <li>
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="/page/millis &gt; 5000">
                                <xsl:text> text-danger</xsl:text>
                            </xsl:when>
                            <xsl:when test="/page/millis &gt; 1000">
                                <xsl:text> text-warning</xsl:text>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:call-template name="millis">
                        <xsl:with-param name="millis" select="/page/millis"/>
                    </xsl:call-template>
                </li>
            </ul>
        </aside>
    </xsl:template>
    <xsl:template match="flash">
        <p>
            <xsl:attribute name="class">
                <xsl:text>alert</xsl:text>
                <xsl:choose>
                    <xsl:when test="level = 'INFO'">
                        <xsl:text> alert-success</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'WARNING'">
                        <xsl:text> alert-info</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'SEVERE'">
                        <xsl:text> alert-danger</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text> alert-default</xsl:text>
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
        </p>
    </xsl:template>
    <xsl:template match="identity">
        <img style="width: 25px; height: 25px;" class="img-rounded"
            src="{photo}" alt="{name}"/>
        <xsl:value-of select="name"/>
        <a title="log out" href="{/page/links/link[@rel='rexsl:logout']/@href}">
            <i class="icon-signout">
                <xsl:comment>signout icon</xsl:comment>
            </i>
        </a>
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
