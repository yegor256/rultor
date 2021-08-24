<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2021 Yegor Bugayenko
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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:r="https://www.rultor.com"
    version="2.0" exclude-result-prefixes="xs r">
    <xsl:output method="text"/>
    <xsl:template match="/talk">
        <xsl:apply-templates select="archive[log]"/>
        <xsl:choose>
            <xsl:when test="request">
                <xsl:apply-templates select="request"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> * no new requests registered&#10;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="daemon[started]">
                <xsl:apply-templates select="daemon"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> * build is not running&#10;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="archive[log]">
        <xsl:text> * </xsl:text>
        <xsl:value-of select="count(log)"/>
        <xsl:text> build(s) archived: </xsl:text>
        <xsl:for-each select="log">
            <xsl:if test="position() &gt; 1">
                <xsl:text>, </xsl:text>
            </xsl:if>
            <xsl:text>[</xsl:text>
            <xsl:value-of select="/talk/@number"/>
            <xsl:text>-</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>](https://www.rultor.com/t/</xsl:text>
            <xsl:value-of select="/talk/@number"/>
            <xsl:text>-</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>)</xsl:text>
        </xsl:for-each>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
    <xsl:template match="request">
        <xsl:text> * request `</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>` is in processing, command is `</xsl:text>
        <xsl:value-of select="type"/>
        <xsl:text>`&#10;</xsl:text>
        <xsl:choose>
            <xsl:when test="args[arg]">
                <xsl:apply-templates select="args[arg]"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> * request has no parameters&#10;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="args[arg]">
        <xsl:text> * request has </xsl:text>
        <xsl:value-of select="count(arg)"/>
        <xsl:text> parameter(s):&#10;</xsl:text>
        <xsl:for-each select="arg">
            <xsl:text>  * `</xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text>`: `</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>`</xsl:text>
            <xsl:text>&#10;</xsl:text>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="daemon[started and dir]">
        <xsl:text> * build started </xsl:text>
        <xsl:value-of select="r:ago(started)"/>
        <xsl:text>&#10;</xsl:text>
        <xsl:if test="dir">
            <xsl:text> * build is running in `</xsl:text>
            <xsl:value-of select="dir"/>
            <xsl:text>` directory&#10;</xsl:text>
        </xsl:if>
        <xsl:if test="ended">
            <xsl:text> * build ended </xsl:text>
            <xsl:value-of select="r:ago(ended)"/>
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:if test="code">
            <xsl:text> * build exit code is `</xsl:text>
            <xsl:value-of select="code"/>
            <xsl:text>`&#10;</xsl:text>
        </xsl:if>
    </xsl:template>
    <xsl:function name="r:ago">
        <xsl:param name="time"/>
        <xsl:variable name="seconds" select="60"/>
        <!--
        <xsl:variable name="seconds"
            select="number((current-dateTime() - xs:dateTime($time)) div xs:dayTimeDuration('PT1S'))"/>
        -->
        <xsl:choose>
            <xsl:when test="$seconds &gt; 60 * 60">
                <xsl:value-of select="format-number($seconds div (60 * 60), '0.0')"/>
                <xsl:text> hours</xsl:text>
            </xsl:when>
            <xsl:when test="$seconds &gt; 60">
                <xsl:value-of select="format-number($seconds div 60, '0.0')"/>
                <xsl:text> minutes</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($seconds, '0')"/>
                <xsl:text> seconds</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text> ago</xsl:text>
    </xsl:function>
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
