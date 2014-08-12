type file;
type script;

app (file out, file err) gen_data (script run, int recsize)
{
    bash @run recsize stdout=@out stderr=@err;
}

app (file out, file err) comb_data (script comb, file array[])
{
    bash @comb @array stdout=@out stderr=@err;
}


file tgen_out[] <simple_mapper; prefix="output/tgen", suffix=".out">;
file tgen_err[] <simple_mapper; prefix="output/tgen", suffix=".err">;

script wrapper <"teragen_wrap.sh">;
int loop = @toInt(@arg("loops","10"));
int fsize = @toInt(@arg("recsize","1")); # This would make 10M records per file
string dir = @arg("dir", "./");

foreach item,i in [0:loop-1] {
	(tgen_out[i], tgen_err[i]) = gen_data(wrapper, fsize);
}

script combine <"combiner.sh">;
file final <"output/final_result">;
file errs <"output/err_file">;
(final, errs) = comb_data(combine, tgen_out);
