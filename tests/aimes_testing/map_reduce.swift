type file;

app (file out) map ()
{
   date stdout=@out;
}

app (file out) reduce (file inputs[])
{
    sort @inputs stdout=@out;
}

file map_out[] <simple_mapper; prefix="date", suffix=".out">;
file red_out <"reduce_result.txt">;

int count = toInt(arg("N", "100"));

foreach i in [1:count] {
    map_out[i] = map();
}

red_out = reduce (map_out);

