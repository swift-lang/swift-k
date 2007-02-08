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
		element(basename, [file]
			last(str:split(file, "/"))
		)
		
		element(pathelements, [file]
			elements := butLast(str:split(file, "/"))
			
			for(element, elements
				element
				"/"
			)
		)
		
		element(dirname, [file]
			concat(
				if (
					file == "" ()
					substring(file, 0, to = 1) == "/" "/"
				)
			
				pathelements(file)
			)
		)
		
		element(reldirname, [file]
			concat(
				pathelements(file)
			)
		)
		
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
					
		element(dircat, [dir, sub], 
			if (dir != "" concat(dir, "/", sub) sub)
		)
		
		element(isDone, [stageout]
			and(
				for(pv, stageout
					[path, var] := each(pv)
					vdl:isLogged(var, path)
				)
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
					for(i, butLast(...), if(isList(i) flatten(i) i), " ") last(...)
				)
			)
		)
		
		element(checkExitCode, [rhost, tmpdir, jobdir, tr]
			if(
				file:exists("{tmpdir}/exitcode", host=rhost) then(
					task:transfer(srchost=rhost, srcdir=tmpdir, srcfile="exitcode",
						destfile="{jobdir}-exitcode")
						
					exitcode := str:strip(file:read("{jobdir}-exitcode"))
					file:remove("{jobdir}-exitcode")
													
					throw("Job {tr} failed with an exit code of {exitcode}")
				)
			)
		)
		
		element(initSharedDir, [rhost]
			once(list(rhost, "shared")
				wfdir := concat("run-", uid())
				sharedDir := dircat(wfdir, "shared")
				dir:make(sharedDir, host=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="wrapper.sh", destdir=sharedDir, desthost=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="seq.sh", destdir=sharedDir, desthost=rhost)
				wfdir, sharedDir
				//we send the cleanup data to vdl:main()
				to(cleanup, list(wfdir, rhost))
			)
		)
		
		element(inFileDirs, [stageins]
			for(file, stageins
		 		dirname(file)
			)
		)
		
		element(outFileDirs, [stageouts] 
			for(pv, stageouts
				[path, var] := each(pv)
			
				file := vdl:absfilename(path, var)
				
				dirname(file)
			)
		)
		
		element(outFiles, [stageouts]
			for(pv, stageouts
				[path, var] := each(pv)
			
				file := vdl:absfilename(path, var)
				
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
				createdirs(u, destdir, host)
			)
		)
				
		element(cleanup, [dir, host]
			log(LOG:INFO, "Cleaning up ", dir, " on ", host)
			task:execute("/bin/rm", arguments="-rf {dir}", host=host)
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
				[srcdir, destdir, filename] := (dirname(file), dircat(dir, reldirname(file)), basename(file))
				
				vdl:cacheAddAndLockFile(filename, destdir, host, file:size(file)
					//This is kinda hackish, but these files should be cleaned before
					//the other ones are uploaded, and doing it nicer is a complex
					//business. So vdl:cacheAddAndLockFile defines "filesToRemove"
					cleanupFiles(cacheFilesToRemove, host)
					log(LOG:INFO, "Staging in {srcdir}{filename} to {destdir} on {host}")
					restartOnError(".*", 2
					    task:transfer(srcfile=filename, srcdir=srcdir,
						    desthost=host, destdir=destdir)
					)
					log(LOG:INFO, "Staged in {srcdir}{filename} to {destdir} on {host}")
				)
			)
		)
		
		element(doStageout, [stageouts, dir, host]
			uparallelFor(pv, stageouts
				[path, var] := each(pv)
				file := vdl:absfilename(path, var)
				rdir := dircat(dir, reldirname(file))
				bname := basename(file)
				ldir := dirname(file)
				fullLocal := dircat(ldir, bname)
				fullRemote := dircat(rdir, bname)
					
				log(LOG:INFO, "Staging out ", dircat(rdir, bname), " to {fullLocal} from {host}")
				//make sure we do have the directory on the client side
				dir:make(ldir)
				restartOnError(".*", 2
				    task:transfer(srchost=host, srcfile=bname, 
				        srcdir=rdir, destdir=ldir)
				)
		        vdl:logvar(var, path)
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
						concat(str:quote(tp), " [label={tr}{errprops}]")
					)
					for(si, stagein
						to(graph
							concat(str:quote(si), " [shape=parallelogram{errprops}]")
							concat(str:quote(si), " -> ", str:quote(tp))
						)
					)
					for(pv, stageout
						[path, var] := each(pv)
						file := vdl:fileName(path, var)
						label := vdl:niceName(var, path = path)
						to(graph
							concat(str:quote(file), " [shape=parallelogram,label={label}{errprops}]")
							concat(str:quote(tp), " -> ", str:quote(file))
						)
					)
				)
			)
		)
		
		element(fileSizes, [files]
			sum(
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
		
		element(transferKickstartRec, [rhost, srcdir, recfile]
			try(
				task:transfer(srchost=rhost, srcdir=srcdir, srcfile="kickstart.xml",
					destfile=recfile)
				(
					maybe(file:remove(recfile))
					throw(exception("Failed to transfer kickstart records from {srcdir}/{rhost}", exception))
				)
			)
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
				tmpdir := dircat(wfdir, jobid)
				
				stdout := try(stdout, "stdout.txt")
				stderr := try(stderr, "stderr.txt")
				
				kickstart := vdl:kickstart(rhost)
				
				try(
					sequential(
						log(LOG:INFO, "Creating temporary directory {tmpdir} on {rhost}")
						dir:make(tmpdir, host=rhost)
					
						/* May need to consult RLS to find the physical file names for stagein and stageout. */
						
						fileDirs := fileDirs(stagein, stageout)
						
						createDirSet(sharedDir, rhost, fileDirs)
						
						doStagein(stagein, sharedDir, rhost)

						log(LOG:INFO, "Running job ", tr, maybe(" with arguments ", arguments), " in {tmpdir} on {rhost}")
																	
						task:execute("/bin/sh",
							list("shared/wrapper.sh", jobid, 
								stdout, stderr
								flatten(each(fileDirs)),
								flatten(each(stagein)), 
								flatten(outfiles(stageout)),
								kickstart,
								vdl:executable(tr, rhost),
								maybe(each(arguments)))
							directory=wfdir
							maybe(stdin="{jobid}/{stdin}")
							redirect = false
							host = rhost
							vdl:tcprofile(tr, rhost) //this gets various app params from the tc, such as environment, walltime, etc
						)
							
						checkExitCode(rhost, tmpdir, jobid, tr)
		
						log(LOG:INFO, "Completed job ", tr, maybe(" with arguments ", arguments), " on {rhost}")

									
						/* need to stage the files to upper scratch area in case they are not transfered to another site
						   before all the files get cleaned out */
					
						doStageout(stageout, sharedDir, rhost)
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, cleanupFiles(cacheFilesToRemove, rhost))
					)
					catch(".*"
						log(LOG:DEBUG, "Application exception: ", exception)
						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, cleanupFiles(cacheFilesToRemove, rhost))
						
						outs := transferSTDs(rhost, tmpdir, jobid, stdout, stderr)
						
						kickstartRec := if(
							kickstart == "" ""
							else(
								try(
									(
										recfile := "{jobid}-kickstart.xml"
										transferKickstartRec(rhost, tmpdir, recfile)
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
									"Arguments: {arguments}", nl(),
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
			gname := if(pgraph == "true" concat("graph-", uid(), ".dot") pgraph)
			file:write(gname
				"digraph SwiftProvenance {{", nl()
				"	graph [", vdl:configProperty("pgraph.graph.options"), "];", nl()
				"	node [", vdl:configProperty("pgraph.node.options"), "];", nl()
						
				for(i, gdata
					"	", i, nl()
				)
				"}", nl()
			)
			echo("Provenance graph saved in ", gname)
		)
	)
)