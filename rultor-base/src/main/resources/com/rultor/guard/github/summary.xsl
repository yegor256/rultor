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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:r="http://www.rultor.com"
    exclude-result-prefixes="xs">
    <xsl:output method="xml"/>
    <xsl:template match="/snapshot">
        <markdown>
            <xsl:text>Test finished at </xsl:text>
            <xsl:value-of select="finish"/>
            <xsl:text> and took </xsl:text>
            <xsl:value-of select="duration div 1000"/>
            <xsl:text> seconds.</xsl:text>
            <xsl:apply-templates select="products/product[name='stdout']"/>
            <xsl:apply-templates select="version"/>
        </markdown>
    </xsl:template>
    <xsl:template match="/snapshot/products/product[name='stdout']">
        <xsl:text> Full output log is available at </xsl:text>
        <xsl:value-of select="markdown"/>
    </xsl:template>
    <xsl:template match="/snapshot/version">
        <xsl:text> By [rultor.com](http://www.rultor.com) </xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text> at `</xsl:text>
        <xsl:value-of select="revision"/>
        <xsl:text>`.</xsl:text>
    </xsl:template>
</xsl:stylesheet>
