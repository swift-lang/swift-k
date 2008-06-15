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
		element(rmdir, [dir, host]
			parallelFor(entry, file:list(dir, host=host)
				epath := concat(dir, "/", entry)
				if(
					file:isDirectory(epath, host=host) rmdir(epath, host)
					file:remove(epath, host=host)
				)
			)
			dir:remove(dir, host=host)
		)
		
		element(createdirs, [path, dir, host]
			dc := dircat(dir, path)
			log(LOG:INFO, "START path={path} dir={dir} - Creating directory structure")

			dir:make(dc, host=host)
		)
							
		element(isDone, [stageout]
			sys:and(
				for(pv, stageout
					[path, var] := each(pv)
					vdl:isLogged(var, path)
				)
				sys:not(isEmpty(stageout))
			)
		)
			
		element(mark, [stageout, err, optional(mapping)]
			if(
				err for(pv, stageout
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
		
		element(checkJobStatus, [rhost, wfdir, jobid, tr, jobdir]
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
					file:remove("{wfdir}/status/{jobdir}/{jobid}-success", host=rhost)
					log(LOG:INFO, "SUCCESS jobid={jobid} - Success file found")
				)
				if(
					file:exists("{wfdir}/status/{jobdir}/{jobid}-error", host=rhost) then(
						log(LOG:INFO, "FAILURE jobid={jobid} - Failure file found")
						task:transfer(srchost=rhost, srcdir="{wfdir}/status/{jobdir}", srcfile="{jobid}-error")
						error := parallel(
							file:remove("{wfdir}/status/{jobdir}/{jobid}-error", host=rhost)
							sequential(
								str:strip(file:read("{jobid}-error"))
								file:remove("{jobid}-error")
							)
						)
						throw(error)
					)
					else (	
						log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Both status files are missing")
						throw("No status file was found. Check the shared filesystem on {rhost}")  
					)
				)
			)
		)
		
		element(initSharedDir, [rhost]
			once(list(rhost, "shared")
				vdl:setprogress("Initializing site shared directory")

				log(LOG:INFO, "START host={rhost} - Initializing shared directory")

				wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
				sharedDir := dircat(wfdir, "shared")
				dir:make(sharedDir, host=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="wrapper.sh", destdir=sharedDir, desthost=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="seq.sh", destdir=sharedDir, desthost=rhost)
				dir:make(dircat(wfdir, "kickstart"), host=rhost)
				dir:make(dircat(wfdir, "status"), host=rhost)
				dir:make(dircat(wfdir, "info"), host=rhost)
				wfdir, sharedDir
				//we send the cleanup data to vdl:main()
				to(cleanup, list(wfdir, rhost))
				log(LOG:INFO, "END host={rhost} - Done initializing shared directory")
			)
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
		
		element(createDirSet, [jobid, destdir, host, dirs]
			/*
			 * Ideally this would be done by creating a tree of the directories
			 * to be created and (eventually) exploiting the concurrency in that.
			 */
			log(LOG:INFO, "START jobid={jobid} host={host} - Initializing directory structure")
			for(u, dirs
				cacheOn(list(u, destdir, host)
					createdirs(u, destdir, host)
				)
			)
			log(LOG:INFO, "END jobid={jobid} - Done initializing directory structure")
		)
		
		element(cleanup, [dir, host]
			log(LOG:INFO, "START dir={dir} host={host}")
			if(vdl:configProperty("sitedir.keep") == "false"
				task:execute("/bin/rm", arguments="-rf {dir}", host=host, batch=true)
			)
			log(LOG:INFO, "END dir={dir} host={host}")
		)
		
		element(cleanups, [cleanup]
			log(LOG:INFO, "START cleanups={cleanup}")
			parallelFor(i, cleanup
				[dir, host] := each(i)
				try(
					cleanup(dir, host)
					catch(".*",
						log(LOG:DEBUG, "EXCEPTION - Exception caught while cleaning up", exception)
						to(warnings, exception("Cleanup on {host} failed", exception))
					)
				)
			)
		)
		
		element(cleanupFiles, [files, host]
			uParallelFor(r, files
				log(LOG:INFO, "Purging ", r, " on ", host)
				file:remove(r, host=host)
				vdl:cacheFileRemoved(r, host)
			)
		)
				
		element(doStagein, [jobid, files, dir, host]
			log(LOG:INFO, "START jobid={jobid} - Staging in files")
			uParallelFor(file, files
				provider := provider(file)
				srchost := hostname(file)
				srcdir := dirname(file)
				destdir := dircat(dir, reldirname(file))
				filename := basename(file)
				size := file:size("{srcdir}/{filename}", host=srchost, provider=provider)

				vdl:cacheAddAndLockFile(filename, destdir, host, size
					cleanupFiles(cacheFilesToRemove, host)
					log(LOG:DEBUG, "FILE_STAGE_IN_START file={file} srchost={srchost} srcdir={srcdir} srcname={filename} ",
						"desthost={host} destdir={destdir} provider={provider}")
					restartOnError(".*", 2
					    task:transfer(srcprovider=provider, srchost=srchost, srcfile=filename, 
					    	srcdir=srcdir, desthost=host, destdir=destdir)
					)
					log(LOG:DEBUG, "FILE_STAGE_IN_END file={file} srchost={srchost} srcdir={srcdir} srcname={filename} ",
						"desthost={host} destdir={destdir} provider={provider}")
				)
			)
			log(LOG:INFO, "END jobid={jobid} - Staging in finished")
		)
		
		element(doStageout, [jobid, stageouts, dir, host]
			log(LOG:INFO, "START jobid={jobid} - Staging out files")
			done := list(
				uparallelFor(pv, stageouts
					[path, var] := each(pv)
					file := vdl:absfilename(vdl:getfield(var, path = path))
					provider := vdl:provider(file)
					dhost := vdl:hostname(file)
					rdir := dircat(dir, reldirname(file))
					bname := basename(file)
					ldir := dirname(file)
					fullLocal := dircat(ldir, bname)
					fullRemote := dircat(rdir, bname)
										
					log(LOG:DEBUG, "FILE_STAGE_OUT_START srcname={bname} srcdir={rdir} srchost={host} ",
							"destdir={ldir} desthost={dhost} provider={provider}")
					//make sure we do have the directory on the client side
					dir:make(ldir)
					restartOnError(".*", 2
					    task:transfer(srchost=host, srcfile=bname,
					        srcdir=rdir, destdir=ldir, desthost=dhost, destprovider=provider)
					)
					vdl:logvar(var, path, host, dir, bname)
					log(LOG:DEBUG, "FILE_STAGE_OUT_END srcname={bname} srcdir={rdir} srchost={host} ",
						"destdir={ldir} desthost={dhost} provider={provider}")
	
					list(bname, rdir, host, file:size(fullLocal))
				)
			)
			uParallelFor(f, done
				[bname, rdir, host, size] := each(f)
				vdl:cacheAddFile(bname, rdir, host, size
					cleanupFiles(cacheFilesToRemove, host)
				)
			)
			log(LOG:INFO, "END jobid={jobid} - Staging out finished")
		)
		
		element(graphStuff, [tr, stagein, stageout, err, optional(args)]
			if(
				vdl:configProperty("pgraph") != "false" then(
					errprops := if(err ",color=lightsalmon" "")
					tp := vdl:threadPrefix()
					to(graph, 
						concat(str:quote(tp), " [label=", str:quote("{tr}{errprops}"), "]")
					)
					for(si, stagein
						si := basename(si)
						to(graph
							concat(str:quote(si), " [shape=parallelogram{errprops}]")
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
								str:quote("{label}{errprops}"), "]")
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
		
		element(transferSTDs, [rhost, tmpdir, jobid, stdout, stderr]
			concat(
				for(f, list(list("stderr.txt", stderr), list("stdout.txt", stdout))	
					[name, file] := each(f)
					destfile := "{jobid}-{file}"
					nl()
					"{name}: "
					try(
						sequential(
							task:transfer(srchost=rhost, srcdir=tmpdir, srcfile=file,
								destfile=destfile)

							file:read(destfile)
						)
						nl()
					)
					maybe(file:remove(destfile))
				)
			)
		)
		
		element(transferKickstartRec, [rhost, wfdir, jobid, jobdir]
			if(sys:not(file:exists("{VDL:SCRIPTNAME}-{VDL:RUNID}.d"))
				task:dir:make("{VDL:SCRIPTNAME}-{VDL:RUNID}.d")
			)
			recfile := "{jobid}-kickstart.xml"
			srcdir := dircat(concat(wfdir, "/kickstart/"), jobdir)
			try(
				task:transfer(srchost=rhost, srcdir=srcdir, srcfile=recfile, destdir="{VDL:SCRIPTNAME}-{VDL:RUNID}.d/")
				(
					maybe(file:remove(recfile))
					log(LOG:WARN, "Failed to transfer kickstart records from {srcdir}/{rhost}", exception)
				)
			)
			recfile
		)

		element(transferWrapperLog, [rhost, wfdir, jobid, jobdir]
			if(sys:not(file:exists("{VDL:SCRIPTNAME}-{VDL:RUNID}.d"))
				task:dir:make("{VDL:SCRIPTNAME}-{VDL:RUNID}.d")
			)
			recfile := "{jobid}-info"
			srcdir := dircat(concat(wfdir, "/info/"), jobdir)
			try(
				task:transfer(srchost=rhost, srcdir=srcdir, srcfile=recfile, destdir="{VDL:SCRIPTNAME}-{VDL:RUNID}.d/")
				(
					maybe(file:remove(recfile))
					log(LOG:WARN, "Failed to transfer wrapper log from {srcdir}/{rhost}")
					log(LOG:DEBUG, "Exception for wrapper log failure from {srcdir}/{rhost}: ", exception)
				)
			)
			recfile
		)

		element(execute2, [tr, optional(arguments, stdin, stdout, stderr), stagein, stageout, 
			replicationGroup, replicationChannel]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))
			vdl:setprogress("Selecting site")
			allocateHost(rhost, constraints=vdl:jobConstraints(tr)
				
				[wfdir, sharedDir] := try(
					initSharedDir(rhost)
					throw(exception("Could not initialize shared directory on {rhost}", exception))
				)
				
				uid := uid()
				jobdir := substring(uid, from=0, to=1)
				jobid := concat(tr, "-", uid)
				
				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread={#thread} host={rhost} replicationGroup={replicationGroup}")
				vdl:setprogress("Stage in")
				tmpdir := dircat(concat(wfdir, "/jobs/", jobdir), jobid)
				
				stdout := try(stdout, "stdout.txt")
				stderr := try(stderr, "stderr.txt")
				
				kickstart := vdl:kickstart(rhost)
				
				try(
					sequential(
						fileDirs := fileDirs(stagein, stageout)
						
						createDirSet(jobid, sharedDir, rhost, fileDirs)
						doStagein(jobid, stagein, sharedDir, rhost)

						log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", maybe(" arguments=", arguments), " tmpdir={tmpdir} host={rhost}")
				
						vdl:setprogress("Executing")
						vdl:execute("/bin/bash",
							list("shared/wrapper.sh", jobid,
								"-jobdir", jobdir,
								"-e", vdl:executable(tr, rhost), 
								"-out", stdout, 
								"-err", stderr, 
								"-i", maybe(stdin),
								"-d", flatten(each(fileDirs)),
								"-if", flatten(infiles(stagein)), 
								"-of", flatten(outfiles(stageout)),
								"-k", kickstart,
								"-a", maybe(each(arguments)))
							directory=wfdir
							redirect=false
							host=rhost
							vdl:tcprofile(tr, rhost) //this gets various app params from the tc, such as environment, walltime, etc
							replicationGroup=replicationGroup
							replicationChannel=replicationChannel
						)
							
						checkJobStatus(rhost, wfdir, jobid, tr, jobdir)
		
						log(LOG:DEBUG, "STAGING_OUT jobid={jobid}")

									
						/* need to stage the files to upper scratch area in case they are not transfered to another site
						   before all the files get cleaned out */
					
						
						vdl:setprogress("Stage out")
						doStageout(jobid, stageout, sharedDir, rhost)
						if(
							kickstart != "" & vdl:configProperty("kickstart.always.transfer") == "true"
							discard(transferKickstartRec(rhost, wfdir, jobid, jobdir))
						)
						if(
							vdl:configProperty("wrapperlog.always.transfer") == "true"
							discard(transferWrapperLog(rhost, wfdir, jobid, jobdir))
						)
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, cleanupFiles(cacheFilesToRemove, rhost))
						log(LOG:DEBUG, "JOB_END jobid={jobid}")
					)
					catch("^Abort$"
						log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, force=false
							cleanupFiles(cacheFilesToRemove, rhost)
						)
						throw(exception)
					)
					catch("^(?!Abort$).*"
						vdl:setprogress("Failed but can retry")
						log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, force=false
							cleanupFiles(cacheFilesToRemove, rhost)
						)
						
						outs := transferSTDs(rhost, tmpdir, jobid, stdout, stderr)
						
						discard(transferWrapperLog(rhost, wfdir, jobid, jobdir))
						kickstartRec := if(
							kickstart == "" ""
							else(
								try(
									(
										recfile := transferKickstartRec(rhost, wfdir, jobid, jobdir)
										"KickstartRecord: {recfile}"
									)
									""
								)
							)
						)
						
						throw(
							exception(
								concat(
									"Exception in {tr}:", nl(),
									maybe("Arguments: {arguments}", nl()),
									"Host: {rhost}", nl(),
									"Directory: {tmpdir}",
									"{outs}", nl(),
									"----", nl(),
									kickstartRec
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
