import(sys)
import(task)
import('swift-lib')
/*
 * Things that are not exposed to the translated file
 */
 
SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:DEBUG_DIR_PREFIX := contextAttribute("SWIFT:DEBUG_DIR_PREFIX")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")

namespace(swift) {

	rmdir := function(dir, host) {
		parallelFor(entry, file:list(dir, host=host)) {
			epath := "{dir}/{entry}"
			if (file:isDirectory(epath, host=host)) {
				rmdir(epath, host)
			}
			else {
				file:remove(epath, host=host)
			}
		}
		dir:remove(dir, host=host)
	}

	createdirs := function(path, dir, host) {
		dc := dircat(dir, path)
		log(LOG:INFO, "START path={path} dir={dir} - Creating directory structure")

		dir:make(dc, host=host)
	}
	
	checkErrorFile := function(rhost, wfdir, jobid, jobdir) {
		if (file:exists("{wfdir}/status/{jobdir}/{jobid}-error", host=rhost)) {
			log(LOG:INFO, "FAILURE jobid={jobid} - Failure file found")
			task:transfer("{jobid}-error", srchost=rhost, srcdir="{wfdir}/status/{jobdir}")
			error := parallel(
				file:remove("{wfdir}/status/{jobdir}/{jobid}-error", host=rhost)
				sequential(
					str:strip(file:read("{jobid}-error"))
					file:remove("{jobid}-error")
				)
			)
			error
		}
		else {
			log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Error file missing")
			throw("No status file was found. Check the shared filesystem on {rhost}")
		}
	}

	checkJobStatus := function(rhost, wfdir, jobid, tr, jobdir) {
		log(LOG:DEBUG, "START jobid={jobid}")
		try {
			file:remove("{wfdir}/status/{jobdir}/{jobid}-success", host=rhost)
			log(LOG:INFO, "SUCCESS jobid={jobid} - Success file found")
		}
		else {
			msg := try {
				checkErrorFile(rhost, wfdir, jobid, jobdir)
			}
			else {
				log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Both status files are missing")
				throw("No status file was found. Check the shared filesystem on {rhost}")
			}
			throw(msg)
		}
	}	
	
	initSharedDir := function(progress, rhost) {
		once(list(rhost, "shared")) {
			setProgress(progress, "Initializing site shared directory")
			log(LOG:INFO, "START host={rhost} - Initializing shared directory")

			wfdir := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
			sharedDir := dircat(wfdir, "shared")

			dir:make(sharedDir, host = rhost)
			transfer(siteProfile(rhost, "swift:wrapperScript"), srcdir="{SWIFT:HOME}/libexec/", destdir=sharedDir, desthost=rhost)
			transfer("_swiftseq", srcdir="{SWIFT:HOME}/libexec/", destdir=sharedDir, desthost=rhost)
			dir:make(dircat(wfdir, "kickstart"), host=rhost)

			statusMode := configProperty("status.mode",host=rhost)
			if (statusMode == "files") {
				dir:make(dircat(wfdir, "status"), host=rhost)
			}

			wrapperMode := configProperty("wrapper.parameter.mode",host=rhost)
			if (wrapperMode == "files") {
				dir:make(dircat(wfdir, "parameters"), host=rhost)
			}

			dir:make(dircat(wfdir, "info"), host=rhost)
			wfdir, sharedDir
			//we send the cleanup data to vdl:main()
			to(cleanup, list(wfdir, rhost))
			log(LOG:INFO, "END host={rhost} - Done initializing shared directory")
		}
	}

	initDDir := function() {
		ddir := "{SWIFT:DEBUG_DIR_PREFIX}{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}.d"
		
		once(ddir) {
			if(!file:exists(ddir)) {
				task:dir:make(ddir)
			}
		}
		ddir
	}

	inFiles := function(stageins) {
		pathnames(stageins)
	}

	fileDirs := function(stageins, stageouts) {
		list(
			unique(
				inFileDirs(stageins)
				outFileDirs(stageouts)
			)
		)
	}

