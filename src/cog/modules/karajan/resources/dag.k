import("sys.k")

element(execdag, [nodes, edges]
	nmap := map(each(nodes))
	emap := map(each(edges))

	fmap := map(
		for(id, nmap, map:entry(id, channel()))
	)

	ins := map(
		for(e, emap
			for(oe, map:get(e, emap), map:entry(oe, true))
		)
	)

	starts := list(
		for(n, nmap, 
			if(not(map:contains(n, ins)) n)
		)
	)

	/*print("Nmap: ", nmap)
	print("Emap: ", emap)
	print("Fmap: ", fmap)
	print("Ins: ", ins)
	print("Starts: ", starts)*/
	
	for(s, starts
		append(map:get(s, fmap), true)
	)

	uparallelFor(id, nmap
		first(map:get(id, fmap))
		executeElement(map:get(id, nmap))
		oute := map:get(id, emap)
		for(e, oute
			append(map:get(e, fmap), true)
		)
	)
)

namespace(prefix="dag"
	export(dag, 
		element([channel(edges), channel(nodes)]
			execdag(nodes, edges)
		)
	)
	
	export(node
		element([id, lambda, channel(eto)]
			to(nodes, map:entry(id, lambda))
			to(edges, map:entry(id, join(each(eto))))
		)
	)
	
	export(edges
		element([optional(to), ...]
			to(eto, maybe(to))
			to(eto, ...)
		)
	)
)
