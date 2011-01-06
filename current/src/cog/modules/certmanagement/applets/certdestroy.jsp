
<p>
<center>

<jsp:plugin type="applet" code="org.globus.cog.security.cert.management.CertDestroyApplet.class" codebase="@CODEBASE@" archive="@ARCHIVE@" jreversion="1.4" width="510" height="460" >
<jsp:params>
<jsp:param name="backGroundColor" value="0xEEEEEE"/>
<jsp:param name="emailAddressOfCA" value="@CA_EMAIL_ADDRESS@"/>
</jsp:params>
    <jsp:fallback>
        Plugin tag OBJECT or EMBED not supported by browser.
    </jsp:fallback>
</jsp:plugin>
</center>
</p>