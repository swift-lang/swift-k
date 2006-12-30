import("sys.k")
import("task.k")
import("rlog.k")

once("vdl.k-print-version"
	echo("VDL/Karajan library V 0.401")
)

import("vdl-xs.k", export = true)

namespace("vdl"
	import("vdl-sc.k", export = true)
	import("vdl-lib.xml", export = true)
	
	import("vdl-int.k")
	import("java.k")
	
	export(
	
		element(split, [var], each(str:split(vdl:getFieldValue(var), " ")))
	
		element(quote, [var, optional(path)],
			str:quote(vdl:getFieldValue(var, maybe(path = path)))
		)
	
		element(types, [])
		
		element(arguments, [...]
			arguments=list(each(...))
		)
				
		export(arg, 
			if (
				vdl:operation == "typecheck" | vdl:operation == "graph" element([name], "")
				elementDef(classname="org.griphyn.vdl.karajan.functions.FnArg")
			)
		)
		
		export(execute
			executeFile(
				if(
					vdl:operation == "typecheck"
						"execute-typecheck.k"
					vdl:operation == "run"
						"execute-default.k"	
				)
			)
		)
		
		element(stagein, [var]
			if(
				vdl:isDatasetBound(var) then(
					fp := vdl:fringePaths(var)
					try (
						for(path, fp
							discard(vdl:getFieldValue(path=path, var))
						)
						catch(".*dependent error.*"
							deperror = true
						)
					)
					channel:to(stagein,
						for(path, fp
							vdl:absFileName(path, var)
						)
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
		
		element(pre, [])
		element(post, [])
		
		element(mains, [channel(cleanup), channel(errors)]
			//this should be reached after everything is done
			parallelFor(i, cleanup
				[dir, host] := each(i)
				cleanup(dir, host)
			)
			
			anyerrors := vdl:processBulkErrors(errors)
			if (anyerrors throw("Execution completed with errors"))
		)

		/*
		 * This would run in parallel with the workflow so that we don't keep
		 * all the data in memory until the workflow is done
		 */
		parallelElement(mainp, [channel(graph)]
			if(
				vdl:configProperty("pgraph") != "false" 
					generateProvenanceGraph(graph)
			)
		)
	)
)