	createDirSet := function(jobid, destdir, host, dirs) {
		/*
		 * Ideally this would be done by creating a tree of the directories
		 * to be created and (eventually) exploiting the concurrency in that.
		 */
		log(LOG:INFO, "START jobid={jobid} host={host} - Initializing directory structure")
		for(u, dirs) {
			cache(list(u, destdir, host)) {
				createdirs(u, destdir, host)
			}
		}
		log(LOG:INFO, "END jobid={jobid} - Done initializing directory structure")
	}

	cleanup := function(dir, host) {
		log(LOG:INFO, "START dir={dir} host={host}")
		cdmfile := cdm:file()
		log(LOG:DEBUG, "cdmfile {cdmfile}")
		if (cdmfile != "" & cdm:get("GATHER_DIR") != "UNSET") {
			log(LOG:INFO, "submitting cdm_cleanup.sh to {dir}")
			task:transfer("cdm_cleanup.sh",
				srcdir="{SWIFT:HOME}/libexec",
				desthost=host, destdir=dir)
			task:transfer("cdm_lib.sh",
				srcdir="{SWIFT:HOME}/libexec",
				desthost=host, destdir=dir)
		        log(LOG:INFO, "execute: cdm_cleanup.sh")
			task:execute("/bin/bash", list("{dir}/cdm_cleanup.sh", cdm:get("GATHER_DIR"), cdm:get("GATHER_TARGET"), UID())
				host=host, batch=true, TCProfile(host))
		}
		if (swift:configProperty("sitedir.keep") == "false") {
			task:execute(siteProfile(host, "swift:cleanupCommand"),
				list(siteProfile(host, "swift:cleanupCommandOptions"), dir)
				host=host, batch=true, TCProfile(host))
		}
		log(LOG:INFO, "END dir={dir} host={host}")
	}

	cleanupFiles := function(files, host) {
		parallelFor(r, files) {
			log(LOG:INFO, "Purging ", r, " on ", host)
			file:remove(r, host=host)
			cacheFileRemoved(r, host)
		}
	}

	stageWrapperParams := function(jobid, jobdir, wrapfile, dir, host) {
		log(LOG:INFO, "START jobid={jobid} - staging in wrapper params")
		provider := provider(wrapfile)
		srchost := hostname(wrapfile)
		srcdir := swift:dirname(wrapfile)
		destdir := dircat(dir, "/parameters/{jobdir}/")
		filename := basename(wrapfile)

		cache(list(destdir, host)) {
			dir:make(destdir, host=host, provider=provider)
		}

		task:transfer(filename, srcprovider=provider, srchost=srchost, srcdir=srcdir, desthost=host, destdir=destdir)
		log(LOG:INFO, "END jobid={jobid}")
	}
	
	doStageinFile := function(provider, srchost, srcfile, srcdir, desthost, destdir, size, policy) {
		cacheAddAndLockFile(srcfile, destdir, desthost, size) {
			cleanupFiles(cacheFilesToRemove, desthost)

			log(LOG:DEBUG, "FILE_STAGE_IN_START file={srcfile} ",
				"srchost={srchost} srcdir={srcdir} srcname={srcfile} ",
				"desthost={desthost} destdir={destdir} provider={provider} ",
				"policy={policy}")
			if (policy == "DEFAULT") {
				restartOnError(2) {
					task:transfer(srcfile, srcprovider=provider, srchost=srchost,
						srcdir=srcdir, desthost=desthost, destdir=destdir,
						transferParams(srchost, desthost, provider))
				}
			}
			else if (policy == "BROADCAST") {
				log(LOG:DEBUG, "FILE_STAGE_IN_BROADCAST file={srcfile} policy={policy}")
				cdm:broadcast(srcfile, srcdir)
			}
			else if (policy == "EXTERNAL") {
				log(LOG:DEBUG, "FILE_STAGE_IN_EXTERNAL file={srcfile} policy={policy}")
				cdm:external(srcfile, srcdir, desthost, destdir)
			}
			else {
				log(LOG:DEBUG, "FILE_STAGE_IN_SKIP file={srcfile} policy={policy}")
			}
			log(LOG:DEBUG, "FILE_STAGE_IN_END file={srcfile} ",
				"srchost={srchost} srcdir={srcdir} srcname={srcfile} ",
				"desthost={desthost} destdir={destdir} provider={provider}")
		}
		cdm:wait()
    }

