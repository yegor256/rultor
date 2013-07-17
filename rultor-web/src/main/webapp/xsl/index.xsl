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
        <form method="post" class="form-inline">
            <xsl:attribute name="action">
                <xsl:value-of select="/page/links/link[@rel='create']/@href"/>
            </xsl:attribute>
            <div class="input-append">
                <input name="name" type="text" class="input-xlarge" />
                <button type="submit" class="btn">
                    <xsl:text>Create</xsl:text>
                </button>
            </div>
        </form>
        <xsl:choose>
            <xsl:when test="/page/units/unit">
                <xsl:apply-templates select="/page/units/unit"/>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:text>Now create your first management unit and configure it as </xsl:text>
                    <a href="http://blog.rultor.com">
                        <xsl:text>this article</xsl:text>
                    </a>
                    <xsl:text> explains.</xsl:text>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="unit">
        <div>
            <ul class="inline btn-group-vertical">
                <li>
                    <a title="edit">
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='edit']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="name"/>
                        <xsl:if test="spec/arguments">
                            <xsl:text>(</xsl:text>
                            <xsl:for-each select="spec/arguments/argument">
                                <xsl:if test="position() &gt; 1">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                                <xsl:value-of select="."/>
                            </xsl:for-each>
                            <xsl:text>)</xsl:text>
                        </xsl:if>
                    </a>
                </li>
                <xsl:if test="spec/drainable = 'true'">
                    <li>
                        <a title="drain">
                            <xsl:attribute name="href">
                                <xsl:value-of select="links/link[@rel='drain']/@href"/>
                            </xsl:attribute>
                            <i class="icon-chevron-sign-right"><xsl:comment>drain</xsl:comment></i>
                        </a>
                    </li>
                </xsl:if>
                <li>
                    <a onclick="return confirm('Are you sure?');"
                        title="delete this unit">
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='remove']/@href"/>
                        </xsl:attribute>
                        <i class="icon-remove"><xsl:comment>remove</xsl:comment></i>
                    </a>
                </li>
            </ul>
        </div>
        <xsl:if test="spec/exception or (spec/type and spec/type != 'java.lang.String')">
            <p style="padding-left: 3em;" class="hidden-phone">
                <xsl:choose>
                    <xsl:when test="spec/face">
                        <xsl:value-of select="spec/face" disable-output-escaping="yes"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <code class="text-error"><xsl:value-of select="spec/exception"/></code>
                    </xsl:otherwise>
                </xsl:choose>
            </p>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
