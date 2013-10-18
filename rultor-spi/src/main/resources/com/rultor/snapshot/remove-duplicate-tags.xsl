<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="@*|node()" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="tags">
        <xsl:copy>
            <xsl:call-template name="collectUniqueTags"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template name="collectUniqueTags">
        <xsl:param name="source" select="tag"/>
        <xsl:if test="count($source) &gt; 0">
            <xsl:variable name="first" select="$source[1]/label"/>
            <xsl:variable name="count" select="count($source[label=$first])"/>
            <xsl:choose>
                <xsl:when test="$count &gt; 0">
                    <xsl:apply-templates select="$source[label=$first][$count]"/>
                    <xsl:call-template name="collectUniqueTags">
                        <xsl:with-param name="source" select="$source[not(label=$first)]"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="$source"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>