	doStagein := function(jobid, files, dir, host) {
                log(LOG:INFO, "START jobid={jobid} - Staging in files {files}")

		cdmfile := cdm:file()
		libexec := "{SWIFT:HOME}/libexec"

		if (cdmfile != "") {
			doStageinFile("file", "localhost", basename(cdmfile),
				swift:dirname(cdmfile), host, dir,
				file:size(cdmfile), "DEFAULT")
			doStageinFile("file", "localhost", "cdm.pl",
				libexec, host, dir,
				file:size("{libexec}/cdm.pl}"), "DEFAULT")
			doStageinFile("file", "localhost", "cdm_lib.sh",
				libexec, host, dir,
				file:size("{libexec}/cdm_lib.sh}"), "DEFAULT")
		}

		parallelFor(file, files) {
			provider := provider(file)
			srchost := hostname(file)
			srcdir := swift:dirname(file)
			destdir := dircat(dir, reldirname(file))
			filename := basename(file)
			size := file:size("{srcdir}/{filename}", host=srchost, provider=provider)

			policy := cdm:query(file)
			log(LOG:DEBUG, "CDM: {file} : {policy}")

			doStageinFile(provider, srchost, filename,
					srcdir, host, destdir, size, policy)
		}
		log(LOG:INFO, "END jobid={jobid} - Staging in finished")
	}
	
	doStageout := function(jobid, stageouts, dir, host) {
		log(LOG:INFO, "START jobid={jobid} - Staging out files")
		log(LOG:DEBUG, "stageouts: {stageouts}")
		done := list(
			parallelFor(pv, stageouts) {
				(path, var) := each(pv)
				file := absFileName(getField(var, path))
				provider := provider(file)
				dhost := hostname(file)
				rdir := dircat(dir, reldirname(file))
				bname := basename(file)
				ldir := swift:dirname(file)
				fullLocal := dircat(ldir, bname)
				fullRemote := dircat(rdir, bname)

                                log(LOG:DEBUG, "FILE_STAGE_OUT_START srcname={bname} srcdir={rdir} srchost={host} ",
                                        "destdir={ldir} desthost={dhost} provider={provider} jobid={jobid}")
				//make sure we do have the directory on the client side
				dir:make(ldir, host=dhost, provider=provider)
				policy := cdm:query(file)
				log(LOG:DEBUG, "CDM: {file} : {policy}")
				if ((policy == "DEFAULT") | (policy == "BROADCAST")) {
					restartOnError(2) {
						task:transfer(bname, srchost=host, srcdir=rdir,
							destdir=ldir, desthost=dhost, destprovider=provider,
							transferParams(host, dhost, provider))
					}
				}
				else {
					log(LOG:DEBUG, "FILE_STAGE_OUT_SKIP srcname={bname}")
				}
				log(LOG:DEBUG, "FILE_STAGE_OUT_END srcname={bname} srcdir={rdir} srchost={host} ",
					"destdir={ldir} desthost={dhost} provider={provider}")

				list(bname, rdir, host, file:size(fullLocal))
			}
		)
		parallelFor(f, done) {
			(bname, rdir, host, size) := each(f)
			cacheAddFile(bname, rdir, host, size, cleanupFiles(cacheFilesToRemove, host))
		}
		log(LOG:INFO, "END jobid={jobid} - Staging out finished")
	}

	fileSizes := function(files) {
		math:sum(
			for(f, files, file:size(f))
		)
	}

