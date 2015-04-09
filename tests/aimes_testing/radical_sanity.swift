type file;

app (file out, file err) remote_driver (file wrapper)
{
    bash @wrapper stdout=@filename(out) stderr=@filename(err);
}

file driver_out[] <simple_mapper; prefix="sanity", suffix=".out">;
file driver_err[] <simple_mapper; prefix="sanity", suffix=".err">;
file wrapper <"wrapper.sh">;

int count = toInt(arg("N", "1"));

foreach i in [1:count] {
    (driver_out[i], driver_err[i]) = remote_driver(wrapper);
}
