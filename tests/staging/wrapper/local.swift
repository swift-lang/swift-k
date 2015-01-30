type file;

file package <"dummy">;
file script  <"wrapper.sh">;
int loop = @toInt(@arg("loops","0"));

app (file out, file err) remote_driver (file run, file tar)
{
    bash @run @tar stdout=@out stderr=@err;
}

file driver_out[] <simple_mapper; prefix="driver", suffix=".out">;
file driver_err[] <simple_mapper; prefix="driver", suffix=".err">;


tracef("Filename of package : %s\n", @package);
tracef("Filename of script  : %s\n", @script);

foreach item,i in [0:loop] {
        (driver_out[i], driver_err[i]) = remote_driver(script, package);
}
