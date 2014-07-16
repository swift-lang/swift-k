type file;


file input <"060-duplicate.in">;

file output <"060-duplicate.out">;

app (file t) echo(file m) { 
        echo @filename(m) stdout=@filename(t);
}

output = echo(input);

