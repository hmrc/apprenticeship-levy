<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes" saxon:indent-spaces="4" xmlns:saxon="http://saxon.sf.net/"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
     <endpoint>
         <xsl:apply-templates select="//endpoint/versions/version"/>
     </endpoint>
  </xsl:template>

  <xsl:template match="version[@level='1.0']">
    <xsl:copy-of select="./name"/>
    <xsl:copy-of select="./description"/>
    <section id="resource">
        <title>Resource</title>
        <resource>GET /apprenticeship-levy<xsl:value-of select="./request/path"/></resource>
    </section>
    <xsl:if test="./request/params/param/type[text()='path']">
      <section id="path-parameters">
          <title>Path Parameters</title>
          <table>
              <headings>
                  <cell>Name</cell>
                  <cell>Type</cell>
                  <cell>Example</cell>
                  <cell>Description</cell>
              </headings>
              <rows>
                <xsl:apply-templates select="./request/params/param" mode="path"/>
              </rows>
          </table>
      </section>
    </xsl:if>
    <xsl:if test="./request/params/param/type[text()='query']">
      <section id="query-parameters">
          <title>Query Parameters</title>
          <table>
              <headings>
                  <cell>Name</cell>
                  <cell>Type</cell>
                  <cell>Default</cell>
                  <cell>Example</cell>
                  <cell>Optional</cell>
                  <cell>Description</cell>
              </headings>
              <rows>
                <xsl:apply-templates select="./request/params/param" mode="query"/>
              </rows>
          </table>
      </section>
    </xsl:if>
    <xsl:apply-templates select="./secured"/>
    <xsl:apply-templates select="./request/headers/header[1]" mode="content-type"/>
    <xsl:apply-templates select="./responses/http[@status='200']"/>
    <xsl:if test="./responses/http[@status!='200']">
      <section id="error-scenarios">
          <title>Error Scenarios</title>
          <table>
              <headings>
                  <cell>Error Scenario</cell>
                  <cell>HTTP Status</cell>
                  <cell>Code</cell>
              </headings>
              <rows>
                <xsl:apply-templates select="./responses/http[@status!='200']"/>
              </rows>
          </table>
      </section>
    </xsl:if>
  </xsl:template>

  <xsl:template match="secured">
    <section id="authorisation">
        <title>Authorisation</title>
        <authorisation>
            <xsl:copy-of select="./type"/>
            <xsl:copy-of select="./scopes/scope[1]"/>
        </authorisation>
    </section>
  </xsl:template>

  <xsl:template match="header" mode="content-type">
    <section id="content-types">
        <title>Accepted Media Types</title>
        <list>
            <item>
                <code><xsl:value-of select="./value"/></code>
            </item>
        </list>
    </section>
  </xsl:template>

  <xsl:template match="http[@status='200']">
    <section id="response">
        <title>Response</title>
        <httpStatus>200 (OK)</httpStatus>
        <xsl:apply-templates select="./body/content-type[@mimeType='application/json']" mode="ok"/>
        <xsl:apply-templates select="./body/description" mode="ok"/>
    </section>
  </xsl:template>

  <xsl:template match="http[@status!='200']">
    <row>
        <cell><xsl:value-of select="./description"/></cell>
        <cell><code><xsl:value-of select="@status"/> (<xsl:value-of select="@msg"/>)</code></cell>
        <cell><code></code></cell>
    </row>
  </xsl:template>

  <xsl:template match="content-type[@mimeType='application/json']" mode="ok">
    <json><xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text><xsl:value-of select="."/><xsl:text disable-output-escaping="yes">]]&gt;</xsl:text></json>
  </xsl:template>

  <xsl:template match="description" mode="ok">
    <table>
      <headings>
        <cell>Name</cell>
        <cell>Type</cell>
        <cell>Example</cell>
        <cell>Description</cell>
      </headings>
      <rows>
        <xsl:apply-templates select="./field" mode="ok"/>
      </rows>
    </table>
  </xsl:template>

  <xsl:template match="field" mode="ok">
    <row>
        <cell><code><xsl:value-of select="name"/></code></cell>
        <cell><code><xsl:value-of select="type"/></code></cell>
        <cell><code><xsl:value-of select="example"/></code></cell>
        <cell><xsl:value-of select="description"/></cell>
    </row>
  </xsl:template>

  <xsl:template match="param[descendant::type ='path']" mode="path">
    <row>
        <cell><code><xsl:value-of select="./name"/></code></cell>
        <cell><code><xsl:value-of select="./dataType"/></code></cell>
        <cell><code><xsl:value-of select="./example"/></code></cell>
        <cell><xsl:value-of select="./description"/></cell>
    </row>
  </xsl:template>

  <xsl:template match="param[descendant::type !='path']" mode="path"/>

  <xsl:template match="param[descendant::type ='query']" mode="query">
    <row>
        <cell><code><xsl:value-of select="./name"/></code></cell>
        <cell><code><xsl:value-of select="./dataType"/></code></cell>
        <cell></cell>
        <cell><code><nowrap><xsl:value-of select="./example"/></nowrap></code></cell>
        <cell>Yes</cell>
        <cell><xsl:value-of select="./description"/></cell>
    </row>
  </xsl:template>

  <xsl:template match="param[descendant::type !='query']" mode="query"/>

  <xsl:template match="text()|@*" />
</xsl:stylesheet>