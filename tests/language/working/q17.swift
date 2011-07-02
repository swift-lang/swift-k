type messagefile {} 
type countfile {} 

(countfile t) countwords (messagefile f) {   
    app {
        wc "-w" @filename(f) stdout=@filename(t);
    }
}

string inputNames[] = ["one.txt", "two.txt", "three.txt"];

messagefile inputfiles[] <array_mapper;files=inputNames>;


foreach f in inputfiles {
  countfile c<regexp_mapper;source=@f,match="(.*)txt",transform="\\1count">;
  c = countwords(f);
}

