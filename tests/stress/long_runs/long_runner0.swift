type file;

file package <"dummy">;
file script  <"wrapper.sh">;
int loop = @toInt(@arg("loops","0"));
int delay = @toInt(@arg("delay","60"));

app (file out, file err) remote_driver (file run, file tar, int delay)
{
    bash @run @filename(tar) delay stdout=@filename(out) stderr=@filename(err);
}

file driver_out[] <simple_mapper; prefix="driver", suffix=".out">;
file driver_err[] <simple_mapper; prefix="driver", suffix=".err">;

foreach item,i in [0:loop] {
    (driver_out[i], driver_err[i]) = remote_driver(script, package, delay);
}