	transferStandardFiles := function(rhost, tmpdir, jobid, stdout, stderr) {
		concat(
			for(f, list(list("stderr.txt", stderr), list("stdout.txt", stdout))) {
				(name, file) := each(f)
				destfile := concat("{jobid}-", basename(file))
				try {
					task:transfer(file, srchost=rhost, srcdir=tmpdir, destfile=destfile)
					"\n    {name}: "
					strip(file:read(destfile))
				}
				else {
					""
				}
				maybe(file:remove(destfile))
			}
		)
	}


	transferWrapperLog := function(rhost, wfdir, jobid, jobdir) {
		recfile := "{jobid}-info"
		srcdir := dircat("{wfdir}/info/", jobdir)
		try {
			task:transfer(recfile, srchost=rhost, srcdir=srcdir, destdir="{SWIFT:DEBUG_DIR_PREFIX}{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}.d/")
		}
		else catch (exception) {
			maybe(file:remove(recfile))
			log(LOG:INFO, "Failed to transfer wrapper log for job {jobid} from {srcdir} on {rhost}", exception)
		}
		recfile
	}
	
	export(cleanups,
		function(cleanup) {
			log(LOG:INFO, "START cleanups={cleanup}")
			parallelFor(i, cleanup) {
				(dir, host) := each(i)
				try {
					cleanup(dir, host)
				}
				else catch(exception) {
					log(LOG:DEBUG, "EXCEPTION - Exception caught while cleaning up", exception)
					to(warnings, exception("Cleanup on {host} failed", exception))
				}
			}
			log(LOG:INFO, "END cleanups={cleanup}")
		}
	)

	export(execute2,
		function(progress, tr, stagein, stageout,  restartout
			replicationGroup, replicationChannel
			arguments = [], stdin = null, stdout = null, stderr = null, attributes = null) {
			
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))

			allocateHost(rhost, constraints = jobConstraints(tr, stagein = stagein)) {
				ddir := initDDir(),
				(wfdir, sharedDir) := 
					try {
						initSharedDir(progress, rhost)
					}
					else catch(exception) {
						throw(exception("Could not initialize shared directory on {rhost}", exception))
					}
					
				uid := UID()
				jobdir := substring(uid, 0, to=1)
				jobid := "{tr}-{uid}"

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")

				statusMode := configProperty("status.mode",host=rhost)
				wrapperMode := configProperty("wrapper.parameter.mode",host=rhost)

				wrapfile := "{ddir}/param-{jobid}"

				stdout := if (stdout == null, "stdout.txt", getFieldValue(stdout))
				stderr := if (stderr == null, "stderr.txt", getFieldValue(stderr))

				fileDirs := fileDirs(stagein, stageout)
				os := siteProfile(rhost, "SYSINFO:OS")
				
				if(wrapperMode == "files") {
					sys:file:write(wrapfile) {
						"-scratch ", try(siteProfile(rhost, "scratch"), ""),
						"\n-e ", executable(tr, rhost),
						"\n-out ", stdout,
						"\n-err ", stderr,
						"\n-i ", if (stdin != null, getFieldValue(stdin)),
						"\n-d ", flatten(each(fileDirs)),
						"\n-if ", flatten(inFiles(stagein)),
						"\n-of ", flatten(outFiles(stageout)),
						"\n-cdmfile ", cdm:file(),
						"\n-status ", statusMode,
						for(a, arguments) {
							"\n-a ", a
						}
					}
				}

				setProgress(progress, "Stage in")
				tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid)

