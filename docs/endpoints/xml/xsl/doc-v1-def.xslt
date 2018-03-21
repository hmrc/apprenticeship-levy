<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>

  <xsl:strip-space elements="*"/>

  <xsl:template name="main">
      <xsl:text>{
</xsl:text>
      <xsl:text>  "scopes": [
    {
      "key": "read:apprenticeship-levy",
      "name": "Access Apprenticeship Levy data associated with your payroll schemes.",
      "description": "Access apprenticeship levy data"
    }
  ],
  "api": {
    "name": "Apprenticeship Levy",
    "description": "Levy declarations for employers",
    "context": "apprenticeship-levy",
    "versions": [
      {
        "version": "1.0",
        "status": "STABLE",
        "endpointsEnabled": true,
        "endpoints": [</xsl:text>
    <xsl:variable name="files" select="collection('../../../../docs/endpoints/xml/?select=*.xml;recurse=no')"/>
    <xsl:for-each select="$files">
      <xsl:apply-templates select="//endpoint/versions/version"/>
      <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>
    <xsl:text>
        ]
      }
    ]
  }
</xsl:text>
     <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="version[@level='1.0']">
    <xsl:variable name="url" select="./request/path" />
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="lower-case(./name) = 'get all employers'"><xsl:text>root</xsl:text></xsl:when>
        <xsl:when test="lower-case(./name) = 'get employer details'"><xsl:text>empref</xsl:text></xsl:when>
        <xsl:when test="lower-case(./name) = 'get employer levy declarations'"><xsl:text>levy-declarations</xsl:text></xsl:when>
        <xsl:when test="lower-case(./name) = 'get employment status'"><xsl:text></xsl:text>employment-check</xsl:when>
        <xsl:when test="lower-case(./name) = 'get employer fraction calculations'"><xsl:text>fraction-calculations</xsl:text></xsl:when>
        <xsl:when test="lower-case(./name) = 'get latest fraction calculation date'"><xsl:text>fraction-calculation-date</xsl:text></xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:text>
          {
            "uriPattern": "</xsl:text><xsl:choose><xsl:when test="contains($url,'?')"><xsl:value-of select="substring-before($url, '?')"/></xsl:when><xsl:otherwise><xsl:value-of select="$url"/></xsl:otherwise></xsl:choose><xsl:text>",
            "endpointName": "</xsl:text><xsl:value-of select="$name"/><xsl:text>",
            "method": "</xsl:text><xsl:value-of select="./request/method"/><xsl:text>",
            "authType": "</xsl:text><xsl:value-of select="./secured/type"/><xsl:text>",
            "throttlingTier": "</xsl:text><xsl:value-of select="./throttlingTier"/><xsl:text>",
            "scope": "</xsl:text><xsl:value-of select="./secured/scopes/scope[1]"/><xsl:text>",
            "queryParameters": [</xsl:text>
    <xsl:if test="./request/params/param/type[text()='query']">
      <xsl:apply-templates select="./request/params/param" mode="query"/>
    </xsl:if>
    <xsl:text>]
          }</xsl:text><xsl:if test="count(following-sibling::*) > 0"><xsl:text>,</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="param[descendant::type ='query']" mode="query">
    <xsl:text>
              {
                "name": "</xsl:text><xsl:value-of select="./name"/><xsl:text>",
                "required": </xsl:text><xsl:value-of select="./required"/><xsl:text>
              }</xsl:text><xsl:if test="count(following-sibling::*) > 0"><xsl:text>,</xsl:text></xsl:if><xsl:if test="count(following-sibling::*) = 0"><xsl:text>
            </xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="param[descendant::type !='query']" mode="query"/>

  <xsl:template match="text()|@*" />
</xsl:stylesheet>