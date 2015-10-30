type file;

app (file out, file err) remote_driver (file input)
{
    cat @input stdout=filename(out) stderr=filename(err);
}

file driver_out <simple_mapper; prefix="sanity2", suffix=".out">;
file driver_err <simple_mapper; prefix="sanity2", suffix=".err">;
file input <"swift.conf">;

(driver_out, driver_err) = remote_driver(input);
