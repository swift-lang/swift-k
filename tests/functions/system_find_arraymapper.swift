type file;

app check_files (file inputs[])
{
   ls "data/foo_a/foo_a.txt" "data/foo_a/foo_b.txt" "data/foo_b/bar_1" "data/foo_b/bar_2" "data/data.txt";
}

file inputs[] <array_mapper; files=system("find data -type f")>;
foreach i in inputs {
   tracef("%s\n", filename(i));
}

check_files(inputs);
