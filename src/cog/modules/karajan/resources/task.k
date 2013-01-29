namespace(task) {
	export(handler, def("org.globus.cog.karajan.compiled.nodes.grid.TaskHandlerNode"))
	export(availableHandlers, def("org.globus.cog.karajan.compiled.nodes.grid.AvailableTaskHandlers"))
	export(scheduler, def("org.globus.cog.karajan.compiled.nodes.grid.SchedulerNode"))
	export(securityContext, def("org.globus.cog.karajan.compiled.nodes.grid.SecurityContextNode"))
	export(resources, def("org.globus.cog.karajan.compiled.nodes.grid.GridNode"))
	export(host, def("org.globus.cog.karajan.compiled.nodes.grid.HostNode"))
	export(service, def("org.globus.cog.karajan.compiled.nodes.grid.ServiceNode"))

	export(host:hasService, def("org.globus.cog.karajan.compiled.nodes.grid.Functions$HostHasService"))
	export(serviceURI, def("org.globus.cog.karajan.compiled.nodes.grid.Functions$ServiceURI"))

	export(allocateHost, def("org.globus.cog.karajan.compiled.nodes.grid.AllocateHost"))
	export(execute, def("org.globus.cog.karajan.compiled.nodes.grid.GridExec"))
	export(transfer, def("org.globus.cog.karajan.compiled.nodes.grid.GridTransfer"))

	export(file:list, def("org.globus.cog.karajan.compiled.nodes.grid.FileList"))
	export(file:remove, def("org.globus.cog.karajan.compiled.nodes.grid.FileRemove"))
	export(file:exists, def("org.globus.cog.karajan.compiled.nodes.grid.FileExists"))
	export(dir:make, def("org.globus.cog.karajan.compiled.nodes.grid.MakeDir"))
	export(dir:remove, def("org.globus.cog.karajan.compiled.nodes.grid.Rmdir"))
	export(file:isDirectory, def("org.globus.cog.karajan.compiled.nodes.grid.IsDirectory"))
	export(file:chmod, def("org.globus.cog.karajan.compiled.nodes.grid.Chmod"))
	export(file:rename, def("org.globus.cog.karajan.compiled.nodes.grid.Rename"))
	export(file:size, def("org.globus.cog.karajan.compiled.nodes.grid.FileSize"))
	export(file:info, def("org.globus.cog.karajan.compiled.nodes.grid.FileInfo"))

	export(LOCATION:STREAM, 1)
	export(LOCATION:LOCAL, 2)
	export(LOCATION:REMOTE, 4)
	
	export(FILETYPE:UNKNOWN, 0)
	export(FILETYPE:FILE, 1)
	export(FILETYPE:DIRECTORY, 2)
	export(FILETYPE:SOFTLINK, 3)
	export(FILETYPE:DEVICE, 4)
	
	export(STAGING_MODE:ALWAYS, 1)
	export(STAGING_MODE:IF_PRESENT, 2)
	export(STAGING_MODE:ON_ERROR, 4)
	export(STAGING_MODE:ON_SUCCESS, 8)
	export(STAGING_MODE:DEFAULT, 10)
	
	import(java)
	import(sys)
	
	export(SSHSecurityContext,
		function(credentials) {
			task:securityContext("ssh", credentials)
		}
	)
	
	export(InteractiveSSHSecurityContext,
		function(username = null, privateKey = null, noGUI = null) {
			sc := java:new("org.globus.cog.abstraction.impl.ssh.InteractiveSSHSecurityContextImpl")
			if (username != null) {
				java:invokeMethod("setAttribute", sc, types = ["java.lang.String", "java.lang.Object"], 
					"username", username)
			}
			if (privateKey != null) {
				java:invokeMethod("setAttribute", sc, types = ["java.lang.String", "java.lang.Object"], 
					"privatekey", privateKey)
			}
			if (noGUI != null) {
				java:invokeMethod("setAttribute", sc, types = ["java.lang.String", "java.lang.Object"], 
					"nogui", noGUI)
			}
			sc
		}
	)

	export(passwordAuthentication,
		function(username, password) {
			java:new("java.net.PasswordAuthentication", types = ["java.lang.String", "char[]"],
				username, password)
		}
	)

	export(publicKeyAuthentication,
		function(username, privatekey, passphrase) {
			java:new("org.globus.cog.abstraction.impl.common.PublicKeyAuthentication",
				types = ["java.lang.String", "java.lang.String", "char[]"],
				username, privatekey, passphrase)
		}
	)
	
	export(taskTransformer,
		function(classname) {
			to(taskTransformers, classname)
		}
	)
	
	export(transferParams,
		def("org.globus.cog.karajan.compiled.nodes.grid.BDP")
	)
	
	export(envVar, 
		function(name, value) {
			to(environment, map:entry(name, value))
		}
	)
	
	export(stageIn, 
		function(source, destination) {
			to(stagein, [source, destination])
		}
	)
		
	export(stageOut, 
		function(source, destination, mode = STAGING_MODE:DEFAULT) {
			to(stageout, [source, destination, mode])
		}
	)
	
	export(cleanUp, 
		function(file) {
			to(cleanup, file)
		}
	)
}