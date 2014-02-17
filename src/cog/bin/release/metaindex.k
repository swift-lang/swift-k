include("sys.k")
include("task.k")
include("html.k")

global(srcdir, "src/cog")

element(main, []
	html:write(file = "modules.html"
		html(
			head(
				title("Java CoG Kit - module index")
				html:text("<link rel='stylesheet' type='text/css' href='style.css'>")
			)
			body(
				h2("Index of modules:")
				ul(
					formodule(element([m], li() a(href=concat("#", m), m)))
				)
				formodule(element([m], module(m)))
			)
		)
	)
)

element(formodule, [lambda]
	for(module, sort(file:list("{srcdir}/modules"))
		if(
			file:exists("{srcdir}/modules/{module}/project.properties") executeElement(lambda, module)
		)
	)
)

element(module, [m]
	print("Module: {m}")
	mdir := "{srcdir}/modules/{m}"
	
	
	name := property:get("{mdir}/project.properties", "long.name")
	
	anchor(m)
	h2(concat(m, ": ", name))
	
	table(
		tr(
			for(f, ["README.txt", "CHANGES.txt", "KNOWN-ISSUES.txt", "TODO.txt"], module:file(m, f))
		)
	)
	
	if(
		file:exists("{srcdir}/modules/{m}/meta/icon.png") 
		then(
			transfer(srcfile="{srcdir}/modules/{m}/meta/icon.png", destfile="{m}-icon.png", provider="local")
			img(src="{m}-icon.png")
		)
	)
	
	choice(
		sequential(
			h3("Description:")
			html:text(file:read("{mdir}/meta/description.txt"))
		)
		discard()
	)
	
	dependencies(m)
)

element(module:file, [module, file]
	mfile := "{srcdir}/modules/{module}/{file}"
	cog:version := cog:version()
	
	if(file:exists(mfile)
		then(
			td(valign="bottom", ""
				if(file:exists("{file}.png")
					img(src="{file}.png")
				)
				a(href="http://cogkit.svn.sourceforge.net/viewvc/cogkit/tags/{cog:version}/src/cog/modules/{module}/{file}", file)
			)
		)
	)
)

element(property:get, [file, name]
	lines := split(file:read(file), nl())
	
	for(line, lines
		nvpair := split(line, "=")
		
		if(list:size(nvpair) == 2
			if(strip(list:first(nvpair)) == name
				strip(list:last(nvpair))
			)
		)
	)
)

element(dependencies, [m]
	set(main.buildfile, "")
	set(deps
		/*
		 Pretend dependencies.xml is a Karajan file. Define the 'property'
		 element to return the 'value' attribute.
		*/
		namespace(prefix="tmp"
			element(target, [name])
			element(ant, [antfile, target])
			element(property, [name, value], value)
			
			list(
				file:execute("{srcdir}/modules/{m}/dependencies.xml")
			)
		)
	)
		
	if(
		list:isEmpty(deps) ""
		else(
			h3("Dependencies:")
			ul(
				li() dep(list:first(deps))
				for(dep, list:butFirst(deps), ", ", li() dep(dep))
			)
		)
	)
)

element(dep, [module]
	a(href=concat("#", module), module)
)

element(cog:version, []
	cache(
		property:get("{srcdir}/VERSION", "cog.version")
	)
)

main()
