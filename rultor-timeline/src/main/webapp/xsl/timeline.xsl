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
            <xsl:value-of select="/page/name"/>
        </title>
        <style><![CDATA[
            .snapshot {
                float: right;
                width: 15em;
            }
            @media (max-width: 30em) {
                .snapshot {
                    float: none;
                    width: 100%;
                }
            }
        ]]></style>
    </xsl:template>
    <xsl:template name="content">
        <h1 class="hidden-phone">
            <xsl:value-of select="/page/name"/>
        </h1>
        <div style="max-width: 50em;">
            <xsl:if test="/page/products/product">
                <aside class="snapshot well span2">
                    <ul class="unstyled">
                        <xsl:apply-templates select="/page/products/product"/>
                    </ul>
                </aside>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="/page/events/event">
                    <ul class="nav spacious">
                        <xsl:apply-templates select="/page/events/event"/>
                    </ul>
                </xsl:when>
                <xsl:otherwise>
                    <p>
                        <xsl:text>No events at the moment, read </xsl:text>
                        <a href="http://blog.rultor.com">
                            <xsl:text>why</xsl:text>
                        </a>
                        <xsl:text>...</xsl:text>
                    </p>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>
    <xsl:template match="event">
        <li>
            <xsl:if test="tags/tag">
                <div>
                    <ul class="inline btn-group-vertical">
                        <xsl:apply-templates select="tags/tag"/>
                    </ul>
                </div>
            </xsl:if>
            <div>
                <xsl:attribute name="title">
                    <xsl:value-of select="time"/>
                </xsl:attribute>
                <xsl:value-of select="when"/>
                <xsl:text> </xsl:text>
                <xsl:value-of disable-output-escaping="yes" select="html"/>
            </div>
            <xsl:if test="products/product">
                <div>
                    <ul class="unstyled" style="padding-left: 2em;">
                        <xsl:apply-templates select="products/product"/>
                    </ul>
                </div>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template match="tag">
        <li>
            <span>
                <xsl:attribute name="class">
                    <xsl:text>label </xsl:text>
                    <xsl:choose>
                        <xsl:when test="level = 'FINE'">
                            <xsl:text>label-success</xsl:text>
                        </xsl:when>
                        <xsl:when test="level = 'INFO'">
                            <xsl:text>label-info</xsl:text>
                        </xsl:when>
                        <xsl:when test="level = 'WARNING'">
                            <xsl:text>label-warning</xsl:text>
                        </xsl:when>
                        <xsl:when test="level = 'SEVERE'">
                            <xsl:text>label-important</xsl:text>
                        </xsl:when>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="label"/>
            </span>
        </li>
    </xsl:template>
    <xsl:template match="product">
        <li>
            <xsl:value-of select="name"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of disable-output-escaping="yes" select="html"/>
        </li>
    </xsl:template>
</xsl:stylesheet>
