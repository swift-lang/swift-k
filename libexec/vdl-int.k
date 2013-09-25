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
				epath := "{dir}/{entry}"
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
				sequential(
					msg := try (
						checkErrorFile(rhost, wfdir, jobid, jobdir)
						sequential (
							log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Both status files are missing")
							throw("No status file was found. Check the shared filesystem on {rhost}")
						)
					)
					throw(msg)
				)
			)
		)
		
		element(checkErrorFile, [rhost, wfdir, jobid, jobdir]
			if (
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
					error
				)
				else (
					log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Error file missing")
					throw("No status file was found. Check the shared filesystem on {rhost}")
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
				transfer(srcdir="{swift.home}/libexec/", srcfile=vdl:siteprofile(rhost, "swift:wrapperScript"), destdir=sharedDir, desthost=rhost)
				transfer(srcdir="{swift.home}/libexec/", srcfile="_swiftseq", destdir=sharedDir, desthost=rhost)
				dir:make(dircat(wfdir, "kickstart"), host=rhost)

				statusMode := configProperty("status.mode",host=rhost)
				if(statusMode == "files"
					dir:make(dircat(wfdir, "status"), host=rhost)
				)

				wrapperMode := configProperty("wrapper.parameter.mode",host=rhost)
				if(wrapperMode == "files"
					dir:make(dircat(wfdir, "parameters"), host=rhost)
				)

				dir:make(dircat(wfdir, "info"), host=rhost)
				wfdir, sharedDir
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
			pathnames(stageins)
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
			cdmfile := cdm:file()
			log(LOG:DEBUG, "cdmfile {cdmfile}")
			if(cdmfile != "" &
                           cdm:get("GATHER_DIR") != "UNSET" then(
				log(LOG:INFO, "submitting cdm_cleanup.sh to {dir}")
				task:transfer(srcfile="cdm_cleanup.sh",
					      srcdir="{swift.home}/libexec",
					      desthost=host, destdir=dir)
				task:transfer(srcfile="cdm_lib.sh",
					      srcdir="{swift.home}/libexec",
					      desthost=host, destdir=dir)
			        log(LOG:INFO, "execute: cdm_cleanup.sh")
				task:execute(
					     executable="/bin/bash",
					     arguments=list("{dir}/cdm_cleanup.sh",
					                     cdm:get("GATHER_DIR"), cdm:get("GATHER_TARGET")
							     sys:uid() )
					     host=host, batch=true, tcprofile(host))
				)
			)
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

		element(cleanupFiles, [files, host]
			uParallelFor(r, files
				log(LOG:INFO, "Purging ", r, " on ", host)
				file:remove(r, host=host)
				vdl:cacheFileRemoved(r, host)
			)
		)

		element(stageWrapperParams, [jobid, jobdir, wrapfile, dir, host]
			log(LOG:INFO, "START jobid={jobid} - staging in wrapper params")
			provider := provider(wrapfile)
			srchost := hostname(wrapfile)
			srcdir := vdl:dirname(wrapfile)
			destdir := dircat(dir, "/parameters/{jobdir}/")
			filename := basename(wrapfile)

			cacheOn(list(destdir, host)
				dir:make(destdir, host=host, provider=provider)
			)

			task:transfer(srcprovider=provider, srchost=srchost, srcfile=filename, srcdir=srcdir, desthost=host, destdir=destdir)
			log(LOG:INFO, "END jobid={jobid}")
		)

		element(doStagein, [jobid, files, dir, host]
			log(LOG:INFO, "START jobid={jobid} - Staging in files")

			cdmfile := cdm:file()
			libexec := "{swift.home}/libexec"

			if (cdmfile != "" then(
				doStageinFile(provider="file", srchost="localhost", srcfile=basename(cdmfile),
					    	srcdir=vdl:dirname(cdmfile), desthost=host, destdir=dir,
						size=file:size(cdmfile), policy="DEFAULT")
				doStageinFile(provider="file", srchost="localhost", srcfile="cdm.pl",
					    	srcdir=libexec, desthost=host, destdir=dir,
						size=file:size("{libexec}/cdm.pl}"), policy="DEFAULT")
				doStageinFile(provider="file", srchost="localhost", srcfile="cdm_lib.sh",
					    	srcdir=libexec, desthost=host, destdir=dir,
						size=file:size("{libexec}/cdm_lib.sh}"), policy="DEFAULT")
			))

			uParallelFor(file, files
				provider := provider(file)
				srchost := hostname(file)
				srcdir := vdl:dirname(file)
				destdir := dircat(dir, reldirname(file))
				filename := basename(file)
				size := file:size("{srcdir}/{filename}", host=srchost, provider=provider)

				policy := cdm:query(query=file)
				log(LOG:DEBUG, "CDM: {file} : {policy}")

				doStageinFile(provider=provider, srchost=srchost, srcfile=filename,
						srcdir=srcdir, desthost=host, destdir=destdir, size=size, policy=policy)
			)
			log(LOG:INFO, "END jobid={jobid} - Staging in finished")
		)

		element(doStageinFile, [provider, srchost, srcfile, srcdir, desthost, destdir, size, policy]
			vdl:cacheAddAndLockFile(srcfile, destdir, desthost, size
				       cleanupFiles(cacheFilesToRemove, desthost)

			log(LOG:DEBUG, "FILE_STAGE_IN_START file={srcfile} ",
                                        "srchost={srchost} srcdir={srcdir} srcname={srcfile} ",
					"desthost={desthost} destdir={destdir} provider={provider} ",
                                        "policy={policy}")
			if (policy == "DEFAULT" then(
				restartOnError(".*", 2
					task:transfer(srcprovider=provider, srchost=srchost, srcfile=srcfile,
					              srcdir=srcdir, desthost=desthost, destdir=destdir,
					              transferParams(srchost, desthost, provider))))
			    policy == "BROADCAST" then(
				log(LOG:DEBUG, "FILE_STAGE_IN_BROADCAST file={srcfile} policy={policy}")
				cdm:broadcast(srcfile=srcfile, srcdir=srcdir))
			    policy == "EXTERNAL" then(
				log(LOG:DEBUG, "FILE_STAGE_IN_EXTERNAL file={srcfile} policy={policy}")
				cdm:external(srcfile=srcfile, srcdir=srcdir,
                                              desthost=desthost, destdir=destdir))
			else(log(LOG:DEBUG, "FILE_STAGE_IN_SKIP file={srcfile} policy={policy}")))
			log(LOG:DEBUG, "FILE_STAGE_IN_END file={srcfile} ",
					"srchost={srchost} srcdir={srcdir} srcname={srcfile} ",
                                        "desthost={desthost} destdir={destdir} provider={provider}")
			)
			cdm:wait()
 	        )

		element(doStageout, [jobid, stageouts, dir, host]
			log(LOG:INFO, "START jobid={jobid} - Staging out files")
                        log(LOG:DEBUG, "stageouts: {stageouts}")
			done := list(
				uParallelFor(pv, stageouts
					[path, var] := each(pv)
					file := vdl:absfilename(vdl:getfield(var, path = path))
					provider := vdl:provider(file)
					dhost := vdl:hostname(file)
					rdir := dircat(dir, reldirname(file))
					bname := basename(file)
					ldir := vdl:dirname(file)
					fullLocal := dircat(ldir, bname)
					fullRemote := dircat(rdir, bname)

					log(LOG:DEBUG, "FILE_STAGE_OUT_START srcname={bname} srcdir={rdir} srchost={host} ",
							"destdir={ldir} desthost={dhost} provider={provider}")
					//make sure we do have the directory on the client side
					dir:make(ldir, host=dhost, provider=provider)
					policy := cdm:query(query=file)
					log(LOG:DEBUG, "CDM: {file} : {policy}")
					if (sys:or(policy == "DEFAULT", policy == "BROADCAST")
						then(
						restartOnError(".*", 2
							task:transfer(srchost=host, srcfile=bname,srcdir=rdir,
								destdir=ldir, desthost=dhost, destprovider=provider,
								transferParams(host, dhost, provider))))
					else(log(LOG:DEBUG, "FILE_STAGE_OUT_SKIP srcname={bname}"))
					)
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

		element(transferStandardFiles, [rhost, tmpdir, jobid, stdout, stderr]
			concat(
				for(f, list(list("stderr.txt", stderr), list("stdout.txt", stdout))
					[name, file] := each(f)
					destfile := concat("{jobid}-", basename(file))
					try(
						sequential(
							task:transfer(srchost=rhost, srcdir=tmpdir, srcfile=file,
								destfile=destfile)
							nl()
							"    {name}: "
							strip(file:read(destfile))
						)
						""
					)
					maybe(file:remove(destfile))
				)
			)
		)

		element(transferKickstartRec, [rhost, wfdir, jobid, jobdir]
			recfile := "{jobid}-kickstart.xml"
			srcdir := dircat("{wfdir}/kickstart/", jobdir)
			try(
				task:transfer(srchost=rhost, srcdir=srcdir, srcfile=recfile, destdir="{VDL:SCRIPTNAME}-{VDL:RUNID}.d/")
				(
					maybe(file:remove(recfile))
					log(LOG:WARN, "Failed to transfer kickstart records from {srcdir} on {rhost}", exception)
				)
			)
			recfile
		)

		element(transferWrapperLog, [rhost, wfdir, jobid, jobdir]
			recfile := "{jobid}-info"
			srcdir := dircat("{wfdir}/info/", jobdir)
			try(
				task:transfer(srchost=rhost, srcdir=srcdir, srcfile=recfile, destdir="{VDL:SCRIPTNAME}-{VDL:RUNID}.d/")
				(
					maybe(file:remove(recfile))
					log(LOG:INFO, "Failed to transfer wrapper log for job {jobid} from {srcdir} on {rhost}", exception)
				)
			)
			recfile
		)

		element(execute2, [tr, optional(arguments, stdin, stdout, stderr, attributes), stagein, stageout,  restartout,
			replicationGroup, replicationChannel]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))

			allocateHost(rhost, constraints=vdl:jobConstraints(tr, stagein=stagein)

				ddir := initDDir()
				[wfdir, sharedDir] := try(
					initSharedDir(rhost)
					throw(exception("Could not initialize shared directory on {rhost}", exception))
				)

				uid := uid()
				jobdir := substring(uid, from=0, to=1)
				jobid := "{tr}-{uid}"

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread={#thread} host={rhost} replicationGroup={replicationGroup}")

				statusMode := configProperty("status.mode",host=rhost)
				wrapperMode := configProperty("wrapper.parameter.mode",host=rhost)

				wrapfile := "{ddir}/param-{jobid}"

				stdout := try(getFieldValue(stdout), "stdout.txt")
				stderr := try(getFieldValue(stderr), "stderr.txt")
				kickstart := vdl:kickstart(rhost)
				fileDirs := fileDirs(stagein, stageout)
				os := vdl:siteprofile(rhost, "SYSINFO:OS")

				if(wrapperMode == "files"
					sequential(
						sys:file:write(wrapfile,
							"-scratch ", try(vdl:siteprofile(rhost, "scratch"), ""), nl(),
							"-e ",vdl:executable(tr, rhost), nl(),
							"-out ", stdout, nl(),
							"-err ", stderr, nl(),
							"-i ", maybe(getFieldValue(stdin)), nl(),
							"-d ", flatten(each(fileDirs)), nl(),
							"-if ", flatten(infiles(stagein)), nl(),
							"-of ", flatten(outfiles(stageout)), nl(),
							"-k ", kickstart, nl(),
							"-cdmfile ", cdm:file(), nl(),
							"-status ", statusMode, nl(),
							for(a, arguments, "-a ", a, nl())
						)
					)
				)

				vdl:setprogress("Stage in")
				tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid)



				try(
					sequential(

						createDirSet(jobid, sharedDir, rhost, fileDirs)
						doStagein(jobid, stagein, sharedDir, rhost)
						if(wrapperMode == "files"
							stageWrapperParams(jobid, jobdir, wrapfile, wfdir, rhost)
						)


						log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", maybe(" arguments=", arguments), " tmpdir={tmpdir} host={rhost}")

						vdl:setprogress("Submitting")

						if(wrapperMode == "files"
							vdl:execute(
								vdl:siteprofile(rhost, "swift:wrapperInterpreter"),
								list(
									vdl:siteprofile(rhost, "swift:wrapperInterpreterOptions"),
									dircat("shared", vdl:siteprofile(rhost, "swift:wrapperScript"), os=os),
									jobid, "-p", jobdir
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
								dircat("shared", vdl:siteprofile(rhost, "swift:wrapperScript"), os=os),
								jobid,
								"-jobdir", jobdir,
								"-scratch", try(vdl:siteprofile(rhost, "scratch"), "")
								"-e", vdl:executable(tr, rhost),
								"-out", stdout,
								"-err", stderr,
								"-i", maybe(getFieldValue(stdin)),
								"-d", flatten(each(fileDirs)),
								"-if", flatten(infiles(stagein)),
								"-of", flatten(outfiles(stageout)),
								"-k", kickstart,
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
							checkJobStatus(rhost, wfdir, jobid, tr, jobdir)
						)

						if(wrapperMode == "files"
							file:remove(wrapfile)
						)

						log(LOG:DEBUG, "STAGING_OUT jobid={jobid}")


						/* need to stage the files to upper scratch area in case they are not transfered to another site
						   before all the files get cleaned out */


						vdl:setprogress("Stage out")
						doStageout(jobid, stageout, sharedDir, rhost)
						doRestartlog(restartout)
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
						prev := exception
						exception := try(exception(checkErrorFile(rhost, wfdir, jobid, jobdir)), prev)
						
						log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

						if(matches(exception,".*executable bit.*")
     					           generateError(exception)
						)

						vdl:cacheUnlockFiles(stagein, sharedDir, rhost, force=false
							cleanupFiles(cacheFilesToRemove, rhost)
						)

						outs := transferStandardFiles(rhost, tmpdir, jobid, stdout, stderr)

						discard(maybe(transferWrapperLog(rhost, wfdir, jobid, jobdir)))

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
									maybe("    Arguments: ", arguments, nl()),
									"    Host: {rhost}", nl(),
									"    Directory: {tmpdir}",
									"{outs}",
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

// Local variables:
// mode: scheme
// tab-width: 4
// indent-tabs-mode: t
// End:
