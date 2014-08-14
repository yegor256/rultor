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
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:include href="./layout.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:value-of select="repo"/>
        </title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <div class="wrapper">
            <header class="header">
                <a href="{links/link[@rel='home']/@href}">
                    <img src="//img.rultor.com/logo.svg" class="logo" alt="logo"/>
                </a>
            </header>
            <div class="main">
                <xsl:apply-templates select="siblings/talk"/>
            </div>
            <footer class="footer">
                <p>
                    <a href="/{links/link[@rel='home']/@href}">rultor.com</a>
                </p>
            </footer>
        </div>
    </xsl:template>
    <xsl:template match="talk">
        <div style="margin-bottom:2em;">
            <a href="{href}">
                <xsl:value-of select="name"/>
            </a>
            <span style="color:gray;margin-left:1em;">
                <xsl:value-of select="timeago"/>
            </span>
            <xsl:if test="archive/log">
                <ul>
                    <xsl:apply-templates select="archive/log"/>
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
