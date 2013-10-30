<?xml version="1.0"?>
<!--
 * Copyright (c) 2010-2012, REMPL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce
 * the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the REMPL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns="http://www.w3.org/1999/xhtml">
    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="description" content="PDD Summary Report"/>
                <meta name="keywords" content="puzzle driven development"/>
                <meta name="author" content="rempl.com"/>
                <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css"/>
                <title><xsl:text>PDD Summary Report</xsl:text></title>
            </head>
            <body style="padding: 2em;">
                <div class="container-fluid">
                    <h1>Puzzle Driven Development (PDD) Summary Report</h1>
                    <table class="table">
                        <colgroup>
                            <col/>
                            <col/>
                            <col/>
                            <col/>
                            <col style="width:7em;"/>
                            <col/>
                        </colgroup>
                        <thead>
                            <tr>
                                <th><xsl:text>ticket</xsl:text></th>
                                <th><xsl:text>body</xsl:text></th>
                                <th><xsl:text>estimate</xsl:text></th>
                                <th><xsl:text>owner</xsl:text></th>
                            </tr>
                        </thead>
                        <tbody>
                            <xsl:apply-templates select="puzzles/puzzle"/>
                        </tbody>
                    </table>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="puzzle">
        <tr>
            <td><xsl:value-of select="ticket"/></td>
            <td>
                <code>
                    <xsl:value-of select="file"/>
                    <xsl:text>:</xsl:text>
                    <xsl:value-of select="lines"/>
                </code>
                <br/>
                <xsl:value-of select="body"/>
            </td>
            <td><xsl:value-of select="estimate"/></td>
            <td><xsl:value-of select="owner"/></td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
