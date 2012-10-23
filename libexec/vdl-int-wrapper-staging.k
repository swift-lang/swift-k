import("sys.k")
import("task.k")
import("vdl-lib.xml")
/*
 * Things that are not exposed to the translated file
 */

global(LOG:DEBUG, "debug")
global(LOG:INFO, "info")
global(LOG:WARN, "warn")
global(LOG:ERROR, "error")
global(LOG:FATAL, "fatal")

global(URL_PREFIX, 
	elementDef(getURLPrefix, className="org.griphyn.vdl.karajan.lib.GetURLPrefix")
	getURLPrefix() 
)

global(WRAPPERLOG_ALWAYS_TRANSFER, vdl:configProperty("wrapperlog.always.transfer"))
global(SITEDIR_KEEP, vdl:configProperty("sitedir.keep"))


namespace("vdl"
	export(
		element(checkJobStatus, [jobdir, jobid, tr]
			log(LOG:DEBUG, "START jobid={jobid}")
			try(
				sequential(
					/*
					 * This is a bit of optimization, but I'm not completely
					 * sure of its correctness. The goal is to both detect
					 * the presence of the success file and remove it, all
					 * in one operation. It relies on file:remove() throwing
					 * an exception if the file is not there.
					 */
					file:remove("{jobdir}/_swift.success")
					log(LOG:INFO, "SUCCESS jobid={jobid} - Success file found")
				)
				throw(checkErrorFile(jobdir, jobid))
			)
		)
		
		element(checkErrorFile, [jobdir, jobid]
			if (
				file:exists("{jobdir}/_swift.error") then(
					log(LOG:INFO, "FAILURE jobid={jobid} - Failure file found")
					error := str:strip(file:read("{jobdir}/_swift.error"))
					file:remove("{jobdir}/_swift.error")
					error
				)
				else (
					log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Both status files are missing")
					throw("No status file was found")
				)
			)
		)

		element(initSharedDir, [rhost]
			once(list(rhost, "shared")
				vdl:setprogress("Initializing site shared directory")

				log(LOG:INFO, "START host={rhost} - Initializing shared directory")

				wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
				dir:make(wfdir, host=rhost)
				transfer(srcdir="{swift.home}/libexec/", srcfile="_swiftwrap.wrapperstaging", destdir=wfdir, desthost=rhost)

				wfdir
				//we send the cleanup data to vdl:main()
				to(cleanup, list(wfdir, rhost))
				log(LOG:INFO, "END host={rhost} - Done initializing shared directory")
			)
		)

		element(initDDir, []
			ddir := "{VDL:SCRIPTNAME}-{VDL:RUNID}.d"
			once(ddir
				if(sys:not(file:exists(ddir))
					task:dir:make(ddir)
				)
			)
			ddir
		)

		element(inFiles, [stageins]
			stageins
		)

		element(fileDirs, [stageins, stageouts]
			list(
				unique(
					inFileDirs(stageins)
					outFileDirs(stageouts)
				)
			)
		)

		element(cleanup, [dir, host]
			log(LOG:INFO, "START dir={dir} host={host}")
			if(vdl:configProperty("sitedir.keep") == "false"
				task:execute(
					vdl:siteprofile(host, "swift:cleanupCommand"),
					arguments=list(
						vdl:siteprofile(host, "swift:cleanupCommandOptions"),
						dir
					)
					host=host, batch=true, tcprofile(host))
			)
			log(LOG:INFO, "END dir={dir} host={host}")
		)

		element(cleanups, [cleanup]
			log(LOG:INFO, "START cleanups={cleanup}")
			parallelFor(i, cleanup
				[dir, host] := each(i)
				try(
					vdl:cleanup(dir, host)
					catch(".*",
						log(LOG:DEBUG, "EXCEPTION - Exception caught while cleaning up", exception)
						to(warnings, exception("Cleanup on {host} failed", exception))
					)
				)
			)
			log(LOG:INFO, "END cleanups={cleanup}")
		)

		element(stageWrapperParams, [jobid, wrapfile, dir, host]
			log(LOG:INFO, "START jobid={jobid} - staging in wrapper params")
			provider := provider(wrapfile)
			srchost := hostname(wrapfile)
			srcdir := vdl:dirname(wrapfile)
			destdir := dir
			filename := basename(wrapfile)

			cacheOn(list(destdir, host)
				dir:make(destdir, host=host, provider=provider)
			)

			log(LOG:INFO, "END jobid={jobid}")
		)

		element(graphStuff, [tr, stagein, stageout, err, optional(args)]
			if(
				vdl:configProperty("pgraph") != "false" then(
					errprops := if(err ",color=lightsalmon" ",color=lightsteelblue1")
					tp := vdl:threadPrefix()
					to(graph,
						concat(str:quote(tp), " [label=", str:quote(tr), "{errprops}]")
					)
					for(si, stagein
						si := basename(si)
						to(graph
							concat(str:quote(si), " [shape=parallelogram]")
							concat(str:quote(si), " -> ", str:quote(tp))
						)
					)
					for(pv, stageout
						[path, var] := each(pv)
						file := vdl:fileName(vdl:getfield(var, path=path))
						file := basename(file)
						label := vdl:niceName(var, path = path)
						to(graph
							concat(str:quote(file), " [shape=parallelogram,label=",
								str:quote(label), "]")
							concat(str:quote(tp), " -> ", str:quote(file))
						)
					)
				)
			)
		)
		
		element(readStandardFiles, [jobdir, stdout, stderr]
			concat(
				for(f, list(list("_swift.stderr", stderr), list("_swift.stdout", stdout))
					[name, file] := each(f)
					destfile := "{jobdir}/{file}"
					nl()
					"{name}: "
					try(
						file:read(destfile)
						nl()
					)
				)
			)
		)
		

		element(execute2, [tr, optional(arguments, stdin, stdout, stderr, attributes), stagein, stageout,  restartout,
			replicationGroup, replicationChannel]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))

			allocateHost(rhost, constraints=vdl:jobConstraints(tr, stagein=stagein)

				ddir := initDDir()
				wfdir := try(
					initSharedDir(rhost)
					throw(exception("Could not initialize shared directory on {rhost}", exception))
				)

				uid := uid()
				jobid := "{tr}-{uid}"
				
				jobdir := concat(ddir, "/jobs/", substring(uid, from=0, to=1), "/{jobid}/")

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread={#thread} host={rhost} replicationGroup={replicationGroup}")

				statusMode := configProperty("status.mode",host=rhost)
				wrapperMode := configProperty("wrapper.parameter.mode",host=rhost)

				wrapfile := "{jobdir}/_paramfile"

				stdout := try(getFieldValue(stdout), "_swift.stdout")
				stderr := try(getFieldValue(stderr), "_swift.stderr")
				fileDirs := fileDirs(stagein, stageout)
				os := vdl:siteprofile(rhost, "SYSINFO:OS")
				
				scratch := vdl:siteprofile(rhost, "scratch")

				if(wrapperMode == "files"
					sequential(
						sys:file:write(wrapfile,
							"-e ",vdl:executable(tr, rhost), nl(),
							"-out ", stdout, nl(),
							"-err ", stderr, nl(),
							"-i ", maybe(getFieldValue(stdin)), nl(),
							"-d ", flatten(each(fileDirs)), nl(),
							"-if ", flatten(inFiles(stagein)), nl(),
							"-of ", flatten(outFiles(stageout)), nl(),
							"-wt", WRAPPERLOG_ALWAYS_TRANSFER,
							"-sk", SITEDIR_KEEP,
							"-cdmfile ", cdm:file(), nl(),
							"-status ", statusMode, nl(),
							for(a, arguments, "-a ", a, nl())
						)
					)
				)

				vdl:setprogress("Stage in")

				try(
					sequential(

						if(wrapperMode == "files"
							stageWrapperParams(jobid, wrapfile, wfdir, rhost)
						)

						log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", maybe(" arguments=", arguments), " host={rhost}")

						vdl:setprogress("Submitting")

						if(wrapperMode == "files"
							vdl:execute(
								vdl:siteprofile(rhost, "swift:wrapperInterpreter"),
								list(
									vdl:siteprofile(rhost, "swift:wrapperInterpreterOptions"),
									"_swiftwrap.wrapperstaging",
									jobid,
									"-urlprefix", URL_PREFIX,
									"-jobdir", jobdir,
									"-scratch", scratch, 
									"-p", wrapfile
								)
								directory=wfdir
								redirect=false
								host=rhost
								vdl:tcprofile(rhost, maybe(attributes=attributes), tr=tr) //this gets various app params from the tc, such as environment, walltime, etc
								replicationGroup=replicationGroup
								replicationChannel=replicationChannel
								jobid=jobid
							)
						)
						if(wrapperMode == "args"
							vdl:execute(
								vdl:siteprofile(rhost, "swift:wrapperInterpreter"),
								list(
									vdl:siteprofile(rhost, "swift:wrapperInterpreterOptions"),
									"_swiftwrap.wrapperstaging",
									jobid,
									"-urlprefix", URL_PREFIX,
									"-jobdir", jobdir,
									"-scratch", scratch,
									"-e", vdl:executable(tr, rhost),
									"-out", stdout,
									"-err", stderr,
									"-i", maybe(getFieldValue(stdin)),
									"-d", flatten(each(fileDirs)),
									"-if", flatten(inFiles(stagein)),
									"-of", flatten(outFiles(stageout)),
									"-wt", WRAPPERLOG_ALWAYS_TRANSFER,
									"-sk", SITEDIR_KEEP,
									"-cdmfile", cdm:file(),
									"-status", statusMode,
									"-a", maybe(each(arguments))
								)
								directory=wfdir
								redirect=false
								host=rhost
								vdl:tcprofile(rhost, maybe(attributes=attributes), tr=tr)
								replicationGroup=replicationGroup
								replicationChannel=replicationChannel
								jobid=jobid
							)
						)

						vdl:setprogress("Checking status")
						if(statusMode == "files"
							checkJobStatus(jobdir, jobid, tr)
						)

						if(wrapperMode == "files"
							file:remove(wrapfile)
						)

						log(LOG:DEBUG, "STAGING_OUT jobid={jobid}")


						/* need to stage the files to upper scratch area in case they are not transfered to another site
						   before all the files get cleaned out */


						vdl:setprogress("Stage out")
						doRestartlog(restartout)
						
						log(LOG:DEBUG, "JOB_END jobid={jobid}")
					)
					catch("^Abort$"
						log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
						throw(exception)
					)
					catch("^(?!Abort$).*"
						vdl:setprogress("Failed but can retry")
						prev := exception
						exception := try(exception(checkErrorFile(jobdir, jobid)), prev)
						
						log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

						if(matches(exception,".*executable bit.*")
							generateError(exception)
						)
						
						outs := readStandardFiles(jobdir, stdout, stderr)

						throw(
							exception(
								concat(
									"Exception in {tr}:", nl(),
									maybe("Arguments: ", arguments, nl()),
									"Host: {rhost}", nl(),
									"Directory: {scratch}/{jobid}",
									"{outs}", nl(),
									"----", nl()
								)
								exception
							)
						)
					)
				)
			)
		)

		element(generateProvenanceGraph, [gdata]
			pgraph := vdl:configProperty("pgraph")
			gname := if(pgraph == "true" "{VDL:SCRIPTNAME}-{VDL:RUNID}.dot" pgraph)
			file:write(gname
				"digraph SwiftProvenance {{", nl()
				"	graph [", vdl:configProperty("pgraph.graph.options"), "];", nl()
				"	node [", vdl:configProperty("pgraph.node.options"), "];", nl()

				for(i, gdata
					"	", i, nl()
				)
				"}", nl()
			)
			log(LOG:INFO, "Provenance graph saved in ", gname)
		)
	)
)

// Local variables:
// mode: scheme
// tab-width: 4
// indent-tabs-mode: t
// End:
