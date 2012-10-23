type messagefile {} 
type countfile {} 

(countfile t) countwords (messagefile f) {   
    app {
        wc "-w" @filename(f) stdout=@filename(t);
    }
}

messagefile inputfile <"q16.txt">;

countfile c<regexp_mapper;source=@inputfile,match="(.*)txt",transform="\\1count">;

c = countwords(inputfile);

