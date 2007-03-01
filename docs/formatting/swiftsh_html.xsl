<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Which DocBook standard xsl file should we use as the default? -->
	<!-- Well, xsltproc doesn't cache the xsl files fetched over the web, so better use a local copy -->
	<xsl:import href="docbook/html/chunk.xsl"/>
	<!--<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/html/chunk.xsl"/>-->
	<!--
	
		testing: if you want to generate your own html without installing
		stylesheets, substitute the following url for the import href above:                
		http://docbook.sourceforge.net/release/xsl/current/html/chunk.xsl
                
	-->
	<!-- speed up the chunking process? -->
	<xsl:param name="chunk.fast">1</xsl:param>
	
	<!--
		Use graphics in admonitions? like 'warnings' 'important' 'note' etc 
	-->
	<xsl:param name="admon.graphics">1</xsl:param>
	<!-- Set path to admonition graphics  -->
	<xsl:param name="admon.graphics.path">/docbook-images/</xsl:param>
	<!--
	
		Set path to docbook graphics (testing)
        <xsl:param name="admon.graphics.path">file:///Z:/testing/alliance/docbook-images/</xsl:param> 
	
	-->
	<!--
		Again, if 1 above, what is the filename extension for admon graphics? 
	-->
	<xsl:param name="admon.graphics.extension" select="'.gif'"/>
	<!-- Set path to callout graphics -->

	<xsl:param name="callout.graphics.path">/docbook-images/</xsl:param>
	<!-- Depth to which sections should be chunked -->
	<xsl:param name="chunk.section.depth">0</xsl:param>

	<!--
		
		Are parts automatically enumerated?
		<xsl:param name="part.autolabel">0</xsl:param> 
	
	-->
	<!-- Are chapters automatically enumerated? -->
	<xsl:param name="chapter.autolabel">0</xsl:param>
	<!-- Are sections enumerated? -->
	<xsl:param name="section.autolabel">1</xsl:param>
	<!-- how deep should each toc be? (how many levels?) -->
	<xsl:param name="toc.max.depth">2</xsl:param>
	<!--
		How deep should recursive sections appear in the TOC for chapters? 
	-->
	<xsl:param name="toc.section.depth">4</xsl:param>
	<!--
		Should the first section be chunked separately from its parent? > 0 = yes
	-->
	<xsl:param name="chunk.first.sections">1</xsl:param>
	<!--
	
		Instead of using default filenames, use ids for filenames (dbhtml 	 	 	
		directives take precedence) taking this out to avoid breaking any
		current bookmarks
			
                <xsl:param name="use.id.as.filename">1</xsl:param>
				
	-->
	<!-- custom toc - book only shows chapter -->
	<xsl:template match="preface|chapter|appendix|article" mode="toc">
		<xsl:param name="toc-context" select="."/>
	
		<xsl:choose>
			<xsl:when test="local-name($toc-context) = 'book'">
				<xsl:call-template name="subtoc">
					<xsl:with-param name="toc-context" select="$toc-context"/>
					<xsl:with-param name="nodes" select="foo"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="subtoc">
					<xsl:with-param name="toc-context" select="$toc-context"/>
					<xsl:with-param name="nodes" select="section|sect1|glossary|bibliography|index                                                                                                 |bridgehead[$bridgehead.in.toc != 0]"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- INDEX PARAMETERS -->
	<!-- do you want an index?  -->
	<xsl:param name="generate.index">1</xsl:param>
	<!-- Select indexterms based on type attribute value -->
	<xsl:param name="index.on.type">1</xsl:param>
	<!-- GLOSSARY PARAMETERS -->
	<!-- Display glossentry acronyms? -->
	<xsl:param name="glossentry.show.acronym">yes</xsl:param>

	<!--
	
		Name of the glossary collection file
        
		<xsl:param name="glossary.collection" select="'glossary.xml'"></xsl:param>
          
	-->

	<!--
		Generate links from glossterm to glossentry automatically?
        
		<xsl:param name="glossterm.auto.link">1</xsl:param>
                
	-->

	<!--
		
		if non-zero value for previous parameter, does automatic glossterm
		linking only apply to firstterms?
        
		<xsl:param name="firstterm.only.link">1</xsl:param> 
	-->

	<!--
		
		permit wrapping of long lines of code
		
		<xsl:attribute-set name="monospace.verbatim.properties" 
			use-attribute-sets="verbatim.properties monospace.properties">
            
			<xsl:attribute name="wrap-option">wrap</xsl:attribute>
		</xsl:attribute-set> 
	-->
	
	<!-- INCORPORATING DOCBOOK PAGES INTO WEBSITE -->

	<!--
		make sure there's a DOCTYPE in the html output (otherwise, some css renders strangely 
	-->
	<xsl:param name="chunker.output.doctype-public" select="'-//W3C//DTD HTML 4.01 Transitional//EN'"/>
	<xsl:param name="chunker.output.doctype-system" select="'http://www.w3.org/TR/html4/loose.dtd'"/>
	<!-- add elements to the HEAD tag -->

	<xsl:template name="user.head.content"> 
		<link href="../css/style2.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="http://vds.uchicago.edu/vdl2/dhtml.js"></script>
		<script class="javascript" src="http://vds.uchicago.edu/vdl2/shCoreu.js"></script>
		<script class="javascript" src="http://vds.uchicago.edu/vdl2/shBrushVDL2.js"></script>
	</xsl:template>

	<!-- add an attribute to the BODY tag -->

	<xsl:template name="body.attributes">
		<xsl:attribute name="class">section-3</xsl:attribute>
	</xsl:template>

	<!--
		pull in 'website' with this code by modifying chunk-element-content from html/chunk-common.xsl
	-->

	<xsl:template name="chunk-element-content">
		<xsl:param name="prev"/>
		<xsl:param name="next"/>
		<xsl:param name="nav.context"/>
		<xsl:param name="content">
			<xsl:apply-imports/>
		</xsl:param>
	
		<xsl:call-template name="user.preroot"/>

		<html>

			<xsl:call-template name="html.head">
				<xsl:with-param name="prev" select="$prev"/>
				<xsl:with-param name="next" select="$next"/>
			</xsl:call-template>

			<body onLoad="initjs();sh();">
				<xsl:call-template name="body.attributes"/>

				<xsl:call-template name="user.header.navigation"/>

				<xsl:call-template name="user.header.content"/>
			
				<xsl:copy-of select="$content"/>
			
				<xsl:call-template name="user.footer.content"/>

				<xsl:call-template name="user.footer.navigation"/>

			</body>
		</html>
	</xsl:template>

	<!--
		prevent h1 and h2 using clear: both - want to control in css, instead 
	-->

	<xsl:template name="section.heading">
		<xsl:param name="section" select="."/>
		<xsl:param name="level" select="'1'"/>
		<xsl:param name="title"/>

		<xsl:element name="h{$level+1}">
			<xsl:attribute name="class">title</xsl:attribute>

			<a>
				<xsl:attribute name="name">
					<xsl:call-template name="object.id">
						<xsl:with-param name="object" select="$section"/>
					</xsl:call-template>
				</xsl:attribute>
			</a>
		
			<xsl:copy-of select="$title"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="user.header.content">
		<xsl:text disable-output-escaping="yes"><![CDATA[
		
		<!-- entire page container -->
		<div id="container">
			<!-- header -->
			<div id="header">
				<?php require('../inc/header.php') ?>
			</div>
			<!-- end header -->
			<!-- nav -->
			<div id="nav">
				<?php require('../inc/nav.php') ?>
			</div>
			<!-- end nav -->
			<!-- content container -->
			<div id="content">
				<!-- left side content -->
				<div id="left">
		]]>
		</xsl:text>
	</xsl:template>
	
	<xsl:template name="user.footer.content">
		<xsl:text disable-output-escaping="yes"><![CDATA[
				</div>
				<!-- end left side content -->
				<!-- right side content -->
				<div id="right">
					<?php require('../inc/side_content.php') ?>
				</div>
				<!-- end right side content -->
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		]]>
		</xsl:text>
	</xsl:template>
</xsl:stylesheet>