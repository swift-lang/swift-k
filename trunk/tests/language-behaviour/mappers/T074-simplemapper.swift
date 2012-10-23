
// The basename of this test must begin with a letter rather than a
// number because the mapper uses the first character to determine
// whether the prefix should be interpreted as a number or as
// a string - see bug 80

type messagefile;

type messagestruct {
    messagefile aleph;
    messagefile beth;
    messagefile gimel;
}

(messagefile t) stringToFile(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagestruct outstruct <simple_mapper;
                         prefix="T074-simplemapper.",
                         suffix=".out">;

outstruct.aleph = stringToFile("foo");
outstruct.beth = stringToFile("bar");
outstruct.gimel = stringToFile("baz");

