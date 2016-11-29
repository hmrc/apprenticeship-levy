<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>

  <xsl:strip-space elements="*"/>

  <xsl:variable name="prefix"><![CDATA[#%RAML 1.0
---

title: Apprenticeship Levy
version: 1.0
protocols: [ HTTPS ]
baseUri: https://api.service.hmrc.gov.uk/

documentation:
 - title: Overview
   content: !include docs/overview.md
 - title: Versioning
   content: !include http://api-documentation-raml-frontend.service/api-documentation/assets/common/docs/versioning.md
 - title: Errors
   content: !include http://api-documentation-raml-frontend.service/api-documentation/assets/common/docs/errors.md

mediaType: [ application/json, application/hal+json ]

uses:
  sec: http://api-documentation-raml-frontend.service/api-documentation/assets/common/modules/securitySchemes.raml
  headers: http://api-documentation-raml-frontend.service/api-documentation/assets/common/modules/headers.raml
traits:
  successResponse:
    responses:
      200:
        body:
          application/json:
            type: <<responseSchema>>
            examples:
              example-1:
                value: <<successExample>>

annotationTypes:
  config: object
  group:
    type: object
    properties:
      name: string
      description: string
  scope:

/apprenticeship-levy:
]]></xsl:variable>

  <xsl:template name="main">
    <xsl:value-of select="$prefix"/>
    <xsl:variable name="files" select="collection('../../../../docs/endpoints/xml/?select=*.xml;recurse=no')"/>
    <xsl:for-each select="$files">
      <xsl:apply-templates select="//endpoint/versions/version"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="version[@level='1.0']">
    <xsl:variable name="url">
      <xsl:choose>
        <xsl:when test="contains(./request/path,'?')"><xsl:value-of select="substring-before(./request/path, '?')"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="./request/path"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:text>
</xsl:text><xsl:value-of select="$url"/><xsl:text>
</xsl:text>
    <xsl:if test="./request/params/param/type[text()='path']">
      <xsl:text>  uriParameters:
</xsl:text>
      <xsl:apply-templates select="./request/params/param" mode="path"/>
    </xsl:if>
  <xsl:text>  </xsl:text><xsl:value-of select="lower-case(./request/method)"/><xsl:text>:
    displayName: </xsl:text><xsl:value-of select="./name"/><xsl:text>
    description: </xsl:text><xsl:value-of select="./description"/><xsl:text>
    is:
      - headers.acceptHeader
      - successResponse:
          successExample: !include examples/employment-check-example-1.json
          responseSchema: !include schemas/employment-check.json
    (scope): "</xsl:text><xsl:value-of select="./secured/scopes/scope[1]"/><xsl:text>"
    securedBy: [ </xsl:text><xsl:value-of select="./secured/method"/><xsl:text>: { scopes: [ "</xsl:text><xsl:value-of select="./secured/scopes/scope[1]"/><xsl:text>" ] } ]
    responses:
</xsl:text>
    <xsl:if test="./request/params/param/type[text()='query']">
      <xsl:text>    queryParameters:
</xsl:text>
      <xsl:apply-templates select="./request/params/param" mode="query"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="param[descendant::type ='query']" mode="query">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="./dataType = 'Date'"><xsl:text>date-only</xsl:text></xsl:when>
        <xsl:otherwise><xsl:value-of select="lower-case(./dataType)"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:text>    </xsl:text><xsl:value-of select="./name"/><xsl:text>:
</xsl:text>
    <xsl:text>      description: </xsl:text><xsl:value-of select="./value"/><xsl:text>
</xsl:text>
    <xsl:text>      type: </xsl:text><xsl:value-of select="$type"/><xsl:text>
</xsl:text>
    <xsl:text>      example: </xsl:text><xsl:value-of select="./example"/><xsl:text>
</xsl:text>
    <xsl:text>      required: </xsl:text><xsl:value-of select="./required"/><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="param[descendant::type !='query']" mode="query"/>

  <xsl:template match="param[descendant::name !='empref' and descendant::type ='path']" mode="path">
    <xsl:text>    </xsl:text><xsl:value-of select="./name"/><xsl:text>:
      description: </xsl:text><xsl:value-of select="./value"/><xsl:text>
      type: </xsl:text><xsl:value-of select="lower-case(./dataType)"/><xsl:text>
      example: </xsl:text><xsl:value-of select="./example"/><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="param" mode="path"/>

  <xsl:template match="text()|@*" />
</xsl:stylesheet>