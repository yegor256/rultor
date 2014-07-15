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
    xmlns="http://www.w3.org/1999/xhtml" version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:include href="./layout.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:text>rultor</xsl:text>
        </title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <div class="wrapper" style="text-align: center; height: 20em;
            position: absolute;
            top: 0; bottom: 0; left: 0; right: 0;
            margin: auto;">
            <p>
                <img style="width:128px;height:128px;" alt="rultor logo">
                    <xsl:attribute name="src">
                        <xsl:text>//img.rultor.com/logo</xsl:text>
                        <xsl:if test="toggles/read-only='true'">
                            <xsl:text>-stripes</xsl:text>
                        </xsl:if>
                        <xsl:text>.svg</xsl:text>
                    </xsl:attribute>
                </img>
            </p>
            <p>
                <xsl:text>Rultor helps coding teams automate </xsl:text>
                <strong><xsl:text>merge</xsl:text></strong>
                <xsl:text>, </xsl:text>
                <strong><xsl:text>deploy</xsl:text></strong>
                <xsl:text> and </xsl:text>
                <strong><xsl:text>release</xsl:text></strong>
                <xsl:text> operations.</xsl:text>
                <xsl:text> Say </xsl:text>
                <code>@rultor hello</code>
                <xsl:text> in a Github issue and start from there.</xsl:text>
            </p>
            <p>
                <xsl:text>For full documentation, look </xsl:text>
                <a href="http://doc.rultor.com">
                    <xsl:text>here</xsl:text>
                </a>
                <xsl:text>.</xsl:text>
            </p>
            <div class="badges" style="margin-top:2em;">
                <div>
                    <span>made by</span>
                    <br/>
                    <a href="http://www.teamed.io">
                        <img src="http://img.teamed.io/logo-light.svg" style="width:96px" alt="teamed.io logo"/>
                    </a>
                </div>
                <div>
                    <span>powered by</span>
                    <br/>
                    <a href="http://www.docker.io">
                        <img src="http://img.rultor.com/docker-logo.png" style="width:96px" alt="docker logo"/>
                    </a>
                </div>
                <div>
                    <span>operates at</span>
                    <br/>
                    <a href="http://www.github.com">
                        <img src="http://img.rultor.com/github-logo.png" style="width:96px" alt="github logo"/>
                    </a>
                </div>
                <div>
                    <span>hosted by</span>
                    <br/>
                    <a href="http://www.cloudbees.net">
                        <img src="http://img.rultor.com/cloudbees-logo.png" style="width:96px" alt="cloudbees logo"/>
                    </a>
                </div>
            </div>
        </div>
    </xsl:template>
</xsl:stylesheet>
