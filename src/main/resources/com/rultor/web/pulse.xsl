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
    xmlns="http://www.w3.org/1999/xhtml" version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:template match="pulse">
        <xsl:variable name="height" select="5"/>
        <xsl:variable name="width" select="3600000"/>
        <svg xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none" version="1.1"
            width="100%" height="100%">
            <xsl:attribute name="viewBox">
                <xsl:value-of select="-$width"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="0"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="$width"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="$height"/>
            </xsl:attribute>
            <defs>
                <style type="text/css">
                    text {
                        font-size: 1.5;
                        font-family: monospace;
                    }
                </style>
            </defs>
            <line x1="{-$width}" y1="{$height}" x2="0" y2="{$height}"
                stroke="lightgray" stroke-width="4px"
                vector-effect="non-scaling-stroke"/>
            <xsl:for-each select="tick">
                <rect height="{@total + 0.5}"
                    x="{@start}" y="{$height - @total - 0.5}" fill="#348C62">
                    <xsl:attribute name="width">
                        <xsl:choose>
                            <xsl:when test="@msec &lt; 5000">5000</xsl:when>
                            <xsl:otherwise><xsl:value-of select="@msec"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </rect>
            </xsl:for-each>
            <xsl:variable name="age" select="-number(tick[last()]/@start) div 1000"/>
            <text x="0" y="0" style="text-anchor:middle;"
                transform="scale(46000,1) translate(-39,1.5)">
                <xsl:choose>
                    <xsl:when test="not($age) or $age &gt; 600">
                        <tspan style="fill:red">
                            <xsl:text>system outage :( click here</xsl:text>
                        </tspan>
                    </xsl:when>
                    <xsl:when test="$age &gt; 240">
                        <tspan style="fill:orange">
                            <xsl:text>temporary out of service</xsl:text>
                        </tspan>
                    </xsl:when>
                    <xsl:otherwise>
                        <tspan style="fill:#348C62">
                            <xsl:text>all systems work fine</xsl:text>
                        </tspan>
                    </xsl:otherwise>
                </xsl:choose>
            </text>
            <text x="0" y="0" style="text-anchor:end;"
                transform="scale(46000,1) translate(0,1.5)">
                <xsl:value-of select="format-number($age,'0')"/>
                <xsl:text> sec</xsl:text>
            </text>
        </svg>
    </xsl:template>
</xsl:stylesheet>
