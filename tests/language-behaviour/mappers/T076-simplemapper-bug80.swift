
// The basename of this test must begin with a letter rather than a
// number because the mapper uses the first character to determine
// whether the prefix should be interpreted as a number or as
// a string - see bug 80

type messagefile;

type secondstruct {
    messagefile epsilon;
    messagefile sigma;
}

type messagestruct {
    messagefile aleph;
    messagefile beth;
    secondstruct subordinate;
}

(messagefile t) stringToFile(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagestruct outstruct <simple_mapper;
                         prefix="T076-simplemapper-bug80.",
                         suffix=".out">;

outstruct.aleph = stringToFile("foo");
outstruct.beth = stringToFile("bar");
outstruct.subordinate.epsilon = stringToFile("E");
outstruct.subordinate.sigma = stringToFile("S");

