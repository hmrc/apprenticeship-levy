<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">

  <xsl:output method="text" />

  <xsl:strip-space elements="*"/>

  <!-- Set to 'yes' to output intermediate XML document -->
  <xsl:variable name="debug" select="'no'"/>

  <!-- ***************************************************************************************************************************************************** -->
  <!-- Entry point                                                                                                                                           -->
  <!-- ***************************************************************************************************************************************************** -->
  <xsl:template name="main">
    <xsl:call-template name="xmlToYaml">
      <xsl:with-param name="doc"><xsl:call-template name="transformXML"/></xsl:with-param>
      <xsl:with-param name="header" select="$prefix"/>
      <xsl:with-param name="footer" select="''"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ***************************************************************************************************************************************************** -->
  <!-- Generate Intermediate XML Document                                                                                                                    -->
  <!-- ***************************************************************************************************************************************************** -->
  <xsl:template name="transformXML">
    <xsl:variable name="files" select="collection('../../../../docs/endpoints/xml/?select=*.xml;recurse=no')"/>
    <xsl:variable name="content">
      <doc>
        <xsl:for-each select="$files">
          <xsl:apply-templates select="//endpoint/versions/version"/>
        </xsl:for-each>
      </doc>
    </xsl:variable>
    <root>
      <xsl:for-each-group select="$content/doc/*" group-adjacent="boolean(self::epayeEndpoint)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <epaye>
              <empref>
                <xsl:copy-of select="current-group()/endpoint"/>
                <employed>
                  <xsl:copy-of select="current-group()/employedEndpoint/endpoint"/>
                </employed>
              </empref>
            </epaye>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="current-group()/endpoint"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </root>
  </xsl:template>

  <xsl:template name="lastPath">
    <xsl:param name="uri"/>
    <xsl:value-of select="tokenize($uri, '/')[last()]"/>
  </xsl:template>

  <xsl:template name="uri">
    <xsl:param name="requestURL"/>
      <xsl:choose>
        <xsl:when test="$requestURL = '/'"><xsl:value-of select="''"/></xsl:when>
        <xsl:when test="contains($requestURL,'?')"><xsl:value-of select="substring-before($requestURL, '?')"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$requestURL"/></xsl:otherwise>
      </xsl:choose>
  </xsl:template>

  <xsl:template name="path">
    <xsl:param name="url"/>
      <xsl:choose>
        <xsl:when test="$url = '{empref}'"><xsl:value-of select="$url"/></xsl:when>
        <xsl:when test="string-length($url) &gt; 0"><xsl:text>/</xsl:text><xsl:value-of select="$url"/></xsl:when>
        <xsl:otherwise />
      </xsl:choose>
  </xsl:template>

  <xsl:template name="params">
    <xsl:param name="endpointNode"/>
    <xsl:param name="url"/>
    <xsl:if test="$endpointNode/request/params/param/type[text()='path'] and $url = '{nino}'">
      <uriParameters>
        <xsl:apply-templates select="$endpointNode/request/params/param" mode="path"/>
      </uriParameters>
    </xsl:if>
  </xsl:template>

  <xsl:template name="queryParams">
    <xsl:param name="endpointNode"/>
    <xsl:if test="$endpointNode/request/params/param/type[text()='query']">
      <queryParameters>
        <xsl:apply-templates select="$endpointNode/request/params/param" mode="query"/>
      </queryParameters>
    </xsl:if>
  </xsl:template>

  <xsl:template name="endpointDetails">
    <xsl:param name="endpointNode"/>
    <method><xsl:value-of select="lower-case($endpointNode/request/method)"/></method>
    <displayName><xsl:value-of select="$endpointNode/name"/></displayName>
    <description><xsl:value-of select="$endpointNode/description"/></description>
    <is>
      <traits>
        <trait>headers.acceptHeader</trait>
        <trait><schemas>employment-check.json</schemas></trait>
        <trait><example>employment-check-example-1.json</example></trait>
      </traits>
    </is>
    <scope><xsl:value-of select="$endpointNode/secured/scopes/scope[1]"/></scope>
    <secured>
      <method><xsl:value-of select="$endpointNode/secured/method"/></method>
      <scope><xsl:value-of select="$endpointNode/secured/scopes/scope[1]"/></scope>
    </secured>
  </xsl:template>

  <xsl:template match="version[@level='1.0']">
    <xsl:variable name="uri"><xsl:call-template name="uri"><xsl:with-param name="requestURL" select="./request/path"/></xsl:call-template></xsl:variable>
    <xsl:variable name="url"><xsl:call-template name="lastPath"><xsl:with-param name="uri" select="$uri"/></xsl:call-template></xsl:variable>
    <xsl:variable name="path"><xsl:call-template name="path"><xsl:with-param name="url" select="$url"/></xsl:call-template></xsl:variable>
    <xsl:choose>
      <xsl:when test="./request/path = '/'">
        <rootEndpoint>
          <endpoint>
            <path>root</path>
            <xsl:call-template name="params"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            <xsl:call-template name="endpointDetails"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            <responses>
              <xsl:apply-templates select="./responses/http"/>
            </responses>
            <xsl:call-template name="queryParams"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
          </endpoint>
        </rootEndpoint>
      </xsl:when>
      <xsl:when test="$url = 'fraction-calculation-date'">
        <rootEndpoint>
          <endpoint>
            <path><xsl:value-of select="$path" /></path>
            <xsl:call-template name="params"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            <xsl:call-template name="endpointDetails"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            <responses>
              <xsl:apply-templates select="./responses/http"/>
            </responses>
            <xsl:call-template name="queryParams"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
          </endpoint>
        </rootEndpoint>
      </xsl:when>
      <xsl:when test="$url = '{nino}' ">
        <epayeEndpoint>
          <employedEndpoint>
            <endpoint>
              <path><xsl:value-of select="$path" /></path>
              <xsl:call-template name="params"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
              <xsl:call-template name="endpointDetails"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
              <responses>
                <xsl:apply-templates select="./responses/http"/>
              </responses>
              <xsl:call-template name="queryParams"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            </endpoint>
          </employedEndpoint>
        </epayeEndpoint>
      </xsl:when>
      <xsl:otherwise>
        <epayeEndpoint>
          <endpoint>
            <path><xsl:value-of select="$path" /></path>
            <xsl:call-template name="params"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            <xsl:call-template name="endpointDetails"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
            <responses>
              <xsl:apply-templates select="./responses/http"/>
            </responses>
            <xsl:call-template name="queryParams"><xsl:with-param name="endpointNode" select="."/></xsl:call-template>
          </endpoint>
        </epayeEndpoint>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="param[descendant::type ='query']" mode="query">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="./dataType = 'Date'"><xsl:text>date-only</xsl:text></xsl:when>
        <xsl:otherwise><xsl:value-of select="lower-case(./dataType)"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="elementName"><xsl:value-of select="./name"/></xsl:variable>
    <xsl:element name="{$elementName}">
      <description><xsl:value-of select="./value"/></description>
      <type><xsl:value-of select="$type"/></type>
      <example><xsl:value-of select="./example"/></example>
      <required><xsl:value-of select="./required"/></required>
    </xsl:element>
  </xsl:template>

  <xsl:template match="http[@status = 200]">
    <response>
      <status><xsl:value-of select="@status"/></status>
      <body>
        <mimeType><xsl:value-of select="./body/content-type/@mimeType"/></mimeType>
        <example><xsl:value-of select="'!include examples/employment-check-example-1.json'"/></example>
      </body>
    </response>
  </xsl:template>

  <xsl:template match="http[@status != 200]">
    <response>
      <status><xsl:value-of select="@status"/></status>
      <body>
        <mimeType><xsl:value-of select="./body/content-type/@mimeType"/></mimeType>
        <example>
          <code><xsl:value-of select="./code"/><xsl:text> </xsl:text><xsl:value-of select="./description"/></code>
        </example>
      </body>
    </response>
  </xsl:template>

  <xsl:template match="param[descendant::type !='query']" mode="query"/>

  <xsl:template match="param[descendant::name !='empref' and descendant::type ='path']" mode="path">
    <variable>
      <name><xsl:value-of select="./name"/></name>
      <description><xsl:value-of select="./value"/></description>
      <type><xsl:value-of select="lower-case(./dataType)"/></type>
      <example><xsl:value-of select="./example"/></example>
    </variable>
  </xsl:template>

  <xsl:template match="param[descendant::name ='empref' and descendant::type ='path']" mode="empref">
    <variable>
      <name><xsl:value-of select="./name"/></name>
      <description><xsl:value-of select="./value"/></description>
      <type><xsl:value-of select="lower-case(./dataType)"/></type>
      <example><xsl:value-of select="./example"/></example>
    </variable>
  </xsl:template>

  <xsl:template match="param" mode="path"/>

  <xsl:template match="text()|@*" />

  <!-- ***************************************************************************************************************************************************** -->
  <!-- Generate RAML                                                                                                                                         -->
  <!-- ***************************************************************************************************************************************************** -->
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

  <xsl:template name="xmlToYaml">
    <xsl:param name="doc"/>
    <xsl:param name="header"/>
    <xsl:param name="footer"/>
    <xsl:value-of select="$header"/>
    <xsl:if test="$debug = 'yes'">
      <xsl:message>
        <xsl:copy-of select="$doc"/>
      </xsl:message>
    </xsl:if>
    <xsl:apply-templates select="$doc/*" mode="xmlToYaml"/>
    <xsl:value-of select="$footer"/>
  </xsl:template>

  <xsl:template match="root/endpoint[descendant::path = 'root']" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="2"/>
      <xsl:with-param name="str">
        <xsl:value-of select="./method"/><xsl:text>:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="epaye" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="2"/>
      <xsl:with-param name="str">
        <xsl:text>/epaye:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="empref" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="4"/>
      <xsl:with-param name="str">
        <xsl:text>/{</xsl:text><xsl:value-of select="local-name()"/><xsl:text>}:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="6"/>
      <xsl:with-param name="str">
        <xsl:text>uriParameters:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="8"/>
      <xsl:with-param name="str">
        <xsl:text>empref:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="10"/>
      <xsl:with-param name="str">
        <xsl:text>description:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="10"/>
      <xsl:with-param name="str">
        <xsl:text>type: string&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="10"/>
      <xsl:with-param name="str">
        <xsl:text>example: 123/AB12345&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="empref/endpoint" mode="xmlToYaml">
    <xsl:choose>
      <xsl:when test="./path = '{empref}'">
        <xsl:call-template name="indent">
          <xsl:with-param name="length" select="6"/>
          <xsl:with-param name="str">
            <xsl:value-of select="./method"/><xsl:text>:&#x0a;</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="indent">
          <xsl:with-param name="length" select="6"/>
          <xsl:with-param name="str">
            <xsl:value-of select="./path"/>
            <xsl:text>:&#x0a;</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="indent">
          <xsl:with-param name="length" select="8"/>
          <xsl:with-param name="str">
            <xsl:value-of select="./method"/><xsl:text>:&#x0a;</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="employed" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="6"/>
      <xsl:with-param name="str"><xsl:text>employed:</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./endpoint" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="employed/endpoint" mode="xmlToYaml">
    <xsl:text>
        /{nino}:
          uriParameters:
            nino:
              description: A valid National Insurance Number (nino) for the individual being checked.
              type: string
              example: XY654321Z
    </xsl:text>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="6"/>
      <xsl:with-param name="str"><xsl:value-of select="./method"/><xsl:text>:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="root/endpoint[descendant::path = '/fraction-calculation-date']" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="2"/>
      <xsl:with-param name="str">
        <xsl:value-of select="./path"/><xsl:text>:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="4"/>
      <xsl:with-param name="str">
        <xsl:value-of select="./method"/><xsl:text>:&#x0a;</xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="displayName|endpoint/description|is|responses|queryParameters|queryParameters/*|queryParameters/*/description|queryParameters/*/type|queryParameters/*/example|queryParameters/*/required" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 1) * 2"/>
      <xsl:with-param name="str">
        <xsl:value-of select="local-name()"/><xsl:text>: </xsl:text>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:choose>
      <xsl:when test="count(./*) = 0"><xsl:value-of select="."/><xsl:text>&#x0a;</xsl:text></xsl:when>
      <xsl:otherwise><xsl:text>&#x0a;</xsl:text><xsl:apply-templates select="./*" mode="xmlToYaml"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="response/body" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 1) * 2"/>
      <xsl:with-param name="str"><xsl:value-of select="local-name()"/><xsl:text>:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
    <xsl:apply-templates select="./*" mode="error"/>
  </xsl:template>

  <xsl:template match="mimeType[ancestor::response/status = '200']" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 1) * 2"/>
      <xsl:with-param name="str"><xsl:value-of select="."/><xsl:text>:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 2) * 2"/>
      <xsl:with-param name="str"><xsl:text>type: !include.... schema</xsl:text><xsl:text>&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 2) * 2"/>
      <xsl:with-param name="str"><xsl:text>example: </xsl:text><xsl:value-of select="../example"/><xsl:text>&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="mimeType[ancestor::response/status != '200']" mode="error">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 1) * 2"/>
      <xsl:with-param name="str"><xsl:value-of select="."/><xsl:text>:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 2) * 2"/>
      <xsl:with-param name="str"><xsl:text>type: !include.... schema</xsl:text><xsl:text>&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 2) * 2"/>
      <xsl:with-param name="str"><xsl:text>example:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 3) * 2"/>
      <xsl:with-param name="str"><xsl:text>value:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 4) * 2"/>
      <xsl:with-param name="str"><xsl:text>displayName: </xsl:text><xsl:value-of select="../example/title"/><xsl:text>&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 4) * 2"/>
      <xsl:with-param name="str"><xsl:text>description: </xsl:text><xsl:value-of select="../example/code"/><xsl:text>&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 4) * 2"/>
      <xsl:with-param name="str"><xsl:text>value: </xsl:text><xsl:value-of select="../example/json"/><xsl:text>&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="scope" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 1) * 2"/>
      <xsl:with-param name="str"><xsl:text>(scope): "</xsl:text><xsl:value-of select="."/><xsl:text>"&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="./*" mode="xmlToYaml"/>
  </xsl:template>

  <xsl:template match="secured" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*) + 1) * 2"/>
      <xsl:with-param name="str"><xsl:text>securedBy: [ </xsl:text><xsl:value-of select="./method"/><xsl:text>: { scopes: [ "</xsl:text><xsl:value-of select="./scope"/><xsl:text>" ] } ]&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="status" mode="xmlToYaml">
    <xsl:call-template name="indent">
      <xsl:with-param name="length" select="(count(ancestor::*)) * 2"/>
      <xsl:with-param name="str"><xsl:value-of select="."/><xsl:text>:&#x0a;</xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="text()|@*" mode="xmlToYaml" />

  <xsl:template match="text()|@*" mode="error" />

  <xsl:template name="pad">
    <xsl:param name="length" as="xs:integer"/>
    <xsl:variable name="pad" select="'                                                      '"/>
    <xsl:value-of select="substring($pad, 1, $length)"/>
  </xsl:template>

  <xsl:template name="indent">
    <xsl:param name="length" as="xs:integer"/>
    <xsl:param name="str"/>
    <xsl:variable name="pad"><xsl:call-template name="pad"><xsl:with-param name="length" select="$length"/></xsl:call-template></xsl:variable>
    <xsl:value-of select="concat($pad, $str)"/>
  </xsl:template>
</xsl:stylesheet>