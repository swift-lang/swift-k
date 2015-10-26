type file;

app (file out) map1 ()
{
   date stdout=@out;
}

app (file out) map2 ()
{
   date stdout=@out;
}

app (file out) map3 (file deps)
{
   date stdout=@out;
}

app (file out) reduce (file i1[], file i2[])
{
    sort @i1 @i2 stdout=@out;
}

file map_out1[] <simple_mapper; prefix="date1", suffix=".out">;
file map_out2[] <simple_mapper; prefix="date2", suffix=".out">;
file map_out3[] <simple_mapper; prefix="date3", suffix=".out">;

file red_out1 <"reduce_result1.txt">;
file red_out2 <"reduce_result2.txt">;

int count = toInt(arg("N", "100"));

foreach i in [1:count] {
    map_out1[i] = map1();
}

foreach i in [1:count] {
    map_out2[i] = map2();
}

red_out1 = reduce (map_out1, map_out2);

foreach i in [1:count] {
    map_out3[i] = map3(red_out1);
}

red_out2 = reduce (map_out1, map_out3);
