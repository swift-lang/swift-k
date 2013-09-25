type messagefile;

p() {
 global string m = "hi";
}

(messagefile t) greeting() { 
    app {
        echo "foo" stdout=@filename(t);
    }
}

messagefile outfile <"1233-global-deep-declaration.out">;

outfile = greeting();

p();
