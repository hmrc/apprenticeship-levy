<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" />

  <xsl:strip-space elements="*"/>

  <!-- Set to 'yes' to output intermediate XML document -->
  <xsl:variable name="debug" select="'no'"/>

  <!-- ***************************************************************************************************************************************************** -->
  <!-- Entry point                                                                                                                                           -->
  <!-- ***************************************************************************************************************************************************** -->
  <xsl:template name="main">
    <xsl:variable name="files" select="collection('../../../../docs/endpoints/xml/?select=*.xml;recurse=no')"/>

    <!-- examples -->
    <xsl:for-each select="$files">
      <xsl:variable name="filename" select="concat(replace(lower-case(normalize-space(//endpoint/versions/version/name)), '\s', '-'), '.json')"/>
      <xsl:variable name="path"><xsl:text>public/api/conf/1.0/examples/</xsl:text><xsl:value-of select="$filename"/></xsl:variable>
      <xsl:message>Generating <xsl:value-of select="$path"/></xsl:message>
      <xsl:result-document method="text" href="{$path}">
        <xsl:apply-templates select="//endpoint/versions/version/responses/http"/>
      </xsl:result-document>
    </xsl:for-each>

    <!-- descriptions -->
    <xsl:for-each select="$files">
      <xsl:variable name="filename" select="concat(replace(lower-case(normalize-space(//endpoint/versions/version/name)), '\s', '-'), '.md')"/>
      <xsl:variable name="path"><xsl:text>public/api/conf/1.0/docs/</xsl:text><xsl:value-of select="$filename"/></xsl:variable>
      <xsl:message>Generating <xsl:value-of select="$path"/></xsl:message>
      <xsl:result-document method="text" href="{$path}">
        <xsl:value-of select="//endpoint/versions/version/description"/><xsl:text>

</xsl:text><xsl:value-of select="//endpoint/versions/version/details"/>
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="http[@status = '200']">
    <xsl:value-of select="./body/content-type[@mimeType = 'application/json']"/>
  </xsl:template>

  <xsl:template match="http[@status != '200']"/>
</xsl:stylesheet>