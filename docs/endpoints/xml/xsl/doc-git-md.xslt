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

    <xsl:for-each select="$files">
      <xsl:variable name="filename" select="concat(replace(lower-case(normalize-space(//endpoint/versions/version/name)), '\s', '-'), '.bak.md')"/>
      <xsl:variable name="path"><xsl:text>docs/</xsl:text><xsl:value-of select="$filename"/></xsl:variable>
      <xsl:message>Generating <xsl:value-of select="$path"/></xsl:message>
      <xsl:result-document method="text" href="{$path}">
        <xsl:apply-templates select="//endpoint/versions/version"/>
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="version/name">
    <xsl:text># </xsl:text><xsl:value-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="description">
    <xsl:value-of select="."/><xsl:text>

</xsl:text>
  </xsl:template>

  <xsl:template match="details">
    <xsl:value-of select="."/><xsl:text>

</xsl:text>
  </xsl:template>

  <xsl:template match="secured">
    <xsl:text>## Authorisation
OAuth 2.0 Bearer Token with the `</xsl:text><xsl:value-of select="./scopes/scope"/><xsl:text>` scope.

</xsl:text>
  </xsl:template>

  <xsl:template match="request">
    <xsl:text>## API
</xsl:text><xsl:value-of select="./method"/><xsl:text> </xsl:text>https://api.service.hmrc.gov.uk/apprenticeship-levy<xsl:value-of select="./path"/><xsl:text>

### Request
#### Headers
| Name | Value |
| --- | --- |
</xsl:text><xsl:apply-templates select="./headers"/><xsl:text>
</xsl:text>

    <xsl:if test="./params/param/type[text()='path']">
<xsl:text>
#### Path Parameters
| Name | Type | Value | Example |
| --- | --- | --- | --- |
</xsl:text><xsl:apply-templates select="./params/param" mode="path"/><xsl:text>

</xsl:text>
    </xsl:if>

    <xsl:if test="./params/param/type[text()='query']">
<xsl:text>
#### Query Parameters
| Name | Type | Value | Example |
| --- | --- | --- | --- |
</xsl:text><xsl:apply-templates select="." mode="query"/><xsl:text>

</xsl:text>
    </xsl:if>

  </xsl:template>

  <xsl:template match="responses">
    <xsl:text><![CDATA[### Response


#### Successful Response

]]></xsl:text>
    <xsl:for-each-group select="./http" group-by="@status">
      <xsl:sort select="@status" data-type="number" order="ascending"/>
      <xsl:choose>
        <xsl:when test="current-grouping-key() = '200'">
          <xsl:for-each select="current-group()">
            <xsl:apply-templates select="." mode="success"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:for-each-group>
    <xsl:text><![CDATA[

#### Error Responses
<table>
  <thead>
    <tr>
      <td>Code &amp; Name</td>
      <td>Value</td>
      <td>Description</td>
      <td>Example</td>
    </tr>
  </thead>
  <tbody>
]]></xsl:text>
    <xsl:for-each-group select="./http" group-by="@status">
      <xsl:sort select="@status" data-type="number" order="ascending"/>
      <xsl:choose>
        <xsl:when test="current-grouping-key() = '200'"/>
        <xsl:otherwise>
          <xsl:for-each select="current-group()">
            <xsl:text><![CDATA[<tr>]]></xsl:text>
            <xsl:apply-templates select="." mode="non-success"/>
            <xsl:text><![CDATA[</tr>]]></xsl:text>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
    <xsl:text><![CDATA[</table>]]></xsl:text>
  </xsl:template>

  <xsl:template match="http" mode="success">
    <xsl:text><![CDATA[```json
]]></xsl:text>
    <xsl:value-of select="./body/content-type"/>
    <xsl:text><![CDATA[
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
]]></xsl:text>
    <xsl:for-each select="./body/description/field">
      <xsl:text><![CDATA[|]]></xsl:text><xsl:value-of select="name"/>
      <xsl:text><![CDATA[|]]></xsl:text><xsl:value-of select="type"/>
      <xsl:text><![CDATA[|]]></xsl:text><xsl:value-of select="description"/>
      <xsl:text><![CDATA[|`]]></xsl:text><xsl:value-of select="example"/><xsl:text><![CDATA[`|
]]></xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="http" mode="non-success">
    <xsl:text><![CDATA[<td>]]></xsl:text><xsl:value-of select="@status"/><xsl:text> </xsl:text><xsl:value-of select="@msg"/><xsl:text><![CDATA[</td>
    <td>]]></xsl:text><xsl:value-of select="code"/><xsl:text><![CDATA[</td>
    <td>]]></xsl:text><xsl:value-of select="./description"/><xsl:text><![CDATA[</td>
    <td><code>]]></xsl:text><xsl:value-of select="./body/content-type"/><xsl:text><![CDATA[</code></td>]]></xsl:text>
  </xsl:template>

  <xsl:template match="param/type[text()='path']" mode="path">
    <xsl:text>|</xsl:text><xsl:value-of select="../name"/><xsl:text>|</xsl:text><xsl:value-of select="../dataType"/><xsl:text>|</xsl:text>
    <xsl:value-of select="../value"/><xsl:text>|`</xsl:text><xsl:value-of select="../example"/><xsl:text>`|
</xsl:text>
  </xsl:template>

  <xsl:template match="param/type[text()='query']" mode="query">
    <xsl:text>|</xsl:text><xsl:value-of select="../name"/><xsl:text>|</xsl:text><xsl:value-of select="../dataType"/><xsl:text>|</xsl:text>
    <xsl:value-of select="../value"/><xsl:text>|`</xsl:text><xsl:value-of select="../example"/><xsl:text>`|
</xsl:text>
  </xsl:template>

  <xsl:template match="text()|@*" mode="path">
  </xsl:template>

  <xsl:template match="text()|@*" mode="query">
  </xsl:template>

  <xsl:template match="header">
    <xsl:text>|</xsl:text><xsl:value-of select="./name"/><xsl:text>|</xsl:text><xsl:value-of select="./value"/><xsl:text>|
</xsl:text>
  </xsl:template>

  <xsl:template match="text()|@*" />
</xsl:stylesheet>