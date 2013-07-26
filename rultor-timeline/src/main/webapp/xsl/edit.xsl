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
            <xsl:value-of select="/page/timeline/name"/>
        </title>
    </xsl:template>
    <xsl:template name="content">
            <xsl:apply-templates select="/page/timeline"/>
    </xsl:template>
    <xsl:template match="timeline">
        <form method="post">
            <xsl:attribute name="action">
                <xsl:value-of select="/page/links/link[@rel='save']/@href"/>
            </xsl:attribute>
            <fieldset>
                <label for="key">
                    <xsl:text>Authentication key of </xsl:text>
                    <code>
                        <xsl:value-of select="name"/>
                    </code>
                </label>
                <div class="input-append">
                    <input name="key" id="key" type="text" disabled="disabled" class="input-block-level input-large uneditable-input">
                        <xsl:attribute name="value">
                            <xsl:value-of select="key"/>
                        </xsl:attribute>
                    </input>
                    <button class="btn" type="button">
                        <xsl:attribute name="onclick">
                            <xsl:text>document.getElementById('key').value = Math.random().toString(36).substring(2);</xsl:text>
                        </xsl:attribute>
                        <i class="icon-refresh"><xsl:comment>refresh icon</xsl:comment></i>
                    </button>
                </div>
                <label for="friends">
                    <xsl:text>Friends (URN masks, one per line)</xsl:text>
                </label>
                <textarea name="friends" id="friends" rows="6" class="input-block-level">
                    <xsl:choose>
                        <xsl:when test="friends/friend">
                            <xsl:for-each select="friends/friend">
                                <xsl:value-of select="."/>
                                <xsl:text>&#10;</xsl:text>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>enter here...</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </textarea>
                <label><xsl:comment>for the submit button below</xsl:comment></label>
                <button type="submit" class="btn">
                    <xsl:text>Save</xsl:text>
                </button>
            </fieldset>
        </form>
    </xsl:template>
</xsl:stylesheet>
