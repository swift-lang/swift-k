import("sys.k")

namespace("xs"
	import("java.k")
	
	element(typesSSB, [], types=["java.lang.String", "java.lang.String", "boolean"])
	
	element(newSimpleNode, [name, type]
		x := new("org.griphyn.vdl.type.impl.TypeImpl", types=["java.lang.String", "boolean"], name, false)
		invokeMethod("setBaseType", object=x, types=["org.griphyn.vdl.type.Type"]
			invokeMethod("getType", classname="org.griphyn.vdl.type.TypeDefinitions", type)
		)
		x
	)
	
	element(newComplexNode, [name]
		new("org.griphyn.vdl.type.impl.TypeImpl", types=["java.lang.String", "boolean"], name, false)
	)
	
	element(addNode, [node]
		invokeMethod("addType", classname="org.griphyn.vdl.type.TypeDefinitions"
			types=["org.griphyn.vdl.type.Type"]
			node
		)
	)
	
	element(addField, [node, name, type, optional(array)]
		default(array, false)
		invokeMethod("addField", object=node, typesSSB(), name, type, array)
	)
	
	export(
		element(schema, []
			invokeMethod("resolveTypes", classname="org.griphyn.vdl.type.TypeDefinitions")
		)
		
		element(simpleType, [name, type]
			print("Type ", name, ": ", type)
			addNode(newSimpleNode(name, type))
		)
		
		element(restriction, [base]
			type=last(split(base, ":"))
		)
		
		element(complexType, [name, ...]
			print("Type ", name, ": ", list(for(f,..., [n,t,a]:=each(f), if(a==true concat(n,": ",t,"[]") concat(n,": ",t)))))
			node := newComplexNode(name)
			for(field, ...
				[name, type, array] := each(field)
				addField(node, name, type, array=array)
			)
			addNode(node)
		)
		
		element(sequence, [optional(minOccurs, maxOccurs), ...]
			maxOccurs := choice(maxOccurs, 0)
			[name, type, array] := each(first(...))
			if (
				maxOccurs == "unbounded" list(name, type, true)
				else(each(...))
			)
		)
		
		element(xs:element, [name, type]
		    type := last(split(type, ":"))
		    list(name, type, false)
		)
	)
)
