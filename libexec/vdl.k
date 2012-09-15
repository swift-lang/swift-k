import("sys.k")
import("task.k")
import("rlog.k")
import("operators.xml", export = true)

import("vdl-xs.k", export = true)

namespace("vdl"

	import("vdl-sc.k", export = true)
	import("vdl-lib.xml", export = true)

	pstaging := configProperty("use.provider.staging")
	wstaging := configProperty("use.wrapper.staging")
	int := if (
		pstaging == "true", "vdl-int-staging.k",
		wstaging == "true", "vdl-int-wrapper-staging.k", 
		"vdl-int.k"
	)

	import(int)
	import("java.k")

	once("vdl.k-print-version"
		log("info",sys:file:read("{swift.home}/libexec/version.txt"))
		echo(sys:file:read("{swift.home}/libexec/version.txt"))
		log("info","RUNID id=run:{VDL:RUNID}")
		echo("RunID: {VDL:RUNID}")
	)

	export("stagein", elementDef(classname="org.griphyn.vdl.karajan.lib.Stagein"))
	export("stageout", elementDef(classname="org.griphyn.vdl.karajan.lib.Stageout"))

	export(

		element(parameterlog, [direction, variable, id, thread],
			if(
				vdl:configProperty("provenance.log") == "true"
				log("info","PARAM thread={thread} direction={direction} variable={variable} provenanceid={id}")
			)
		)
		element(split, [var], each(str:split(vdl:getFieldValue(var), " ")))

		element(quote, [var, optional(path)],
			str:quote(vdl:getFieldValue(var, maybe(path = path)))
		)

		element(types, [])

		element(arguments, [...]
			arguments=expandArguments(each(...))
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

		element(mapping, [descriptor, ...]
			mapping=map(map:entry("swift#descriptor", descriptor), each(...))
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

    element(attributes, [attrs]
      attributes = attrs
    )

		element(mains, [channel(cleanup), channel(errors), channel(warnings)]
			anyerrors := vdl:processBulkErrors("The following errors have occurred:", errors)

			//this should be reached after everything is done
			if(
				sys:not(anyerrors) then(
					//hmm, you can append to channels!
					log(LOG:DEBUG, "Starting cleanups")
					append(warnings, from(warnings, cleanups(cleanup)))
					log(LOG:DEBUG, "Ending cleanups")
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
