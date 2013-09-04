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
            <xsl:text>account</xsl:text>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <xsl:if test="/page/receipts/receipt">
            <form action="https://www.paypal.com/cgi-bin/webscr" method="post" class="form-inline spacious">
                <fieldset>
                    <input type="hidden" name="cmd" value="_s-xclick"/>
                    <input type="hidden" name="hosted_button_id" value="LMPZA6C7KTZPY"/>
                    <input type="hidden" name="on0" value="One-time payment amount"/>
                    <input type="hidden" name="currency_code" value="USD"/>
                    <input type="hidden" name="invoice">
                        <xsl:attribute name="value">
                            <xsl:value-of select="/page/identity/urn"/>
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="/page/@date"/>
                        </xsl:attribute>
                    </input>
                    <div class="row">
                        <div class="col-6 col-sm-4 col-lg-2">
                            <select name="os0" class="form-control">
                                <option value="Small">Small $5.00 USD</option>
                                <option value="Medium">Medium $10.00 USD</option>
                                <option value="Large">Large $25.00 USD</option>
                            </select>
                        </div>
                        <div class="col-6 col-sm-4 col-lg-2">
                            <button type="submit" class="btn btn-primary">
                                <xsl:text>Add funds</xsl:text>
                            </button>
                        </div>
                    </div>
                    <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1"/>
                </fieldset>
            </form>
            <ul class="list-unstyled" style="margin-bottom: 3em;">
                <li>
                    <xsl:text>All payments are made between our customers, we don't charge any commission/margin.</xsl:text>
                </li>
                <li>
                    <xsl:text>At the moment you can fund your account with one-time non-refundable PayPal payments.</xsl:text>
                </li>
                <li>
                    <xsl:text>In the nearest future we'll make possible recurring credit card payments and funds withdrawal (refunds).</xsl:text>
                </li>
                <li>
                    <xsl:text>If your account gets below $5.00 we compensate it (but this may happen only once in every 30 days).</xsl:text>
                </li>
                <li>
                    <xsl:text>Please </xsl:text>
                    <a href="mailto:team@rultor.com">email us</a>
                    <xsl:text>, if any questions.</xsl:text>
                </li>
            </ul>
        </xsl:if>
        <xsl:if test="/page/since &gt; 0">
            <div class="spacious">
                <ul class="list-inline">
                    <li>
                        <xsl:text>Since </xsl:text>
                        <xsl:value-of select="/page/since"/>
                    </li>
                    <li>
                        <a title="back to start">
                            <xsl:attribute name="href">
                                <xsl:value-of select="//links/link[@rel='latest']/@href"/>
                            </xsl:attribute>
                            <xsl:text>back to start</xsl:text>
                        </a>
                    </li>
                </ul>
            </div>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="/page/receipts/receipt">
                <div class="panel panel-default">
                    <div class="panel-body">
                        <table class="table table-striped table-hover table-condensed" style="font-size: 80%;">
                            <thead>
                                <tr>
                                    <xsl:apply-templates select="/page/columns/column"/>
                                </tr>
                            </thead>
                            <tbody>
                                <xsl:apply-templates select="/page/receipts/receipt"/>
                            </tbody>
                        </table>
                    </div>
                </div>
                <xsl:if test="//links/link[@rel='more']">
                    <p>
                        <xsl:text>See </xsl:text>
                        <a title="more">
                            <xsl:attribute name="href">
                                <xsl:value-of select="//links/link[@rel='more']/@href"/>
                            </xsl:attribute>
                            <xsl:text>more</xsl:text>
                        </a>
                        <xsl:text>.</xsl:text>
                    </p>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:text>No receipts to show.</xsl:text>
                </p>
            </xsl:otherwise>
        </xsl:choose>
        <pre><xsl:value-of select="/page/sql"/></pre>
    </xsl:template>
    <xsl:template match="receipt">
        <xsl:variable name="agg" select="not(/page/columns[not(column/@grouped)])"/>
        <xsl:variable name="r" select="."/>
        <tr>
            <xsl:for-each select="cell">
                <xsl:variable name="p" select="position()"/>
                <xsl:variable name="column" select="/page/columns/column[position()=$p]"/>
                <td>
                    <xsl:attribute name="class">
                        <xsl:if test="$agg and not($column/@sum) and not($column/@grouped)">
                            <xsl:text>text-muted</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test=". = /page/identity/urn">
                            <i title="it's you" class="icon-male"><xsl:comment>you</xsl:comment></i>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:for-each>
        </tr>
    </xsl:template>
    <xsl:template match="column">
        <th style="min-width: 8em;">
            <ul class="list-inline">
                <li>
                    <xsl:value-of select="title"/>
                </li>
                <xsl:choose>
                    <xsl:when test="not(@sorted) and (links/link[@rel='asc'] or links/link[@rel='desc'])">
                        <li>
                            <a class="text-muted" title="click to sort in ASC order">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='asc']/@href"/>
                                </xsl:attribute>
                                <i class="icon-sort-by-alphabet"><xsl:comment>asc</xsl:comment></i>
                            </a>
                        </li>
                    </xsl:when>
                    <xsl:when test="@sorted = 'asc'">
                        <li class="icon">
                            <a title="click to sort in DESC order">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='desc']/@href"/>
                                </xsl:attribute>
                                <i class="icon-sort-by-alphabet"><xsl:comment>asc</xsl:comment></i>
                            </a>
                        </li>
                    </xsl:when>
                    <xsl:when test="@sorted = 'desc'">
                        <li class="icon">
                            <a title="click to sort in ASC order">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='asc']/@href"/>
                                </xsl:attribute>
                                <i class="icon-sort-by-alphabet-alt"><xsl:comment>desc</xsl:comment></i>
                            </a>
                        </li>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="not(@grouped) and links/link[@rel='group']">
                        <li class="icon">
                            <a class="text-muted" title="click to group">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='group']/@href"/>
                                </xsl:attribute>
                                <i class="icon-collapse"><xsl:comment>collapse</xsl:comment></i>
                            </a>
                        </li>
                    </xsl:when>
                    <xsl:when test="@grouped">
                        <li class="icon">
                            <i class="icon-expand"><xsl:comment>group</xsl:comment></i>
                        </li>
                    </xsl:when>
                </xsl:choose>
            </ul>
        </th>
    </xsl:template>
</xsl:stylesheet>
