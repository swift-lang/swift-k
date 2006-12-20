import("sys.k")
import("task.k")
import("rlog.k")

once("vdl.k-print-version"
	echo("VDL/Karajan library V 0.315")
)

import("vdl-xs.k", export = true)

namespace("vdl"
	import("vdl-sc.k", export = true)
	import("vdl-lib.k", export = true)
	import("java.k")
	global(vds.home, java:systemProperty("vds.home"))
	
	export(arg, 
		op := choice(first(cmdline:arguments), "run")
		if (
			op == "typecheck" | op == "graph"
				element([name], "")
			elementDef(classname="org.griphyn.vdl.karajan.functions.FnArg")
		)
	)
	
	export(quote, 
		element([var, optional(path)],
			str:quote(vdl:getFieldValue(var, maybe(path = path)))
		)
	)
	
	export(split, element([var], each(str:split(vdl:getFieldValue(var), " "))))
	
		
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

	export(
		element(types, [])
		
		element(arguments, [...]
			arguments=list(each(...))
		)
		
		element(createdirs, [path, dir, host]
			dc := dircat(dir, path)
			print("Creating directory structure {path} in {dir} ({dc})")
			dir:make(dc, host=host)
		)
				
		element(dircat, [dir, sub], 
			if (dir != "" concat(dir, "/", sub) sub)
		)
		
		export(execute
			op := choice(first(cmdline:arguments), "run")
			if(	
				op == "graph"
					element([tr, optional(arguments, stdin, stdout, stderr), channel(stagein), channel(stageout)]
						tmp := random()
						tp := vdl:threadPrefix()
						print("DOT: ", str:quote(tp), " [label={tr}]")
						print("GE: <node id=", str:quote("{tr}{tmp}"), " name=", str:quote("{tr}"), "/>")
						for(file, stagein
							print("DOT: ", str:quote(file), " [shape=parallelogram]")
							print("GE: <node id=", str:quote("{file}"), " name=", str:quote("{file}"), "/>")
							print("DOT: ", str:quote(file), " -> ", str:quote(tp))
							print("GE: <edge from=", str:quote("{file}"), " to=", str:quote(tp), "/>")
						)
						
						for(pv, stageout
							[path, var] := each(pv)
							file := vdl:filename(path, var)
							print("DOT: ", str:quote(file), " [shape=parallelogram]")
							print("GE: <node id=", str:quote("{file}"), " name=", str:quote("{file}"), "/>")
							print("DOT: ", str:quote(tp), " -> ", str:quote(file))
							print("GE: <edge from=", str:quote(tp), " to=", str:quote("{file}"), "/>")
						)
						
						mark(stageout)
					)
					
				op == "typecheck"
					element([tr, optional(arguments, stdin, stdout, stderr), channel(stagein), channel(stageout)]
						mark(stageout)
					)
					
				else(
					element([tr, optional(arguments, stdin, stdout, stderr), channel(stagein), channel(stageout)]
						/* may need to substitute with site selection 
						   meaning filter out sites first by looking at transformation catalog,
						   if the application is not present at a site, then it should not be
						   a candidate. Then we choose a site by some criteria.
						   
						   [m] This is done in the scheduler.
					    */
			
						done := isDone(stageout)
							
						if(
							not(done) restartOnError(".*", 2,
								execute2(
									tr, maybe(arguments=arguments), 
									maybe(stdin=stdin), maybe(stdout=stdout), maybe(stderr=stderr), 
									stagein, stageout
								)
							)
						)
			
						mark(stageout)
					)
				)
			)
		)
			
			
		element(isDone, [stageout]
			and(
				for(pv, stageout
					[path, var] := each(pv)
					vdl:isLogged(var, path)
				)
			)
		)
		
		element(mark, [stageout]
			for(pv, stageout
				[path, var] := each(pv)
				maybe(vdl:setFieldValue(var, true, path=path))
			)
		)
		
		element(execute2, [tr, optional(arguments, stdin, stdout, stderr), stagein, stageout]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))
			
			allocateHost(rhost, constraints=vdl:getjobconstraints(tr)
			
				[wfdir, sharedDir] := initSharedDir(rhost)
				
				jobdir := concat(tr, "-", uid())
				tmpdir := dircat(wfdir, jobdir)
				/*
					Add scratchdir to hosts
					Make the scheduler use those if no dir in task:exec()
				*/
				
				print("Creating temporary directory {tmpdir} on {rhost}")
				dir:make(tmpdir, host=rhost)
				
				choice(	
					sequential(
					
						/* May need to consult RLS to find the physical file names for stagein and stageout. */
						
						fileDirs := fileDirs(stagein, stageout)
						
						createDirSet(sharedDir, rhost, fileDirs)
						
						doStagein(stagein, sharedDir, rhost)
						
						print("Running job ", tr, maybe(" with arguments ", arguments), " in {tmpdir} on {rhost}")
																	
						task:execute("/bin/sh",
							list("shared/wrapper.sh", jobdir, 
								choice(stdout, "stdout.txt"), choice(stderr, "stderr.txt"),
								flatten(each(fileDirs)), flatten(each(stagein)), 
								flatten(outfiles(stageout)),
								vdl:executable(tr, rhost), maybe(each(arguments)))
							directory=wfdir
							maybe(stdin="{jobdir}/{stdin}")
							redirect = false
							host = rhost
							vdl:tcprofile(tr, rhost) //this gets various app params from the tc, such as environment, walltime, etc
						)
							
						checkExitCode(rhost, tmpdir, jobdir, tr)
	
						print("Completed job ", tr, maybe(" with arguments ", arguments), " on {rhost}")
										
						/* need to stage the files to upper scratch area in case they are not transfered to another site
						   before all the files get cleaned out */
						
						doStageout(stageout, sharedDir, rhost)
							
						//cleanup is done at the end of everything
						//cleanup(tmpdir, rhost)
					)
					sequential(
						outs := concat(
							for(f, ["stderr.txt", "stdout.txt"]
								if(
									file:exists("{tmpdir}/{f}", host=rhost) 
										then(
											task:transfer(srchost=rhost, srcdir=tmpdir, srcfile=f,
												destfile="{jobdir}-{f}")
	
											"{f}: ", file:read("{jobdir}-{f}")
											/*
											 * If multiple concurrent errors occur, 
											 * the files might not get deleted
											 */
											file:remove("{jobdir}-{f}")
										)
								)
							)
						)
						generateError(concat(outs, nl(), trace), maybe(exception))
					)
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
													
					generateError("Job {tr} failed with an exit code of {exitcode}")
				)
			)
		)
		
		element(initSharedDir, [rhost]
			once(list(rhost, "shared")
				wfdir := concat("run-", uid())
				sharedDir := dircat(wfdir, "shared")
				dir:make(sharedDir, host=rhost)
				transfer(srcdir="{vds.home}/libexec/", srcfile="wrapper.sh", destdir=sharedDir, desthost=rhost)
				wfdir, sharedDir
				//we send the cleanup data to vdl:main()
				to(cleanup, list(wfdir, rhost))
			)
		)
		
		element(doStagein, [files, dir, host]
			uParallelFor(file, files
				/* 
				 * Local files can be either relative or absolute
				 * Remote files however, must always be relative, and they
				 * go into the job directory
				 *
				 */
				[srcdir, destdir, filename] := (dirname(file), dircat(dir, reldirname(file)), basename(file))
				once(list(file, destdir, host)
					echo("Staging in {srcdir} {filename} to {destdir} on {host}")
					restartOnError(".*", 2
					    task:transfer(srcfile=filename, srcdir=srcdir,
						    desthost=host, destdir=destdir)
					)
					echo("Staged in {srcdir} {filename} to {destdir} on {host}")
				)
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
		
		element(doStageout, [stageouts, dir, host]
			uparallelFor(pv, stageouts
				[path, var] := each(pv)
				file := vdl:absfilename(path, var)
				rdir := dircat(dir, reldirname(file))
				bname := basename(file)
				ldir := dirname(file)
				fullLocal := dircat(ldir, bname)
					
				print("Staging out ", dircat(rdir, bname), " to {fullLocal} from {host}")
				//make sure we do have the directory on the client side
				dir:make(ldir)
				restartOnError(".*", 2
				    task:transfer(srchost=host, srcfile=bname, 
				        srcdir=rdir, destdir=ldir)
				)
                vdl:logvar(var, path)
                print("Staged out ", dircat(rdir, bname), " to {fullLocal} from {host}")
				
				once(list(bname, rdir, host))//just sayin' that it's there
			)
		)
		
		element(cleanup, [dir, host]
			print("Cleaning up ", dir, " on ", host)
			task:execute("/bin/rm", arguments="-rf {dir}", host=host)
		)
		
		element(stagein, [var]
			if(vdl:isDatasetBound(var)
				channel:to(stagein,
					for(path, vdl:fringePaths(var)
						discard(vdl:getFieldValue(path=path, var))
						vdl:absFileName(path, var)
					)
				)
			)
		)
		
		element(stageout, [var]
			if(vdl:isDatasetBound(var) 
				channel:to(stageout,
					for(path, vdl:fringePaths(var)
						list(path, var)
					)
				)
			)
		)
		
		element(mapping, [descriptor, ...]
			mapping=map(map:entry("descriptor", descriptor), each(...))
		)
		
		element(parameter, [name, value]
			map:entry(name, value)
		)
				
		element(stdout, [file]
			stdout = file
		)
		
		element(stdin, [file]
			stdin = file
		)
		
		element(stderr, [file]
			stderr = file
		)
		
		element(tr, [name]
			tr = name
		)
		
		element(main, [channel(cleanup)]
			//this should be reached after the everything is done
			parallelFor(i, cleanup
				[dir, host] := each(i)
				cleanup(dir, host)
			)
		)
	)
)
