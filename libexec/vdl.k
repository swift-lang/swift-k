import("sys.k")
import("task.k")
import("rlog.k")
import("operators.xml", export = true)

import("vdl-xs.k", export = true)

namespace("vdl"

	import("vdl-sc.k", export = true)
	import("vdl-lib.xml", export = true)

	import("vdl-int.k")
	import("java.k")

	once("vdl.k-print-version"
		log("info",sys:file:read("{vds.home}/libexec/version.txt"))
		echo(sys:file:read("{vds.home}/libexec/version.txt"))

		log("info","RUNID id=tag:benc@ci.uchicago.edu,2007:swift:run:{VDL:RUNID}")
		echo("RunID: {VDL:RUNID}")
	)

	export(

		element(parameterlog, [direction, variable, id],
			log("info","PARAM thread={#thread} direction={direction} variable={variable} provenanceid={id}")
		)

		element(split, [var], each(str:split(vdl:getFieldValue(var), " ")))

		element(quote, [var, optional(path)],
			str:quote(vdl:getFieldValue(var, maybe(path = path)))
		)

		element(types, [])

		element(arguments, [...]
			arguments=list(each(...))
		)

		export(execute
			executeFile(
				if(
					vdl:operation == "dryrun"
						"execute-dryrun.k"
					vdl:operation == "typecheck"
						"execute-typecheck.k"
					vdl:operation == "run"
						"execute-default.k"
				)
			)
		)

		element(stagein, [var]
			if(
				vdl:isFileBound(var) try(
					sequential(
						fp := vdl:fringePaths(var)
						try (
							for(path, fp
								vdl:waitFieldValue(path=path, var)
							)
// NOTE this string-based catch is messy, and needs to match with the string used in DataDependentException and VDLFunction
							catch(".*errors in data dependencies.*"
								log(LOG:DEBUG, exception)
								deperror = true
							)
						)
						channel:to(stagein,
							for(path, fp
								vdl:absFileName(vdl:getfield(var, path = path))
							)
						)
					)
					catch(".*not mapped.*"
						log(LOG:DEBUG, exception)
						mdeperror = true
						deperror = true
					)
				)
				else(
					//we still wait until the primitive value is there
					vdl:waitFieldValue(var)
				)
			)
		)

		element(stageout, [var]
			try(
				if(vdl:isFileBound(var) 
					channel:to(stageout,
						for(path, vdl:fringePaths(var)
							list(path, var)
						)
					)
				)
				catch(".*not mapped.*"
					log(LOG:DEBUG, exception)
					mdeperror = true
					deperror = true
				)
			)
			try(
				if(vdl:isRestartable(var) 
					channel:to(restartout,
						for(path, vdl:fringePaths(var)
							list(path, var)
						)
					)
				)
				catch(".*not mapped.*"
					log(LOG:DEBUG, exception)
					mdeperror = true
					deperror = true
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

		element(mains, [channel(cleanup), channel(errors), channel(warnings)]
			anyerrors := vdl:processBulkErrors("The following errors have occurred:", errors)

			//this should be reached after everything is done
			if(
				sys:not(anyerrors) then(
					//hmm, you can append to channels!
					append(warnings, from(warnings, cleanups(cleanup)))
				)
				else(
					log(LOG:INFO, "Errors detected. Cleanup not done.")
				)
			)
			anywarnings := vdl:processBulkErrors("The following warnings have occurred:", warnings, onStdout=true)
			if (anyerrors throw("Execution completed with errors"))
		)

		/*
		 * This would run in parallel with the workflow so that we don't keep
		 * all the data in memory until the workflow is done
		 */
		parallelElement(mainp, [channel(graph), channel(cleanup)]
			parallel(
				if(
					vdl:configProperty("pgraph") != "false" 
						generateProvenanceGraph(graph)
				)
				to(cleanup, unique(for(c, cleanup, c)))
			)
		)
	)
)
