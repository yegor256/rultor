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
    <xsl:template match="widget[@class='com.rultor.widget.BuildHealth']">
        <xsl:choose>
            <xsl:when test="not(builds) or builds[count(build) = 0]">
                <div class="panel-body">
                    <span class="pull-left" style="font-size: 3em; margin-right: .2em;">
                        <i class="icon-microphone-off text-muted"><xsl:comment>nothing</xsl:comment></i>
                    </span>
                    <xsl:text>No builds found in this stand yet...</xsl:text>
                </div>
            </xsl:when>
            <xsl:when test="builds[count(build) = 1]">
                <div class="panel-body">
                    <xsl:apply-templates select="builds/build" mode="build-health-single"/>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <table class="table table-condensed">
                    <thead>
                        <tr>
                            <th><xsl:text>H.</xsl:text></th>
                            <th><xsl:text>St.</xsl:text></th>
                            <th><xsl:text>Rule</xsl:text></th>
                            <th><xsl:text>By</xsl:text></th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:apply-templates select="builds/build" mode="build-health-row"/>
                    </tbody>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="build" mode="build-health-single">
        <p>
            <span class="pull-left" style="font-size: 3em; margin-right: .2em;">
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
            </span>
            <xsl:text>Latest commit </xsl:text>
            <code><xsl:value-of select="head"/></code>
            <xsl:text> by </xsl:text>
            <xsl:value-of select="author"/>
            <xsl:text> </xsl:text>
            <xsl:choose>
                <xsl:when test="code = 0">
                    <xsl:text>has been built </xsl:text>
                    <strong><xsl:text>successfully</xsl:text></strong>
                </xsl:when>
                <xsl:otherwise>
                    <strong><xsl:text>failed</xsl:text></strong>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text> </xsl:text>
            <span class="timeago"><xsl:value-of select="coordinates/scheduled"/></span>
            <xsl:text>. </xsl:text>
            <xsl:text>Overall build health is </xsl:text>
            <xsl:choose>
                <xsl:when test="health &gt; 0.8">
                    <span class="text-success"><xsl:text>good</xsl:text></span>
                </xsl:when>
                <xsl:when test="health &gt; 0.5">
                    <span class="text-warning"><xsl:text>average</xsl:text></span>
                </xsl:when>
                <xsl:otherwise>
                    <span class="text-danger"><xsl:text>critical</xsl:text></span>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text> (</xsl:text>
            <xsl:value-of select="format-number(health, '0.00')"/>
            <xsl:text>).</xsl:text>
        </p>
    </xsl:template>
    <xsl:template match="build" mode="build-health-row">
        <tr>
            <td>
                <xsl:choose>
                    <xsl:when test="health &gt; 0.8">
                        <i class="icon-beer text-success"><xsl:comment>ok</xsl:comment></i>
                    </xsl:when>
                    <xsl:when test="health &gt; 0.5">
                        <i class="icon-umbrella text-warning"><xsl:comment>fail</xsl:comment></i>
                    </xsl:when>
                    <xsl:otherwise>
                        <i class="icon-bolt text-danger"><xsl:comment>fail</xsl:comment></i>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
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
                <xsl:value-of select="author"/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
