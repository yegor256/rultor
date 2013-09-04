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
            <xsl:value-of select="/page/rule/name"/>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <xsl:apply-templates select="/page/face"/>
        <xsl:apply-templates select="/page/rule/exception"/>
        <form method="post" class="spacious">
            <xsl:attribute name="action">
                <xsl:value-of select="/page/links/link[@rel='save']/@href"/>
            </xsl:attribute>
            <fieldset>
                <div class="form-group">
                    <label for="spec" class="hidden-phone">
                        <xsl:text>Specification of </xsl:text>
                        <code><xsl:value-of select="/page/rule/name"/></code>
                    </label>
                    <textarea name="spec" id="spec" rows="18" class="form-control">
                        <xsl:value-of select="/page/rule/spec"/>
                    </textarea>
                </div>
                <div class="form-group">
                    <label><xsl:comment>for the submit button below</xsl:comment></label>
                    <button type="submit" class="btn btn-primary">
                        <xsl:text>Save</xsl:text>
                    </button>
                    <span class="help-block hidden-phone">
                        <xsl:text>Takes up to five minutes to update all servers</xsl:text>
                    </span>
                </div>
            </fieldset>
        </form>
    </xsl:template>
    <xsl:template match="face">
        <xsl:apply-templates select="exception"/>
        <!--
        Doesn't look nice so far, that's why disabling it...
        <xsl:if test="type and html">
            <p class="spacious">
                <code><xsl:value-of select="type"/></code>
                <xsl:text> </xsl:text>
                <xsl:value-of disable-output-escaping="yes" select="html"/>
            </p>
        </xsl:if>
        -->
    </xsl:template>
    <xsl:template match="exception">
        <pre class="text-danger"><xsl:value-of select="."/></pre>
    </xsl:template>
</xsl:stylesheet>
