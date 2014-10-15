type file;
type script;

script wrapper <"wrapper.sh">;

app (file out, file err) remote_driver (script run)
{
    bash @run stdout=@out stderr=@err;
}

file driver_out <simple_mapper; prefix="output/driver", suffix=".out">;
file driver_err <simple_mapper; prefix="output/driver", suffix=".err">;

tracef("Filename of the wraper  : %s \n", @wrapper);

(driver_out, driver_err) = remote_driver(wrapper);

