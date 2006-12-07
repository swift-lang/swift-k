<xsl:stylesheet version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
−
	<!--
 now replace all these settings with those specific for use with the fo stylesheet (for pdf output) 
-->
−
	<!--
 just realized both html and fo can share many parameters - need to create common.xsl that gets imported to both so i can single source
 those variables 
-->
<!-- which stylesheet to use? -->
<!--  
<xsl:import href="/usr/share/xml/docbook/stylesheet/nwalsh/fo/docbook.xsl"/>
-->
<xsl:import href="/sw/share/xml/xsl/docbook-xsl/fo/docbook.xsl"/>

<!-- enable extensions -->
<xsl:param name="use.extensions" select="'0'"/>
<xsl:param name="xep.extensions" select="0"/>
−
	<!--
 turn off table column extensions (unless you use xalan or saxon - it's a java thing 
-->
<xsl:param name="tablecolumns.extension" select="'0'"/>
<!-- should output be in draft mode? -->
<xsl:param name="draft.mode" select="'no'"/>
<!-- ALIGNMENT -->
<xsl:param name="alignment">left</xsl:param>
<!-- GRAPHICS -->
−
	<!--
 Use graphics in admonitions? like 'warnings' 'important' 'note' etc COMMON 
-->
<xsl:param name="admon.graphics">1</xsl:param>
<!-- Set path to admonition graphics  COMMON -->
<xsl:param name="admon.graphics.path">/www/www-unix.globus.org/docbook-images/</xsl:param>
−
	<!--
 Set path to docbook graphics (testing)
                                <xsl:param name="admon.graphics.path">file:///Z:/testing/alliance/docbook-images/</xsl:param> 
-->
−
	<!--
 Again, if 1 above, what is the filename extension for admon graphics?
-->
<xsl:param name="admon.graphics.extension" select="'.png'"/>
−
	<!--
 for some reason, xep makes the admon graphics too large, this scales them back down 
-->
<xsl:template match="*" mode="admon.graphic.width">14pt</xsl:template>
−
	<!--
 Set path to callout graphics COMMON
               <xsl:param name="callout.graphics.path">/www/www-unix.globus.org/docbook-images/callouts/</xsl:param> 
-->
−
	<!--
 callouts look fuzzy in print - using the following two parameters to force unicode 
-->
<xsl:param name="callout.graphics" select="'0'"/>
<xsl:param name="callout.unicode" select="1"/>
<!-- NUMBERING -->
−
	<!--
 are parts enumerated?  COMMON 
               <xsl:param name="part.autolabel">1</xsl:param>
-->
<!-- Are chapters automatically enumerated? COMMON-->
<xsl:param name="chapter.autolabel">1</xsl:param>
<!-- Are sections enumerated? COMMON -->
<xsl:param name="section.autolabel">1</xsl:param>
−
	<!--
 how deep should each toc be? (how many levels?) COMMON 
-->
<xsl:param name="toc.max.depth">2</xsl:param>
−
	<!--
 How deep should recursive sections appear in the TOC? COMMON 
-->
<xsl:param name="toc.section.depth">1</xsl:param>
<!-- LINKS -->
<!-- display ulinks as footnotes at bottom of page? -->
<xsl:param name="ulink.footnotes" select="1"/>
<!-- display xref links with underline? -->
−
	<xsl:attribute-set name="xref.properties">
<xsl:attribute name="text-decoration">underline</xsl:attribute>
</xsl:attribute-set>
<!-- TABLES -->
<xsl:param name="default.table.width" select="'6in'"/>
<!-- INDEX  -->
<!-- do you want an index? COMMON -->
<xsl:param name="generate.index">1</xsl:param>
<!-- index attributes for xep -->
−
	<xsl:attribute-set name="xep.index.item.properties">
<xsl:attribute name="merge-subsequent-page-numbers">true</xsl:attribute>
<xsl:attribute name="link-back">true</xsl:attribute>
</xsl:attribute-set>
<!-- GLOSSARY  -->
<!-- Display glossentry acronyms? COMMON> -->
<xsl:param name="glossentry.show.acronym">yes</xsl:param>
<!-- Name of the glossary collection file COMMON -->
−
	<xsl:param name="glossary.collection">
/www/www-unix.globus.org/toolkit/docs/development/4.2-drafts/glossary.xml
</xsl:param>
−
	<!--
 Generate links from glossterm to glossentry automatically?  COMMON
-->
<xsl:param name="glossterm.auto.link">1</xsl:param>
−
	<!--
 if non-zero value for previous parameter, does automatic glossterm linking only apply to firstterms? COMMON
                <xsl:param name="firstterm.only.link">1</xsl:param>
-->
<!-- reduce 'indentation' of body text -->
−
	<xsl:param name="body.start.indent">
−
	<xsl:choose>
<xsl:when test="$fop.extensions != 0">0pt</xsl:when>
<xsl:when test="$passivetex.extensions != 0">0pt</xsl:when>
<xsl:otherwise>0pc</xsl:otherwise>
</xsl:choose>
</xsl:param>
</xsl:stylesheet>