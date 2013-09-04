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
    <xsl:template name="head">
        <title>
            <xsl:text>index</xsl:text>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <p>
            <xsl:text>
                Rultor.com is a Programmable Enforcer of a Software Development Process.
            </xsl:text>
        </p>
        <p>
            <xsl:text>To start, login using one of your accounts: </xsl:text>
        </p>
        <ul class="list-inline">
            <xsl:if test="/page/links/link[@rel='auth-facebook']">
                <li>
                    <a class="btn btn-default">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='auth-facebook']/@href"/>
                        </xsl:attribute>
                        <i class="icon-facebook-sign"><xsl:comment>facebook sign</xsl:comment></i>
                        <span class="hidden-phone"><xsl:text> Facebook</xsl:text></span>
                    </a>
                </li>
            </xsl:if>
            <xsl:if test="/page/links/link[@rel='auth-google']">
                <li>
                    <a class="btn btn-default">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='auth-google']/@href"/>
                        </xsl:attribute>
                        <i class="icon-google-plus-sign"><xsl:comment>google plus sign</xsl:comment></i>
                        <span class="hidden-phone"><xsl:text> Google</xsl:text></span>
                    </a>
                </li>
            </xsl:if>
            <xsl:if test="/page/links/link[@rel='auth-github']">
                <li>
                    <a class="btn btn-default">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='auth-github']/@href"/>
                        </xsl:attribute>
                        <i class="icon-github-sign"><xsl:comment>github sign</xsl:comment></i>
                        <span class="hidden-phone"><xsl:text> Github</xsl:text></span>
                    </a>
                </li>
            </xsl:if>
        </ul>
    </xsl:template>
</xsl:stylesheet>
