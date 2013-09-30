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
    <xsl:template match="widget[@class='com.rultor.widget.BuildHistory']">
        <xsl:choose>
            <xsl:when test="not(builds) or builds[count(build) = 0]">
                <div class="panel-body">
                    <xsl:text>No builds found in this stand yet...</xsl:text>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <table class="table table-condensed">
                    <thead>
                        <tr>
                            <th>
                                <i class="icon-flag-alt"><xsl:comment>flag</xsl:comment></i>
                            </th>
                            <th><xsl:text>Rule</xsl:text></th>
                            <th><xsl:text>Commit</xsl:text></th>
                            <th><xsl:text>By</xsl:text></th>
                            <th><xsl:text>When</xsl:text></th>
                            <th><xsl:text>Time</xsl:text></th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:apply-templates select="builds/build" mode="build-history-row"/>
                    </tbody>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="build" mode="build-history-row">
        <tr>
            <td>
                <a class="icon">
                    <xsl:attribute name="href">
                        <xsl:value-of select="/page/links/link[@rel='pulse-open']/@href"/>
                        <xsl:value-of select="coordinates/scheduled"/>
                        <xsl:text>+</xsl:text>
                        <xsl:value-of select="coordinates/rule"/>
                        <xsl:text>+</xsl:text>
                        <xsl:value-of select="coordinates/owner"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="code = 0">
                            <i class="icon-thumbs-up text-success"><xsl:comment>ok</xsl:comment></i>
                        </xsl:when>
                        <xsl:otherwise>
                            <i class="icon-thumbs-down text-danger"><xsl:comment>fail</xsl:comment></i>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </td>
            <td>
                <xsl:value-of select="coordinates/rule"/>
            </td>
            <td>
                <code>
                    <xsl:attribute name="class">
                        <xsl:if test="code != 0">
                            <xsl:text>text-danger</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:value-of select="head"/>
                </code>
            </td>
            <td>
                <xsl:value-of select="author"/>
            </td>
            <td>
                <span class="timeago"><xsl:value-of select="coordinates/scheduled"/></span>
            </td>
            <td>
                <xsl:call-template name="millis">
                    <xsl:with-param name="millis" select="duration"/>
                </xsl:call-template>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
