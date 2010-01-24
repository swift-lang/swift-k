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

namespace("vdl"
	export(
		element(isDone, [stageout]
			sys:and(
				for(pv, stageout
					[path, var] := each(pv)
					vdl:isLogged(var, path)
				)
				sys:not(isEmpty(stageout))
			)
		)
			
		element(mark, [restarts, err, optional(mapping)]
			if(
				err for(pv, restarts
					[path, var] := each(pv)
					vdl:setFutureFault(var, path=path, mapping=mapping)
				)
			)
		)
		
		element(flatten, [...]
			if (
				isEmpty(...) ""
				
				concat(
					for(i, butLast(...), if(isList(i) flatten(i) i), "|") last(...)
				)
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
		
		element(inFileDirs, [stageins]
			for(file, stageins
		 		reldirname(file)
			)
		)
		
		element(outFileDirs, [stageouts] 
			for(pv, stageouts
				[path, var] := each(pv)
			
				file := vdl:filename(vdl:getfield(var, path = path))
				
				dirname(file)
			)
		)
		
		element(inFiles, [stageins]
			pathnames(stageins)
		)
		
		
		element(outFiles, [stageouts]
			for(pv, stageouts
				[path, var] := each(pv)
			
				file := vdl:filename(vdl:getfield(var, path = path))
				
				file
			)
		)
		
		element(fileDirs, [stageins, stageouts]
			list(
				unique(
					inFileDirs(stageins)
					outFileDirs(stageouts)
				)
			)
		)
														
		element(appStageins, [jobid, files, dir, stagingMethod]
			for(file, files
				protocol := vdl:provider(file)
				provider := if(protocol == "file", stagingMethod, protocol)
				srchost := hostname(file)
				srcdir := dirname(file)
				destdir := dircat(dir, reldirname(file))
				filename := basename(file)
				
				stageIn(
					"{provider}://{srchost}/{srcdir}/{filename}", 
					"{destdir}/{filename}"
				)
			)
		)
		
		element(appStageouts, [jobid, stageouts, dir, stagingMethod]
			for(pv, stageouts
				[path, var] := each(pv)
				file := vdl:absfilename(vdl:getfield(var, path = path))
				protocol := vdl:provider(file)
				provider := if(protocol == "file", stagingMethod, protocol)
				dhost := vdl:hostname(file)
				rdir := dircat(dir, reldirname(file))
				bname := basename(file)
				ldir := dirname(file)
				fullLocal := dircat(ldir, bname)
				fullRemote := dircat(rdir, bname)
				
				stageOut(
					"{rdir}/{bname}",
				    "{provider}://{dhost}/{ldir}/{bname}"
				)									
			)
		)

		element(doRestartlog, [restartouts]
			uParallelFor(f, restartouts,
				[path, var] := each(f)
				vdl:logvar(var, path)
			)
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
		
		element(fileSizes, [files]
			math:sum(
				for(f, files, file:size(file))
			)
		)
		
		element(cleanups, [cleanup]
			log(LOG:INFO, "START cleanups={cleanup}")
		)

		element(execute2, [tr, optional(arguments, stdin, stdout, stderr), stagein, stageout,  restartout,
			replicationGroup, replicationChannel]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))
			allocateHost(rhost, constraints=vdl:jobConstraints(tr, stagein=stagein)
				
				ddir := initDDir()
				
				uid := uid()
				jobdir := substring(uid, from=0, to=1)
				jobid := concat(tr, "-", uid)
				
				fileDirs := fileDirs(stagein, stageout)
				
				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread={#thread} host={rhost} replicationGroup={replicationGroup}")

				wrapper := "_swiftwrap.staging"
				wrapfile := "{ddir}/param-{jobid}"

				stdout := try(stdout, "stdout.txt")
				stderr := try(stderr, "stderr.txt")

				vdl:setprogress("Stage in")
				wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
				tmpdir := dircat(concat(wfdir, "/jobs/", jobdir), jobid)
				
				try(
					sequential(
						log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", maybe(" arguments=", arguments), " tmpdir={tmpdir} host={rhost}")
				
						vdl:setprogress("Submitting")

						vdl:execute(
							vdl:siteprofile(rhost, "swift:wrapperInterpreter"),
							list(
								vdl:siteprofile(rhost, "swift:wrapperInterpreterOptions"),
								wrapper,
								"-e", vdl:executable(tr, rhost), 
								"-out", stdout,
								"-err", stderr, 
								"-i", maybe(stdin),
								"-d", flatten(each(fileDirs)),
								"-if", flatten(infiles(stagein)), 
								"-of", flatten(outfiles(stageout)),
								"-k",
								"-status", "provider"
								"-a", maybe(each(arguments))
							)
							directory = "{wfdir}-{jobdir}-{jobid}"
							redirect = false
							host = rhost
							vdl:tcprofile(rhost, tr = tr) //this gets various app params from the tc, such as environment, walltime, etc
							replicationGroup = replicationGroup
							replicationChannel = replicationChannel
							jobid = jobid
							
							stagingMethod := vdl:siteProfile(rhost, "swift:stagingMethod", default="proxy") 	
							
							stageIn("{stagingMethod}://localhost/{swift.home}/libexec/{wrapper}", wrapper)
							appStageins(jobid, stagein, ".", stagingMethod)
							
							stageOut("wrapper.log", "{stagingMethod}://localhost/{ddir}/{jobid}.info")
							stageOut("{stdout}", "{stagingMethod}://localhost/{ddir}/{stdout}")
							stageOut("{stderr}", "{stagingMethod}://localhost/{ddir}/{stderr}")
							appStageouts(jobid, stageout, ".", stagingMethod)
							
							task:cleanUp(".") //the whole job directory
						)
						doRestartlog(restartout)
						log(LOG:DEBUG, "JOB_END jobid={jobid}")
					)
					catch("^Abort$"
						log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
						throw(exception)
					)
					catch("^(?!Abort$).*"
						vdl:setprogress("Failed but can retry")
						log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)
						
						throw(
							exception(
								concat(
									"Exception in {tr}:", nl(),
									maybe("Arguments: {arguments}", nl()),
									"Host: {rhost}", nl(),
									"Directory: {tmpdir}",
									"TODO: outs", nl(),
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
