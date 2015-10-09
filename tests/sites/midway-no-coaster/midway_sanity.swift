type file;

app (file out, file err) remote_driver ()
{
    mpi_run stdout=filename(out) stderr=filename(err);
}

file driver_out[] <simple_mapper; location="output", prefix="sanity", suffix=".out">;
file driver_err[] <simple_mapper; location="output", prefix="sanity", suffix=".err">;

int loops=toInt(arg("loops","1"));

foreach i in [1:loops]{

    printf("Loop : %d", i);
    (driver_out[i], driver_err[i]) = remote_driver();
}
