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
    <xsl:template match="snapshot">
        <xsl:if test="tags/tag">
            <ul class="list-inline">
                <xsl:apply-templates select="tags/tag"/>
                <li class="text-muted">
                    <xsl:text>updated </xsl:text>
                    <span class="timeago"><xsl:value-of select="updated"/></span>
                </li>
            </ul>
        </xsl:if>
        <ul class="list-inline">
            <xsl:if test="spec">
                <li>
                    <i class="icon-beaker" style="cursor:pointer;" title="show specification"
                        onclick="$(this).parent().parent().parent().parent().find('pre.spec').toggle();"><xsl:comment>spec</xsl:comment></i>
                </li>
            </xsl:if>
            <xsl:apply-templates select="version" mode="compact"/>
            <xsl:apply-templates select="work"/>
            <xsl:if test="lines">
                <li>
                    <i class="icon-signal"><xsl:comment>lines</xsl:comment></i>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="lines"/>
                </li>
            </xsl:if>
            <xsl:if test="start and not(finish)">
                <li>
                    <i class="icon-flag-alt"><xsl:comment>start</xsl:comment></i>
                    <xsl:text> </xsl:text>
                    <span class="timeago"><xsl:value-of select="start"/></span>
                </li>
            </xsl:if>
            <xsl:if test="eta">
                <li>
                    <i class="icon-suitcase"><xsl:comment>eta</xsl:comment></i>
                    <xsl:text> </xsl:text>
                    <span class="timeago"><xsl:value-of select="eta"/></span>
                </li>
            </xsl:if>
            <xsl:if test="finish">
                <li>
                    <i class="icon-flag-checkered"><xsl:comment>finish</xsl:comment></i>
                    <xsl:text> </xsl:text>
                    <span class="timeago"><xsl:value-of select="finish"/></span>
                </li>
            </xsl:if>
            <xsl:if test="duration">
                <li>
                    <xsl:call-template name="millis">
                        <xsl:with-param name="millis" select="duration"/>
                    </xsl:call-template>
                </li>
            </xsl:if>
        </ul>
        <xsl:if test="spec">
            <pre style="display:none;" class="spec"><xsl:value-of select="spec"/></pre>
        </xsl:if>
        <xsl:if test="products/product">
            <ul class="list-inline">
                <xsl:apply-templates select="products/product"/>
            </ul>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="stdout">
                <div class="progress progress-striped active">
                    <small>
                        <a style="margin-right: .5em;" class="pull-right text-danger" title="stop execution immediately">
                            <xsl:attribute name="href">
                                <xsl:value-of select="stdout"/>
                                <xsl:text>?interrupt</xsl:text>
                            </xsl:attribute>
                            <i class="icon-off"><xsl:comment>off</xsl:comment></i>
                        </a>
                    </small>
                    <a title="click to tail the output">
                        <xsl:attribute name="href">
                            <xsl:value-of select="stdout"/>
                        </xsl:attribute>
                        <xsl:call-template name="bar">
                            <xsl:with-param name="style" select="'progress-bar-info'"/>
                            <xsl:with-param name="snapshot" select="."/>
                        </xsl:call-template>
                    </a>
                </div>
            </xsl:when>
            <xsl:when test="eta">
                <div class="progress">
                    <xsl:call-template name="bar">
                        <xsl:with-param name="style" select="'progress-bar-warning'"/>
                        <xsl:with-param name="snapshot" select="."/>
                    </xsl:call-template>
                </div>
            </xsl:when>
        </xsl:choose>
        <xsl:if test="steps/step">
            <xsl:apply-templates select="steps/step"/>
        </xsl:if>
    </xsl:template>
    <xsl:template match="work">
        <li>
            <i class="icon-male">
                <xsl:attribute name="title">
                    <xsl:value-of select="owner"/>
                </xsl:attribute>
                <xsl:comment>signal</xsl:comment>
            </i>
        </li>
        <li>
            <xsl:value-of select="unit"/>
        </li>
    </xsl:template>
    <xsl:template match="version" mode="compact">
        <li>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>https://github.com/rultor/rultor/commit/</xsl:text>
                    <xsl:value-of select="revision"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="revision"/>
                </xsl:attribute>
                <i class="icon-github"><xsl:comment>github</xsl:comment></i>
            </a>
        </li>
    </xsl:template>
    <xsl:template match="step">
        <div>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="level = 'INFO'">
                        <xsl:text>text-success</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'WARNING'">
                        <xsl:text>text-warning</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'SEVERE'">
                        <xsl:text>text-danger</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>text-muted</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="exception">
                    <i class="icon-warning-sign"><xsl:comment>exception</xsl:comment></i>
                </xsl:when>
                <xsl:when test="finish">
                    <i class="icon-check"><xsl:comment>done</xsl:comment></i>
                </xsl:when>
                <xsl:when test="start">
                    <i class="icon-spinner"><xsl:comment>progress</xsl:comment></i>
                </xsl:when>
                <xsl:otherwise>
                    <i class="icon-check-empty"><xsl:comment>waiting</xsl:comment></i>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text> </xsl:text>
            <span class="markdown">
                <xsl:value-of select="summary"/>
            </span>
            <xsl:if test="exception">
                <xsl:text> (</xsl:text>
                <span class="markdown">
                    <xsl:value-of select="exception"/>
                </span>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </div>
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
            <span class="markdown"><xsl:value-of select="html"/></span>
        </li>
    </xsl:template>
    <xsl:template name="bar">
        <xsl:param name="snapshot"/>
        <xsl:param name="style"/>
        <div>
            <xsl:attribute name="class">
                <xsl:text>progress-bar </xsl:text>
                <xsl:value-of select="$style"/>
            </xsl:attribute>
            <xsl:attribute name="style">
                <xsl:text>width:</xsl:text>
                <xsl:choose>
                    <xsl:when test="$snapshot/start and $snapshot/eta">
                        <xsl:text>15</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>50</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>%;</xsl:text>
            </xsl:attribute>
            <!-- this is for W3C compliance -->
            <xsl:text> </xsl:text>
        </div>
    </xsl:template>
</xsl:stylesheet>
