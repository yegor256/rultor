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
    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="description" content="Programmable Enforcer of a Software Development Process"/>
                <meta name="keywords" content="IPaaS, continuous integration, continuous delivery"/>
                <meta name="author" content="rultor.com"/>
                <link href="//netdna.bootstrapcdn.com/font-awesome/3.1.1/css/font-awesome.css" rel="stylesheet" />
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/main.css?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="icon" type="image/gif">
                    <xsl:attribute name="href">
                        <xsl:text>http://img.rultor.com/favicon.ico?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <xsl:call-template name="head"/>
                <script type="text/javascript"><![CDATA[
                    var _gaq = _gaq || [];
                    _gaq.push(['_setAccount', 'UA-1963507-10']);
                    _gaq.push(['_trackPageview']);
                    (function() {
                        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
                    })();
                ]]></script>
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            </head>
            <body>
                <a href="https://github.com/yegor256/rultor" class="hidden-phone">
                    <img style="position: absolute; top: 0; right: 0; border: 0; width: 100px; height: 100px;"
                        src="https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"
                        alt="Fork me on GitHub" />
                </a>
                <div class="page">
                    <ul class="inline" style="padding-bottom: 4em; padding-top: 2em;">
                        <li class="hidden-phone">
                            <a class="logo">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="/page/links/link[@rel='home']/@href"/>
                                </xsl:attribute>
                                <xsl:text>R</xsl:text>
                            </a>
                        </li>
                        <li class="hidden-phone">
                            <a href="http://blog.rultor.com/">
                                <xsl:text>how it works?</xsl:text>
                            </a>
                        </li>
                        <xsl:apply-templates select="version"/>
                        <xsl:apply-templates select="identity"/>
                    </ul>
                    <xsl:apply-templates select="flash"/>
                    <xsl:choose>
                        <xsl:when test="/page/identity">
                            <xsl:call-template name="content"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="login"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="millis">
        <xsl:param name="millis" as="xs:integer"/>
        <xsl:choose>
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
    <xsl:template match="version">
        <li class="hidden-phone hidden-tablet">
            <xsl:value-of select="name"/>
        </li>
        <li class="hidden-phone hidden-tablet">
            <a title="see commit in Github">
                <xsl:attribute name="href">
                    <xsl:text>https://github.com/yegor256/rultor/commit/</xsl:text>
                    <xsl:value-of select="revision"/>
                </xsl:attribute>
                <i class="icon-github"><xsl:comment>github icon</xsl:comment></i>
            </a>
            <xsl:text> </xsl:text>
            <xsl:value-of select="revision"/>
        </li>
        <li class="hidden-phone hidden-tablet">
            <xsl:call-template name="millis">
                <xsl:with-param name="millis" select="/page/millis"/>
            </xsl:call-template>
        </li>
    </xsl:template>
    <xsl:template match="flash">
        <p>
            <xsl:attribute name="class">
                <xsl:text>alert </xsl:text>
                <xsl:choose>
                    <xsl:when test="level = 'INFO'">
                        <xsl:text>alert-success</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'WARNING'">
                        <xsl:text>alert-info</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'ERROR'">
                        <xsl:text>alert-error</xsl:text>
                    </xsl:when>
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
        <li class="hidden-phone hidden-tablet">
            <code><xsl:value-of select="urn"/></code>
        </li>
        <li class="hidden-phone hidden-tablet">
            <img style="width: 25px; height: 25px;">
                <xsl:attribute name="src">
                    <xsl:value-of select="photo"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="name"/>
                </xsl:attribute>
            </img>
        </li>
        <li>
            <xsl:value-of select="name"/>
        </li>
        <li class="hidden-phone">
            <i>
                <xsl:attribute name="class">
                    <xsl:text>icon-</xsl:text>
                    <xsl:choose>
                        <xsl:when test="starts-with(urn, 'urn:facebook:')">
                            <xsl:text>facebook-sign</xsl:text>
                        </xsl:when>
                        <xsl:when test="starts-with(urn, 'urn:github:')">
                            <xsl:text>github-sign</xsl:text>
                        </xsl:when>
                        <xsl:when test="starts-with(urn, 'urn:google:')">
                            <xsl:text>google-plus-sign</xsl:text>
                        </xsl:when>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:comment>authenticated</xsl:comment>
            </i>
        </li>
        <li>
            <a title="log out">
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='auth-logout']/@href"/>
                </xsl:attribute>
                <i class="icon-signout"><xsl:comment>signout icon</xsl:comment></i>
            </a>
        </li>
    </xsl:template>
    <xsl:template name="login">
        <p>
            <xsl:text>To start, login using one of your accounts at: </xsl:text>
        </p>
        <ul class="inline btn-group">
            <li>
                <a class="btn">
                    <xsl:attribute name="href">
                        <xsl:value-of select="/page/links/link[@rel='auth-facebook']/@href"/>
                    </xsl:attribute>
                    <i class="icon-facebook-sign"><xsl:comment>facebook sign</xsl:comment></i>
                    <span class="hidden-phone"><xsl:text> Facebook</xsl:text></span>
                </a>
            </li>
            <li>
                <a class="btn">
                    <xsl:attribute name="href">
                        <xsl:value-of select="/page/links/link[@rel='auth-google']/@href"/>
                    </xsl:attribute>
                    <i class="icon-google-plus-sign"><xsl:comment>google plus sign</xsl:comment></i>
                    <span class="hidden-phone"><xsl:text> Google</xsl:text></span>
                </a>
            </li>
            <li>
                <a class="btn">
                    <xsl:attribute name="href">
                        <xsl:value-of select="/page/links/link[@rel='auth-github']/@href"/>
                    </xsl:attribute>
                    <i class="icon-github-sign"><xsl:comment>github sign</xsl:comment></i>
                    <span class="hidden-phone"><xsl:text> Github</xsl:text></span>
                </a>
            </li>
        </ul>
    </xsl:template>
</xsl:stylesheet>
