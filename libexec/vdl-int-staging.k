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

global(WRAPPER_TRANSFER_MODE,
	if(vdl:configProperty("wrapperlog.always.transfer") == "true", 
		STAGING_MODE:IF_PRESENT, STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT))
		
pinOption := configProperty("provider.staging.pin.swiftfiles")
global(PIN, if(pinOption == "true", "pinned:", ""))
global(PROVENANCE_GRAPH_ENABLED, vdl:configProperty("pgraph") != "false") 

namespace("vdl"
	export(
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

		element(graphStuff, [tr, stagein, stageout, err, optional(args)]
			if(PROVENANCE_GRAPH_ENABLED 
				then(
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
		
		element(readErrorFile, [dir, jobid]
			str:strip(file:read("{dir}/{jobid}.error"))
			file:remove("{dir}/{jobid}.error")
		)
		
		element(execute2, [tr, optional(arguments, stdin, stdout, stderr), stagein, stageout,  restartout,
			replicationGroup, replicationChannel]
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))

			allocateHost(rhost, constraints=vdl:jobConstraints(tr, stagein=stagein)

				ddir := initDDir()

				uid := uid()
				jobdir := substring(uid, from=0, to=1)
				jobid := "{tr}-{uid}"

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread={#thread} host={rhost} replicationGroup={replicationGroup}")

				wrapper := "_swiftwrap.staging"
				wrapfile := "{ddir}/param-{jobid}"

				stdout := try(getFieldValue(stdout), "stdout.txt")
				stderr := try(getFieldValue(stderr), "stderr.txt")

				wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
				tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid)
				cdmfile := cdm:file()

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
								"-i", maybe(getFieldValue(stdin)),
								"-d", flatten(unique(outFileDirs(stageout))),
								"-if", flatten(infiles(stagein)),
								"-of", flatten(outfiles(stageout)),
								"-k",
								"-cdmfile", cdmfile,
								"-status", "provider"
								"-a", maybe(each(arguments))
							)
							directory = tmpdir
							redirect = false
							host = rhost

							vdl:tcprofile(rhost, tr = tr) //this gets various app params from the tc, such as environment, walltime, etc
							replicationGroup = replicationGroup
							replicationChannel = replicationChannel
							jobid = jobid

							stagingMethod := vdl:siteProfile(rhost, "swift:stagingMethod", default="proxy")

							stageIn("{PIN}{stagingMethod}://localhost/{swift.home}/libexec/{wrapper}", wrapper)

							if ( cdmfile != "" then(
								d  := vdl:dirname(cdmfile)
								file := basename(cdmfile)
								dir := if ( d == "" then("./") else(str:concat(d,"/")))
								loc := "{PIN}{stagingMethod}://localhost/"
								stageIn("{loc}{dir}{file}", cdmfile)
								stageIn("{loc}{swift.home}/libexec/cdm.pl", "cdm.pl")
								stageIn("{loc}{swift.home}/libexec/cdm_lib.sh", "cdm_lib.sh")
							))

							appStageins(jobid, stagein, stagingMethod)

							stageOut("wrapper.log", "{stagingMethod}://localhost/{ddir}/{jobid}.info", 
								mode = WRAPPER_TRANSFER_MODE)
							//stageOut("{stdout}", "{stagingMethod}://localhost/{ddir}/{stdout}")
							//stageOut("{stderr}", "{stagingMethod}://localhost/{ddir}/{stderr}")
							stageOut("wrapper.error", "{stagingMethod}://localhost/{ddir}/{jobid}.error", 
								mode = STAGING_MODE:IF_PRESENT)
							appStageouts(jobid, stageout, stagingMethod)

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
						prev := exception
						exception := try(exception(readErrorFile(ddir, jobid)), prev)
						log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

						throw(
							exception(
								concat(
									"Exception in {tr}:", nl(),
									maybe("    Arguments: {arguments}", nl()),
									"    Host: {rhost}", nl(),
									"    Directory: {tmpdir}", nl()
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
// tab-width: 4
// indent-tabs-mode: 1
// End:
