namespace(java) {
	export(classOf, def("org.globus.cog.karajan.compiled.nodes.functions.Java$ClassOf"))
	export(invokeMethod, def("org.globus.cog.karajan.compiled.nodes.JavaMethodInvocationNode"))
    export(new, def("org.globus.cog.karajan.compiled.nodes.NewJavaObjectNode"))
	export(waitForEvent, def("org.globus.cog.karajan.compiled.nodes.EventWait"))
	export(getField, def("org.globus.cog.karajan.compiled.nodes.functions.Java$GetField"))
	export(null, def("org.globus.cog.karajan.compiled.nodes.functions.JavaNull"))
	export(systemProperty, def("org.globus.cog.karajan.compiled.nodes.functions.Java$SystemProperty"))
}