				try {
					createDirSet(jobid, sharedDir, rhost, fileDirs)
					doStagein(jobid, stagein, sharedDir, rhost)
					if(wrapperMode == "files") {
						stageWrapperParams(jobid, jobdir, wrapfile, wfdir, rhost)
					}

					log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", arguments)), " tmpdir={tmpdir} host={rhost}")

					setProgress(progress, "Submitting")

					if (wrapperMode == "files") {
						swift:execute(
							progress,
							siteProfile(rhost, "swift:wrapperInterpreter"),
							list(
								siteProfile(rhost, "swift:wrapperInterpreterOptions"),
								dircat("shared", siteProfile(rhost, "swift:wrapperScript"), os=os),
								jobid, "-p", jobdir
							)
							directory = wfdir
							redirect = false
							host = rhost
							TCProfile(rhost, if (attributes != null, attributes = attributes), tr = tr)
							replicationGroup = replicationGroup
							replicationChannel = replicationChannel
							jobid = jobid
						)
					}
					else if (wrapperMode == "args") {
						swift:execute(
							progress,
							siteProfile(rhost, "swift:wrapperInterpreter"),
							list(
								siteProfile(rhost, "swift:wrapperInterpreterOptions"),
								dircat("shared", siteProfile(rhost, "swift:wrapperScript"), os=os),
								jobid,
								"-jobdir", jobdir,
								"-scratch", try(siteProfile(rhost, "scratch"), "")
								"-e", executable(tr, rhost),
								"-out", stdout,
								"-err", stderr,
								"-i", if (stdin != null, getFieldValue(stdin)),
								"-d", flatten(each(fileDirs)),
								"-if", flatten(inFiles(stagein)),
								"-of", flatten(outFiles(stageout)),
								"-cdmfile", cdm:file(),
								"-status", statusMode,
								"-a", if (arguments != null, each(arguments))
							)
							directory = wfdir
							redirect = false
							host = rhost
							TCProfile(rhost, if(attributes != null, attributes = attributes), tr = tr)
							replicationGroup = replicationGroup
							replicationChannel = replicationChannel
							jobid = jobid
						)
					}

					setProgress(progress, "Checking status")
					if (statusMode == "files") {
						checkJobStatus(rhost, wfdir, jobid, tr, jobdir)
					}

					if (wrapperMode == "files") {
						file:remove(wrapfile)
					}

					log(LOG:DEBUG, "STAGING_OUT jobid={jobid}")


					/* need to stage the files to upper scratch area in case they are not transfered to another site
					   before all the files get cleaned out */


					setProgress(progress, "Stage out")
					doStageout(jobid, stageout, sharedDir, rhost)
					doRestartLog(restartout)
					
					if (configProperty("wrapperlog.always.transfer") == "true") {
						discard(transferWrapperLog(rhost, wfdir, jobid, jobdir))
					}
					
					cacheUnlockFiles(stagein, sharedDir, rhost) {
						cleanupFiles(cacheFilesToRemove, rhost)
					}
						
					log(LOG:DEBUG, "JOB_END jobid={jobid}")
				}
				else catch(prev) {
					if (matches(prev, "^Abort$")) {
						log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
						cacheUnlockFiles(stagein, sharedDir, rhost, force=false) {
							cleanupFiles(cacheFilesToRemove, rhost)
						}
						throw(prev)
					}
					else {
						setProgress(progress, "Failed but can retry")
						exception := try(exception(checkErrorFile(rhost, wfdir, jobid, jobdir), prev), prev)
						
						log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

						if (matches(exception,".*executable bit.*")) {
							throw(exception)
						}

						cacheUnlockFiles(stagein, sharedDir, rhost, force=false) {
							cleanupFiles(cacheFilesToRemove, rhost)
						}

						outs := transferStandardFiles(rhost, tmpdir, jobid, stdout, stderr)

						discard(maybe(transferWrapperLog(rhost, wfdir, jobid, jobdir)))

					
						throw(
							exception(
								concat(
									"Exception in {tr}:",
									if (arguments != null, "\n    Arguments: {arguments}")
									"\n    Host: {rhost}",
									"\n    Directory: {tmpdir}",
									"{outs}",
								)
								exception
							)
						)
					}
				}
			}
		}
	)
}

// Local variables:
// mode: scheme
// tab-width: 4
// indent-tabs-mode: t
// End:
