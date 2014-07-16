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

messagefile outfile[] <simple_mapper; prefix="076-structured-regexp-mapper.a",suffix=".out", padding=1>;

messagefile outfile2[] <structured_regexp_mapper;
    source=outfile, match="(.*)per.a(.*)",
    transform="\\1per.b\\2" >;

foreach i in [0:1]{
 outfile[i] = greeting();
 outfile2[i] = greeting2(outfile[i]);
}

