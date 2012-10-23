
type file;

type s
{
    file m;
}

app (file out) create ()
{
    echo_sh "file1" @filename(out);
}

(file result) list (file inf)
{
    s diffs[] <csv_mapper; file=inf>;
    foreach f, i in diffs
    {
        tracef("%s\n", @f.m);
    }
}

file o<"tmp.csv">;
file i;
file p;

o = create();
p = list(o);
