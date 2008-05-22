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


messagefile outfile <"062-two-in-a-row.a.out">;
messagefile outfile2 <"062-two-in-a-row.b.out">;

outfile = greeting();

outfile2 = greeting2(outfile);
