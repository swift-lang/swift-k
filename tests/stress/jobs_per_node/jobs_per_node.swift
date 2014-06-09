type file;
type script;

app (file out, file err) app_run (script run)
{
    bash @run stdout=@out stderr=@err;
}

file f_out[] <simple_mapper; prefix="output/t", suffix=".out">;
file f_err[] <simple_mapper; prefix="output/t", suffix=".err">;

script wrapper <"count_jobs.sh">;

int loop = @toInt(@arg("loops","10"));

foreach item,i in [0:loop-1] {
	(f_out[i], f_err[i]) = app_run(wrapper);
}
