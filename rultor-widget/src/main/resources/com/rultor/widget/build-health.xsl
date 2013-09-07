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
        <table class="table table-condensed">
            <thead>
                <tr>
                    <th>St.</th>
                    <th>Rule</th>
                    <th>By</th>
                </tr>
            </thead>
            <tbody>
                <xsl:apply-templates select="builds/build" mode="build-health"/>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="build" mode="build-health">
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="health &gt; 0.8">
                        <xsl:text>success</xsl:text>
                    </xsl:when>
                    <xsl:when test="health &gt; 0.5">
                        <xsl:text>warning</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>danger</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <td>
                <xsl:choose>
                    <xsl:when test="code = 0">
                        <i class="icon-thumbs-up text-success"><xsl:comment>ok</xsl:comment></i>
                    </xsl:when>
                    <xsl:otherwise>
                        <i class="icon-thumbs-down text-danger"><xsl:comment>fail</xsl:comment></i>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td>
                <xsl:value-of select="coordinates/rule"/>
            </td>
            <td>
                <xsl:value-of select="commit/author"/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
