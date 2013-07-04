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
            <xsl:apply-templates select="/page/unit"/>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <xsl:choose>
            <xsl:when test="/page/pulses/pulse">
                <xsl:if test="/page/since">
                    <div>
                        <ul class="inline">
                            <li>
                                <xsl:text>Since </xsl:text>
                                <xsl:value-of select="/page/since"/>
                            </li>
                            <li>
                                <a title="back to start">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="//links/link[@rel='latest']/@href"/>
                                    </xsl:attribute>
                                    <xsl:text>back to start</xsl:text>
                                </a>
                            </li>
                        </ul>
                    </div>
                </xsl:if>
                <ul class="nav">
                    <xsl:apply-templates select="/page/pulses/pulse"/>
                </ul>
                <xsl:if test="//links/link[@rel='more']">
                    <xsl:text>See </xsl:text>
                    <a title="more">
                        <xsl:attribute name="href">
                            <xsl:value-of select="//links/link[@rel='more']/@href"/>
                        </xsl:attribute>
                        <xsl:text>more</xsl:text>
                    </a>
                    <xsl:text> pulses.</xsl:text>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:text>Drain is empty.</xsl:text>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="pulse">
        <li>
            <ul class="inline btn-group-vertical">
                <li>
                    <xsl:choose>
                        <xsl:when test="stages/stage[@final='true']">
                            <xsl:apply-templates select="stages/stage[@final='true']/result"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <i class="icon-question-sign text-warning"><xsl:comment>unknown</xsl:comment></i>
                        </xsl:otherwise>
                    </xsl:choose>
                </li>
                <li>
                    <a title="see details">
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='see']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="date"/>
                    </a>
                </li>
                <li>
                    <xsl:value-of select="when"/>
                    <xsl:text> ago</xsl:text>
                </li>
                <xsl:if test="links/link[@rel='stop']">
                    <li>
                        <a title="stop">
                            <xsl:attribute name="href">
                                <xsl:value-of select="links/link[@rel='stop']/@href"/>
                            </xsl:attribute>
                            <i class="icon-off text-error"><xsl:comment>off</xsl:comment></i>
                        </a>
                    </li>
                </xsl:if>
                <li>
                    <a title="see full log">
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='stream']/@href"/>
                        </xsl:attribute>
                        <i class="icon-file-alt"><xsl:comment>see full log</xsl:comment></i>
                    </a>
                </li>
            </ul>
            <div>
                <xsl:if test="count(stages/stage) &gt; 0">
                    <table class="table table-condensed">
                        <colgroup>
                            <col style="width: 2em;"/>
                            <col style="width: 5em;"/>
                            <col/>
                        </colgroup>
                        <tbody>
                            <xsl:apply-templates select="stages/stage"/>
                        </tbody>
                    </table>
                </xsl:if>
            </div>
        </li>
    </xsl:template>
    <xsl:template match="stage">
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="result = 'SUCCESS'">
                        <xsl:text>success</xsl:text>
                    </xsl:when>
                    <xsl:when test="result = 'FAILURE'">
                        <xsl:text>error</xsl:text>
                    </xsl:when>
                    <xsl:when test="result = 'RUNNING'">
                        <xsl:text>warning</xsl:text>
                    </xsl:when>
                </xsl:choose>
            </xsl:attribute>
            <td>
                <xsl:apply-templates select="result"/>
            </td>
            <td>
                <xsl:call-template name="millis">
                    <xsl:with-param name="millis" select="msec"/>
                </xsl:call-template>
                <xsl:value-of select="duration"/>
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="links/link[@rel='see']">
                        <a title="see log">
                            <xsl:attribute name="href">
                                <xsl:value-of select="links/link[@rel='log']/@href"/>
                            </xsl:attribute>
                            <xsl:value-of select="output"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="output"/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="result">
        <xsl:choose>
            <xsl:when test=". = 'SUCCESS'">
                <i class="icon-ok-sign text-success"><xsl:comment>success</xsl:comment></i>
            </xsl:when>
            <xsl:when test=". = 'FAILURE'">
                <i class="icon-minus-sign text-error"><xsl:comment>failure</xsl:comment></i>
            </xsl:when>
            <xsl:when test=". = 'RUNNING'">
                <i class="icon-circle-blank text-success"><xsl:comment>success</xsl:comment></i>
            </xsl:when>
            <xsl:when test=". = 'WAITING'">
                <xsl:comment>waiting</xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <i class="icon-question-sign text-error"><xsl:comment>unknown</xsl:comment></i>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
