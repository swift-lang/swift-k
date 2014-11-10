import(sys)
import(task)
import('swift-lib')
/*
 * Things that are not exposed to the translated file
 */
 
SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")
SWIFT:DEBUG_DIR_PREFIX := contextAttribute("SWIFT:DEBUG_DIR_PREFIX")

RUN_DIR := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
SHARED_DIR := dircat(RUN_DIR, "shared")
DEBUG_DIR := "{SWIFT:DEBUG_DIR_PREFIX}{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}.d"

WRAPPER_LOG_ALWAYS_TRANSFER := configProperty("alwaysTransferWrapperLog")

if (!file:exists(DEBUG_DIR)) {
	task:dir:make(DEBUG_DIR)
}

namespace(swiftStaging) {

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
	
	checkErrorFile := function(rhost, jobid, jobdir) {
		if (file:exists("{RUN_DIR}/status/{jobdir}/{jobid}-error", host=rhost)) {
			log(LOG:INFO, "FAILURE jobid={jobid} - Failure file found")
			task:transfer("{jobid}-error", srchost=rhost, srcdir="{RUN_DIR}/status/{jobdir}")
			error := parallel(
				file:remove("{RUN_DIR}/status/{jobdir}/{jobid}-error", host=rhost)
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

	checkJobStatus := function(rhost, jobid, tr, jobdir) {
		log(LOG:DEBUG, "START jobid={jobid}")
		try {
			file:remove("{RUN_DIR}/status/{jobdir}/{jobid}-success", host=rhost)
			log(LOG:INFO, "SUCCESS jobid={jobid} - Success file found")
		}
		else {
			msg := try {
				checkErrorFile(rhost, jobid, jobdir)
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

			dir:make(SHARED_DIR, host = rhost)
			transfer(siteProfile(rhost, "wrapperScript"), srcdir="{SWIFT:HOME}/libexec/", destdir=SHARED_DIR, desthost=rhost)

			statusMode := configProperty("statusMode", host=rhost)
			if (statusMode == "files") {
				dir:make(dircat(RUN_DIR, "status"), host=rhost)
			}

			wrapperMode := configProperty("wrapperParameterMode", host=rhost)
			if (wrapperMode == "files") {
				dir:make(dircat(RUN_DIR, "parameters"), host=rhost)
			}

			dir:make(dircat(RUN_DIR, "info"), host=rhost)

			//we send the cleanup data to vdl:main()
			to(cleanup, list(RUN_DIR, rhost))
			log(LOG:INFO, "END host={rhost} - Done initializing shared directory")
		}
	}

	createDirSet := function(jobid, destdir, host, dirs) {
		/*
		 * Ideally this would be done by creating a tree of the directories
		 * to be created and (eventually) exploiting the concurrency in that.
		 */
		log(LOG:INFO, "START jobid={jobid} host={host} - Initializing directory structure")
		for(path, dirs) {
			path := remoteFileName(path)
			cache(list(path, destdir, host)) {
				dc := dircat(destdir, path)
				log(LOG:INFO, "START path={path} dir={destdir} - Creating directory structure")

				dir:make(dc, host=host)
			}
		}
		log(LOG:INFO, "END jobid={jobid} - Done initializing directory structure")
	}

	cleanupFiles := function(files, host) {
		parallelFor(r, files) {
			log(LOG:INFO, "Purging ", r, " on ", host)
			file:remove(r, host=host)
			cacheFileRemoved(r, host)
		}
	}

	stageWrapperParams := function(jobid, jobdir, wrapfile, dir, host) {
		log(LOG:INFO, "START jobid={jobid} - staging in wrapper params"),		
		(provider, srchost, destdir, filename, srcdir) := splitFileURL(wrapfile, dir, destdir="parameters/{jobdir}")

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

	doStagein := function(jobid, dir, host, files) {
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
			(provider, srchost, destdir, filename, srcdir) := splitFileURL(file, dir)
			size := file:size("{srcdir}/{filename}", host=srchost, provider=provider)

			policy := cdm:query(file)
			log(LOG:DEBUG, "CDM: {file} : {policy}")

			doStageinFile(provider, srchost, filename,
					srcdir, host, destdir, size, policy)
		}
		log(LOG:INFO, "END jobid={jobid} - Staging in finished")
	}
	
	doStageout := function(jobid, dir, host, files) {
		log(LOG:INFO, "START jobid={jobid} - Staging out files")
		log(LOG:DEBUG, "stageouts: {files}")
		done := list(
			parallelFor(file, files) {
				(provider, dhost, rdir, bname, ldir) := splitFileURL(file, dir)
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
	
	doStageoutCollect := function(jobid, dir, host, outFiles) {
		fileList := "{jobid}-collect"
		task:transfer(fileList, srchost=host, srcdir=dir)
		collectList := readCollectList(fileList)
		doStageout(jobid, dir, host, 
			list:join(outFiles, collectList))
		collectList
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


	transferWrapperLog := function(rhost, jobid, jobdir) {
		recfile := "{jobid}-info"
		srcdir := dircat("{RUN_DIR}/info/", jobdir)
		try {
			task:transfer(recfile, srchost=rhost, srcdir=srcdir, destdir=DEBUG_DIR)
		}
		else catch (exception) {
			maybe(file:remove(recfile))
			log(LOG:INFO, "Failed to transfer wrapper log for job {jobid} from {srcdir} on {rhost}", exception)
		}
		recfile
	}

	export(execute2,
		function(jobid, rhost, progress, tr, arguments, attributes, stdin, stdout, stderr, 
			stagein, stageout, replicationGroup, replicationChannel) {

			try {
				initSharedDir(progress, rhost)
			}
			else catch(exception) {
				throw(exception("Could not initialize shared directory on {rhost}", exception))
			},
				
			jobdir := substring(jobid, 0, to = 1)

			log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")

			statusMode := configProperty("statusMode", host=rhost)
			wrapperMode := configProperty("wrapperParameterMode", host=rhost)

			wrapfile := "{DEBUG_DIR}/param-{jobid}"
						
			(fileDirs, inFiles, outFiles, outCollect) := getStagingInfo(stagein, stageout),
			(stdins, stdouts, stderrs) := getStandardFilesInfo(stdin, stdout, stderr)

			os := siteProfile(rhost, "OS")
			eargs := expandArguments(arguments)
			
			if (wrapperMode == "files") {
				sys:file:write(wrapfile) {
					"-scratch ", try(siteProfile(rhost, "scratch"), ""),
					"\n-e ", executable(tr, rhost),
					"\n-out ", stdouts,
					"\n-err ", stderrs,
					"\n-i ", if (stdin != null, stdins),
					"\n-d ", remoteFileNames(fileDirs),
					"\n-if ", remoteFileNames(inFiles),
					"\n-of ", remoteFileNames(outFiles),
					"\n-cf ", remoteFileNames(outCollect),
					"\n-cdmfile ", cdm:file(),
					"\n-status ", statusMode,
					for(a, eargs) {
						"\n-a ", a
					}
				}
			}

			setProgress(progress, "Stage in")
			tmpdir := dircat("{RUN_DIR}/jobs/{jobdir}", jobid)

			try {
				createDirSet(jobid, SHARED_DIR, rhost, fileDirs)
				doStagein(jobid, SHARED_DIR, rhost, inFiles)
				if (wrapperMode == "files") {
					stageWrapperParams(jobid, jobdir, wrapfile, RUN_DIR, rhost)
				}

				log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", arguments)), " tmpdir={tmpdir} host={rhost}")

				setProgress(progress, "Submitting")

				if (wrapperMode == "files") {
					swift:execute(
						progress,
						siteProfile(rhost, "wrapperInterpreter"),
						list(
							siteProfile(rhost, "wrapperInterpreterOptions"),
							dircat("shared", siteProfile(rhost, "wrapperScript"), os=os),
							jobid, "-p", jobdir
						)
						directory = RUN_DIR
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
						siteProfile(rhost, "wrapperInterpreter"),
						list(
							siteProfile(rhost, "wrapperInterpreterOptions"),
							dircat("shared", siteProfile(rhost, "wrapperScript"), os=os),
							jobid,
							"-jobdir", jobdir,
							"-scratch", try(siteProfile(rhost, "scratch"), "")
							"-e", executable(tr, rhost),
							"-out", stdouts,
							"-err", stderrs,
							"-i", if (stdin != null, stdins),
							"-d", remoteFileNames(fileDirs),
							"-if", remoteFileNames(inFiles),
							"-of", remoteFileNames(outFiles),
							"-cf", remoteFileNames(outCollect),
							"-cdmfile", cdm:file(),
							"-status", statusMode,
							"-a", if (arguments != null, each(eargs))
						)
						directory = RUN_DIR
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
					checkJobStatus(rhost, jobid, tr, jobdir)
				}

				if (wrapperMode == "files") {
					file:remove(wrapfile)
				}

				log(LOG:DEBUG, "STAGING_OUT jobid={jobid}")


				/* need to stage the files to upper scratch area in case they are not transfered to another site
				   before all the files get cleaned out */


				setProgress(progress, "Stage out")
				if (isEmpty(outCollect)) {
					doStageout(jobid, SHARED_DIR, rhost, outFiles)
					[] // empty collect list
				}
				else {
					doStageoutCollect(jobid, SHARED_DIR, rhost, outFiles)
				}
				
				if (WRAPPER_LOG_ALWAYS_TRANSFER) {
					discard(transferWrapperLog(rhost, jobid, jobdir))
				}
				
				cacheUnlockFiles(inFiles, SHARED_DIR, rhost) {
					cleanupFiles(cacheFilesToRemove, rhost)
				}
					
				log(LOG:DEBUG, "JOB_END jobid={jobid}")
			}
			else catch(prev) {
				if (matches(prev, "^Abort$")) {
					log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
					cacheUnlockFiles(inFiles, SHARED_DIR, rhost, force=false) {
						cleanupFiles(cacheFilesToRemove, rhost)
					}
					throw(prev)
				}
				else {
					setProgress(progress, "Failed but can retry")
					exception := try(exception(checkErrorFile(rhost, jobid, jobdir), prev), prev)
					
					log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

					if (matches(exception,".*executable bit.*")) {
						throw(exception)
					}

					cacheUnlockFiles(inFiles, SHARED_DIR, rhost, force=false) {
						cleanupFiles(cacheFilesToRemove, rhost)
					}

					outs := transferStandardFiles(rhost, tmpdir, jobid, stdouts, stderrs)

					discard(maybe(transferWrapperLog(rhost, jobid, jobdir)))

				
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
	)
}

// Local variables:
// mode: scheme
// tab-width: 4
// indent-tabs-mode: t
// End:
