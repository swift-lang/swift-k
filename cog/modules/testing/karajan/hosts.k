host("arbat.mcs.anl.gov"
	//service(provider="gt2", type="execution", url="arbat.mcs.anl.gov:2119")
	//service(provider="gridftp", type="file", url="gsiftp://arbat.mcs.anl.gov:2811")
)
host("wiggum.mcs.anl.gov"
	//service(provider="gt2", type="execution", url="wiggum.mcs.anl.gov:2119")
	service(provider="gsiftp", type="file", url="gsiftp://wiggum.mcs.anl.gov:2811")
)

host("plussed(4.0.1)"
	service(provider="gt2", type="execution", url="plussed.mcs.anl.gov:4010")
	service(provider="gt4.0.0", type="execution", url="plussed.mcs.anl.gov:4012")
	service(provider="gsiftp", type="file", url="gsiftp://plussed.mcs.anl.gov:4011")
)
host("plussed(4.0.2)"
	service(provider="gt2", type="execution", url="plussed.mcs.anl.gov:4020")
	service(provider="gt4.0.0", type="execution", url="plussed.mcs.anl.gov:4022")
	service(provider="gsiftp", type="file", url="gsiftp://plussed.mcs.anl.gov:4021")
)
host("jglobus.lcrc.anl.gov"
	//service(provider="gridftp", type="file", url="gsiftp://jglobus.lcrc.anl.gov:2811")
)
/*
<host name="gruntled.mcs.anl.gov">
	<service provider="gt3.0.2"	type="execution"	url="gruntled.mcs.anl.gov"/>
	<service provider="gridftp"	type="file"			url="gsiftp://gruntled.mcs.anl.gov:2811"/>
</host>
<host name="nmi-build1.cs.wisc.edu">
	<service provider="gt2"		type="execution"	url="nmi-build1.cs.wisc.edu"/>
	<service provider="gt3.2.1"	type="execution"	url="nmi-build1.cs.wisc.edu"/>
	<service provider="gridftp"	type="file"			url="gsiftp://nmi-build1.cs.wisc.edu:2811"/>
</host>
<host name="nmi-build3.cs.wisc.edu">
	<service provider="gt2"		type="execution"	url="nmi-build3.cs.wisc.edu"/>
	<service provider="gt3.2.1"	type="execution"	url="nmi-build3.cs.wisc.edu"/>
	<service provider="gridftp"	type="file"			url="gsiftp://nmi-build3.cs.wisc.edu:2811"/>
</host>
<host name="nmi-sles8-ia64.cs.wisc.edu">
	<service provider="gt2"		type="execution"	url="nmi-sles8-ia64.cs.wisc.edu"/>
	<service provider="gt3.2.1"	type="execution"	url="nmi-sles8-ia64.cs.wisc.edu"/>
	<service provider="gridftp"	type="file"			url="gsiftp://nmi-sles8-ia64.cs.wisc.edu:2811"/>
</host>
<host name="nmi-solaris9.cs.wisc.edu">
	<service provider="gt2"		type="execution"	url="nmi-solaris9.cs.wisc.edu"/>
	<service provider="gt3.2.1"	type="execution"	url="nmi-solaris9.cs.wisc.edu"/>
	<service provider="gridftp"	type="file"			url="gsiftp://nmi-solaris9.cs.wisc.edu:2811"/>
</host>
<host name="nyxtest2.engin.umich.edu">
	<service provider="gt2"		type="execution"	url="nyxtest2.engin.umich.edu"/>
	<service provider="gt3.2.1"	type="execution"	url="nyxtest2.engin.umich.edu"/>
	<service provider="gridftp"	type="file"		url="gsiftp://nyxtest2.engin.umich.edu"/>
</host>
<host name="darya.ucs.indiana.edu">
	<service provider="gt2"		type="execution" 	url="darya.ucs.indiana.edu"/>
	<service provider="gt3.0.2"	type="execution"	url="http://darya.ucs.indiana.edu:8090"/>
	<service provider="gridftp"	type="file"		url="gsiftp://darya.ucs.indiana.edu"/>
</host>*/

