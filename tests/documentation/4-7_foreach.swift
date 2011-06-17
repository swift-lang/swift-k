type messagefile {}
type countfile {}

app (countfile t) countwords (messagefile f) {
	wc "-w" @filename(f) stdout=@filename(t);
}

string inputNames = "one.txt two.txt three.txt";

messagefile inputfiles[] <fixed_array_mapper;files=inputNames>;

foreach f in inputfiles {
	countfile c<regexp_mapper;
		    source=@f,
		    match="(.*)txt",
		    transform="\\1count">;
	c = countwords(f);
}
