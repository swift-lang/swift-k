type file;


file input <"170-duplicate-input.in">;

file output <"170-duplicate-input.out">;

app (file t) cat(file m, file n) { 
        cat @filename(m) @filename(n) stdout=@filename(t);
}

output = cat(input, input);

