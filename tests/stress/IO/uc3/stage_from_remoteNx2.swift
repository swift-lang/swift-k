type file;
file script  <"filemaker.sh">;

app (file out, file log) remote_driver (file run, int size)
{
    bash @run size @out stdout=@filename(log);
}

file driver_out[] <simple_mapper; prefix="driver", suffix=".out">;
file driver_log[] <simple_mapper; prefix="driver", suffix=".log">;

int filesize = @toInt(@arg("size","10"));
int loop = @toInt(@arg("loops","0"));

foreach item,i in [0:loop] {
        (driver_out[i], driver_log[i]) = remote_driver(script, filesize);
}