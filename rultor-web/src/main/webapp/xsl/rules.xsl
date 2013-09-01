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
            <xsl:text>rules</xsl:text>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <form method="post" class="form-inline spacious">
            <xsl:attribute name="action">
                <xsl:value-of select="/page/links/link[@rel='create']/@href"/>
            </xsl:attribute>
            <fieldset>
                <div class="row">
                    <div class="col-12 col-sm-6 col-lg-4">
                        <div class="input-group">
                            <input name="name" type="text" class="form-control" />
                            <span class="input-group-btn">
                                <button type="submit" class="btn btn-primary">
                                    <xsl:text>Create</xsl:text>
                                </button>
                            </span>
                        </div>
                    </div>
                </div>
            </fieldset>
        </form>
        <xsl:choose>
            <xsl:when test="/page/rules/rule">
                <xsl:apply-templates select="/page/rules/rule"/>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:text>Now create your first rule and configure it as </xsl:text>
                    <a href="//doc.rultor.com/start.html">
                        <xsl:text>this article</xsl:text>
                    </a>
                    <xsl:text> explains.</xsl:text>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="rule">
        <div class="spacious">
            <ul class="list-inline">
                <li>
                    <a title="edit this rule">
                        <xsl:if test="face/exception">
                            <xsl:attribute name="class">
                                <xsl:text>text-danger</xsl:text>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='edit']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="name"/>
                        <xsl:if test="face/arguments">
                            <xsl:text>(</xsl:text>
                            <xsl:for-each select="face/arguments/argument">
                                <xsl:if test="position() &gt; 1">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                                <xsl:text>&quot;</xsl:text>
                                <xsl:value-of select="."/>
                                <xsl:text>&quot;</xsl:text>
                            </xsl:for-each>
                            <xsl:text>)</xsl:text>
                        </xsl:if>
                    </a>
                </li>
                <xsl:if test="face/drainable = 'true'">
                    <li class="icon">
                        <a title="view drain of the rule">
                            <xsl:attribute name="href">
                                <xsl:value-of select="links/link[@rel='drain']/@href"/>
                            </xsl:attribute>
                            <i class="icon-chevron-sign-right"><xsl:comment>drain</xsl:comment></i>
                        </a>
                    </li>
                </xsl:if>
                <li class="icon">
                    <a onclick="return confirm('Are you sure?');"
                        title="delete this rule">
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='remove']/@href"/>
                        </xsl:attribute>
                        <i class="icon-remove"><xsl:comment>remove</xsl:comment></i>
                    </a>
                </li>
                <xsl:if test="face/exception">
                    <li class="icon">
                        <i class="icon-warning-sign text-danger" title="show exception"
                            onclick="$(this).parent().parent().parent().find('.exception').toggle();"><xsl:comment>exception</xsl:comment></i>
                    </li>
                </xsl:if>
            </ul>
            <xsl:if test="face/exception">
                <pre style="display:none;" class="text-danger exception"><xsl:value-of select="face/exception"/></pre>
            </xsl:if>
        </div>
    </xsl:template>
</xsl:stylesheet>
