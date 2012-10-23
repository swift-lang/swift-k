import("sys.k")

namespace("xs"
	import("java.k")
	
	element(UnresolvedType, [name, isArray]
		new("org.griphyn.vdl.type.impl.UnresolvedType", types=["String"], name)
	)
	
	element(typesST, [], types=["String", "org.griphyn.vdl.type.Type"])
	
	element(newSimpleNode, [name, type]
		x := new("org.griphyn.vdl.type.impl.TypeImpl", types=["String"], name)
		invokeMethod("setBaseType", object=x, types=["org.griphyn.vdl.type.Type"]
			invokeMethod("getType", classname="org.griphyn.vdl.type.Types", type)
		)
		x
	)
	
	element(newComplexNode, [name]
		new("org.griphyn.vdl.type.impl.TypeImpl", types=["String"], name)
	)
	
	element(addNode, [node]
		invokeMethod("addType", classname="org.griphyn.vdl.type.Types"
			types=["org.griphyn.vdl.type.Type"]
			node
		)
	)
	
	element(addField, [node, name, type]
		invokeMethod("addField", object=node, typesST(), name, type)
	)
	
	export(
		element(schema, []
			invokeMethod("resolveTypes", classname="org.griphyn.vdl.type.Types")
		)
		
		element(simpleType, [name, type]
			addNode(newSimpleNode(name, type))
		)
		
		element(restriction, [base]
			type=last(split(base, ":"))
		)
		
		element(complexType, [name, ...]
			node := newComplexNode(name)
			for(field, ...
				[name, type] := each(field)
				addField(node, name, type)
			)
			addNode(node)
		)
		
		element(sequence, [optional(minOccurs, maxOccurs), ...]
			maxOccurs := choice(maxOccurs, 0)
			[name, type] := each(first(...))
			if (
				maxOccurs == "unbounded" list(name, UnresolvedType(type, true))
				else(each(...))
			)
		)
		
		//should be noted that we're dealing with type names here
		element(xs:element, [name, type]
		    type := last(split(type, ":"))
		    list(name, UnresolvedType(type, false))
		)
	)
)
