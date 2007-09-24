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
			log(LOG:INFO, "Creating directory structure {path} in {dir} ({dc})")
			dir:make(dc, host=host)
		)
							
		element(isDone, [stageout]
			and(
				for(pv, stageout
					[path, var] := each(pv)
					vdl:isLogged(var, path)
				)
				not(isEmpty(stageout))
			)
		)
			
		element(mark, [stageout, optional(err, mapping)]
			if(
				err for(pv, stageout
					[path, var] := each(pv)
					vdl:setFutureFault(var, path=path, mapping=mapping)
				)
				for(pv, stageout
					[path, var] := each(pv)
					maybe(vdl:setFieldValue(var, true, path=path))
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
		
		element(checkJobStatus, [rhost, wfdir, jobid, tr]
			try(
				sequential(
					/*
					 * This is a bit of optimization, but I'm not completely
					 * sure of its correctness. The goal is to both detect
					 * the presence of the success file and remove it, all
					 * in one operation. It relies on file:remove() throwing 
					 * an exception if the file is not there. 
					 */
					file:remove("{wfdir}/status/{jobid}-success", host=rhost)
					log(LOG:INFO, "Job {jobid} succeeded")
				)
				if(
					file:exists("{wfdir}/status/{jobid}-error", host=rhost) then(
						task:transfer(srchost=rhost, srcdir="{wfdir}/status", srcfile="{jobid}-error")
						error := parallel(
							file:remove("{wfdir}/status/{jobid}-error", host=rhost)
							sequential(
								str:strip(file:read("{jobid}-error"))
								file:remove("{jobid}-error")
							)
						)
						throw(error)
					)
					else (	
						throw("No status file was found. Check the shared filesystem on {rhost}")  
					)
				)
			)
		)
		
		element(initSharedDir, [rhost]
			once(list(rhost, "shared")
				log(LOG:INFO, "Initializing shared directory on ", rhost)

				wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
				sharedDir := dircat(wfdir, "shared")
				dir:make(sharedDir, host=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="wrapper.sh", destdir=sharedDir, desthost=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="seq.sh", destdir=sharedDir, desthost=rhost)
				dir:make(dircat(wfdir, "kickstart"), host=rhost)
				dir:make(dircat(wfdir, "status"), host=rhost)
				dir:make(dircat(wfdir, "info"), host=rhost)
				log(LOG:INFO, "Done initializing shared directory on ", rhost)
				wfdir, sharedDir
				//we send the cleanup data to vdl:main()
				to(cleanup, list(wfdir, rhost))
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
		
		element(createDirSet, [destdir, host, dirs]
			/*
			 * Ideally this would be done by creating a tree of the directories
			 * to be created and (eventually) exploiting the concurrency in that.
			 */
	
			for(u, dirs
				cacheOn(list(u, destdir, host)
					createdirs(u, destdir, host)
				)
			)
		)
		
		element(cleanup, [dir, host]
			log(LOG:INFO, "Cleaning up ", dir, " on ", host)
			task:execute("/bin/rm", arguments="-rf {dir}", host=host, batch=true)
		)
		
		element(cleanups, [cleanup]
			log(LOG:INFO, "Cleanups: ", cleanup)
			parallelFor(i, cleanup
				[dir, host] := each(i)
				try(
					cleanup(dir, host)
					catch(".*",
						log(LOG:DEBUG, "Exception caught while cleaning up", exception)
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
				
		element(doStagein, [files, dir, host]
			uParallelFor(file, files
				[provider, srchost, srcdir, destdir, filename] := 
					(provider(file), hostname(file), dirname(file), dircat(dir, reldirname(file)), basename(file))
				
				vdl:cacheAddAndLockFile(filename, destdir, host, file:size(file)
					//This is kinda hackish, but these files should be cleaned before
					//the other ones are uploaded, and doing it nicer is a complex
					//business. So vdl:cacheAddAndLockFile defines "filesToRemove"
					cleanupFiles(cacheFilesToRemove, host)
					log(LOG:INFO, "Staging in {file} ({srcdir} - {filename} to {destdir} on {host}")
					restartOnError(".*", 2
					    task:transfer(srcprovider=provider, srchost=srchost, srcfile=filename, 
					    	srcdir=srcdir, desthost=host, destdir=destdir)
					)
					log(LOG:INFO, "Staged in {file} to {destdir} on {host}")
				)
			)
		)
		
		element(doStageout, [stageouts, dir, host]
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
									
				log(LOG:INFO, "Staging out ", dircat(rdir, bname), " to {provider}://{dhost}/{fullLocal} from {host}")
				//make sure we do have the directory on the client side
				dir:make(ldir)
				restartOnError(".*", 2
				    task:transfer(srchost=host, srcfile=bname,
				        srcdir=rdir, destdir=ldir, desthost=dhost, destprovider=provider)
				)
		        vdl:logvar(var, path, host, dir, bname)
		        log(LOG:INFO, "Staged out {fullRemote} to {fullLocal} from {host}")

				//there is really no way to tell big files created by apps will be, so
				//the decent thing to do is clean things up as much as possible
				//note however that if a hard quota is enforced remotely, this
				//will not prevent the quota from being exhausted
				vdl:cacheAddFile(bname, rdir, host, file:size(fullLocal)
					cleanupFiles(cacheFilesToRemove, host)
				)
			)
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
						to(graph
							concat(str:quote(si), " [shape=parallelogram{errprops}]")
							concat(str:quote(si), " -> ", str:quote(tp))
						)
					)
					for(pv, stageout
						[path, var] := each(pv)
						file := vdl:fileName(vdl:getfield(var, path=path))
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
							 // If multiple concurrent errors occur, 
							 // then files might not get deleted (in non-lazy error mode)
						)
						nl()
					)
					maybe(file:remove(destfile))
				)
			)
		)
		
		element(transferKickstartRec, [rhost, wfdir, jobid]
			recfile := "{jobid}-kickstart.xml"
			srcdir := dircat(wfdir, "kickstart")
			try(
				task:transfer(srchost=rhost, srcdir=srcdir, srcfile=recfile)
				(
					maybe(file:remove(recfile))
					throw(exception("Failed to transfer kickstart records from {srcdir}/{rhost}", exception))
				)
			)
			recfile
		)
	
		element(execute2, [tr, optional(arguments, stdin, stdout, stderr), stagein, stageout]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))
			allocateHost(rhost, constraints=vdl:jobConstraints(tr)
			
				[wfdir, sharedDir] := try(
					initSharedDir(rhost)
					throw(exception("Could not initialize shared directory on {rhost}", exception))
				)
				
				jobid := concat(tr, "-", uid())
				log(LOG:DEBUG, "Job {jobid} running in thread {#thread} on host {rhost}")
				tmpdir := dircat(wfdir, jobid)
				
				stdout := try(stdout, "stdout.txt")
				stderr := try(stderr, "stderr.txt")
				
				kickstart := vdl:kickstart(rhost)
				
				try(
					sequential(
						log(LOG:INFO, "Creating temporary directory {tmpdir} on {rhost}")
					
						/* May need to consult RLS to find the physical file names for stagein and stageout. */
						
						fileDirs := fileDirs(stagein, stageout)
						
						createDirSet(sharedDir, rhost, fileDirs)
						
						doStagein(stagein, sharedDir, rhost)

						log(LOG:INFO, "Running job {jobid} {tr}", maybe(" with arguments ", arguments), " in {tmpdir} on {rhost}")
				
						task:execute("/bin/sh",
							list("shared/wrapper.sh", jobid,
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
						)
							
						checkJobStatus(rhost, wfdir, jobid, tr)
		
						log(LOG:INFO, "Completed job {jobid} {tr}", maybe(" with arguments ", arguments), " on {rhost}")

									
						/* need to stage the files to upper scratch area in case they are not transfered to another site
						   before all the files get cleaned out */
					
						doStageout(stageout, sharedDir, rhost)
						if(
							kickstart != "" & vdl:configProperty("kickstart.always.transfer") == "true"
							discard(transferKickstartRec(rhost, wfdir, jobid))
						)
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, cleanupFiles(cacheFilesToRemove, rhost))
					)
					catch(".*"
						log(LOG:DEBUG, "{jobid} application exception: ", exception)
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, force=false
							cleanupFiles(cacheFilesToRemove, rhost)
						)
						
						outs := transferSTDs(rhost, tmpdir, jobid, stdout, stderr)
						
						kickstartRec := if(
							kickstart == "" ""
							else(
								try(
									(
										recfile := transferKickstartRec(rhost, wfdir, jobid)
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
