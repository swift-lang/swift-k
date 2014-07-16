type messagefile; 
type countfile; 

app (countfile t) countwords (messagefile f) {   
	wc "-w" @filename(f) stdout=@filename(t);
}

messagefile inputfile <"regexp_mapper.words.txt">;

countfile c <regexp_mapper;
	    source=@inputfile,
            match="(.*)txt",
            transform="\\1count">;

c = countwords(inputfile);

