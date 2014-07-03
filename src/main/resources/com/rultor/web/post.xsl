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
    version="2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:r="http://www.rultor.com"
    exclude-result-prefixes="xs">
    <xsl:output method="xml" indent="yes"/>
    <xsl:function name="r:epoch" as="xs:integer">
        <xsl:param name="iso" as="xs:string"/>
        <!-- @see http://stackoverflow.com/questions/15345457 -->
        <xsl:value-of select="(xs:dateTime($iso) - xs:dateTime('1970-01-01T00:00:00Z')) div xs:dayTimeDuration('PT1S')" />
    </xsl:function>
    <xsl:function name="r:cap" as="xs:integer">
        <xsl:param name="iso" as="xs:string"/>
        <!-- @see http://stackoverflow.com/questions/15345457 -->
        <xsl:value-of select="(xs:dateTime($iso) - xs:dateTime('1970-01-01T00:00:00Z')) div xs:dayTimeDuration('PT1S')" />
    </xsl:function>
    <xsl:variable name="start">
        <xsl:choose>
            <xsl:when test="/snapshot/start">
                <xsl:value-of select="r:epoch(/snapshot/start)" />
            </xsl:when>
            <xsl:when test="/snapshot/steps/step/start">
                <xsl:value-of select="r:epoch(/snapshot/steps/step[position()=1]/start)" />
            </xsl:when>
            <xsl:when test="/snapshot/steps/step/finish">
                <xsl:value-of select="r:epoch(/snapshot/steps/step[position()=1]/finish)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="r:epoch('1970-01-01T00:00:00Z')" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="finish">
        <xsl:choose>
            <xsl:when test="/snapshot/finish">
                <xsl:value-of select="r:epoch(/snapshot/finish)" />
            </xsl:when>
            <xsl:when test="/snapshot/eta">
                <xsl:value-of select="r:epoch(/snapshot/eta)" />
            </xsl:when>
            <xsl:when test="/snapshot/updated">
                <xsl:value-of select="$start + (r:epoch(/snapshot/updated) - $start) * 1.25" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$start + xs:dayTimeDuration('PT1H') div xs:dayTimeDuration('PT1S')" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="length" select="$finish - $start" />
    <xsl:template match="steps/step/start|steps/step/finish|updated">
        <xsl:variable name="ratio" select="(r:epoch(.) - $start) div $length" />
        <xsl:variable name="at">
            <xsl:choose>
                <xsl:when test="$ratio &gt; 1">
                    <xsl:text>1</xsl:text>
                </xsl:when>
                <xsl:when test="$ratio &lt; 0">
                    <xsl:text>0</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$ratio" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:element name="{local-name()}">
            <xsl:attribute name="at">
                <xsl:value-of select="format-number($at,'0.000')" />
            </xsl:attribute>
            <xsl:if test="$ratio != $at">
                <xsl:attribute name="ratio">
                    <xsl:value-of select="$ratio" />
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
