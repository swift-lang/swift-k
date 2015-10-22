type file;

app (file out, file err) remote_driver_files ()
{
    date_f stdout=filename(out) stderr=filename(err);
}

app (file out, file err) remote_driver_default ()
{
    date_d stdout=filename(out) stderr=filename(err);
}

file driver_d_out <SimpleMapper; prefix="sanity_d", suffix=".out">;
file driver_d_err <SimpleMapper; prefix="sanity_d", suffix=".err">;
(driver_d_out, driver_d_err) = remote_driver_default();

file driver_f_out <SimpleMapper; prefix="sanity_f", suffix=".out">;
file driver_f_err <SimpleMapper; prefix="sanity_f", suffix=".err">;
(driver_f_out, driver_f_err) = remote_driver_files();
