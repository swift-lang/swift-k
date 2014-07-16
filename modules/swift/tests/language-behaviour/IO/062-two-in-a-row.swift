type messagefile {}

app (messagefile t) greeting() { 
        echo "Hello, world!" stdout=@filename(t);
}

app (messagefile t) greeting2(messagefile ignored) { 
        echo "Hello, world!" stdout=@filename(t);
}


messagefile outfile <"062-two-in-a-row.a.out">;
messagefile outfile2 <"062-two-in-a-row.b.out">;

outfile = greeting();

outfile2 = greeting2(outfile);
