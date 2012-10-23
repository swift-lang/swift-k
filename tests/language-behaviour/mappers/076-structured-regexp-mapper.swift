type messagefile {}

(messagefile t) greeting() { 
    app {
        echo "Hello, world!" stdout=@filename(t);
    }
}

(messagefile t) greeting2(messagefile ignored) { 
    app {
        echo "Hello, world!" stdout=@filename(t);
    }
}


messagefile outfile <"076-structured-regexp-mapper.a.out">;
messagefile outfile2 <structured_regexp_mapper;
    source=outfile,
    match="(.*)per.a(.*)",
    transform="\\1per.b\\2"
>;

outfile = greeting();

outfile2 = greeting2(outfile);
