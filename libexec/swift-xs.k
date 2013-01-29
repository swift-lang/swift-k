import(sys)

namespace(xs) {
	import(java)
	
	UnresolvedType := function(name, isArray) {
		java:new("org.griphyn.vdl.type.impl.UnresolvedType", types=["String"], name)
	}
	
	typesST := function() {
		types = ["String", "org.griphyn.vdl.type.Type"]
	}
	
	newSimpleNode := function(name, type) {
		x := java:new("org.griphyn.vdl.type.impl.TypeImpl", types=["String"], name)
		invokeMethod("setBaseType", object=x, types=["org.griphyn.vdl.type.Type"]
			invokeMethod("getType", classname="org.griphyn.vdl.type.Types", type)
		)
		x
	}
	
	newComplexNode := function(name) {
		java:new("org.griphyn.vdl.type.impl.TypeImpl", types=["String"], name)
	}
	
	addNode := function(node) {
		invokeMethod("addType", classname="org.griphyn.vdl.type.Types"
			types=["org.griphyn.vdl.type.Type"]
			node
		)
	}
	
	addField := function(node, name, type) {
		invokeMethod("addField", object=node, typesST(), name, type)
	}
	
	export(schema, 
		function() {
			invokeMethod("resolveTypes", classname="org.griphyn.vdl.type.Types")
		}
	)
	
	export(simpleType,
		function(name, type) {
			addNode(newSimpleNode(name, type))
		}
	)
	
	export(restriction,	
		function(base) {
			type = last(split(base, ":"))
		}
	)
	
	export(complexType,	
		function(name, ...) {
			node := newComplexNode(name)
			for(field, ...) {
				(name, type) := each(field)
				addField(node, name, type)
			}
			addNode(node)
		}
	)
	
	export(sequence,
		function(minOccurs = 0, maxOccurs = 0, ...) {
			(name, type) := each(first(...))
			if (maxOccurs == "unbounded") {
				list(name, UnresolvedType(type, true))
			}
			else {
				each(...)
			}
		}
	)
		
		//should be noted that we're dealing with type names here
	export(element,
		function(name, type) {
		    type := last(split(type, ":"))
		    list(name, UnresolvedType(type, false))
		}
	)
}
