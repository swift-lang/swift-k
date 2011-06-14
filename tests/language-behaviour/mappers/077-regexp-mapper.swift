type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile outfile <regexp_mapper;
       source="outfoo",
       match="(...)(.*)",
       transform="077-regexp-mapper.\\2.\\1">;

outfile = greeting();

