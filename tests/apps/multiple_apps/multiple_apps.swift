type file;


app (file out, file err) date ()
{
    date stdout=@out stderr=@err;
}

app (file out, file err) cat (file dates[])
{
    cat @dates stdout=@out stderr=@err;
}

int loops = toInt(arg("loops", "10"));

file out[] <simple_mapper; location="output", prefix="foo", suffix=".out">;
file err[] <simple_mapper; location="output", prefix="foo", suffix=".err">;

foreach item in [1:loops] {
    tracef("Item : %d\n", item);
    (out[item], err[item]) = date();
}

file fin_out <simple_mapper; location="output", prefix="final", suffix=".out">;
file fin_err <simple_mapper; location="output", prefix="final", suffix=".out">;

(fin_out, fin_err) = cat(out);
