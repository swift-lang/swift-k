cache(
	namespace("sys"
		import("sys-common.xml", export = true)
		export(set, elementDef(classname="org.globus.cog.karajan.workflow.nodes.SetVarK"))
		export(global, elementDef(classname="org.globus.cog.karajan.workflow.nodes.GlobalK"))
       	
		export(quoted, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.QuotedString"))
		export(__dot___dot___dot_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.VargsK"))
		export(optional, elementDef(classname="org.globus.cog.karajan.workflow.nodes.OptionalArgs"))
        
		export(__q_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.ConditionNode"))
	
		export(__amp_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Logic"))
		export(__pipe_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Logic"))
		export(__bang_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Logic"))
	
		namespace("math"
			export(__plus_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Sum"))
			export(__times_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__minus_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__fwslash_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__percent_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__gt_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__lt_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__gt___eq_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
			export(__lt___eq_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Math"))
		)
		//Let's be tolerant
		export(__eq___eq_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Misc"))
		export(__bang___eq_, elementDef(classname="org.globus.cog.karajan.workflow.nodes.functions.Misc"))
		
		export(fold
			parallelElement([lambda, initial, ...]
					set(fc, channel())
					discard(
						append(fc, initial)
					)
					for(i, dot(..., fc)
						discard(
							append(fc, executeElement(lambda, each(i)))
						)
					)
					first(fc)
			)
		)
	)
)