type file;

file package <"dummy">;
file script  <"wrapper.sh">;
int loops = @toInt(@arg("loops","0"));
int inner = @toInt(@arg("inner", "10"));
int delay = @toInt(@arg("delay","60"));

app (file out, file err) remote_driver (file run, file tar, int delay)
{
    bash @run @filename(tar) delay stdout=@filename(out) stderr=@filename(err);
}

file driver_out[][] <simple_mapper; prefix="driver", suffix=".out">;
file driver_err[][] <simple_mapper; prefix="driver", suffix=".err">;

foreach index,j in  [1:loops] {
   foreach item,i in [1:inner] {
       (driver_out[j][i], driver_err[j][i]) = remote_driver(script, package, delay);
   }
}