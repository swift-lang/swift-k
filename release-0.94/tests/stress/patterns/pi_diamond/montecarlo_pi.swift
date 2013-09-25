type data;
type script;

app (data out) gen_plots (script run, int recsize)
{
    bash @run recsize stdout=@out;
}

app (data out) analyse_data (script run, data plots[])
{
    bash @run @plots stdout=@out;
}

data points_out[] <simple_mapper; prefix="points", suffix=".out">;
script wrapper <"random_plot.sh">;

int loop = @toInt(@arg("loops","1"));
int fsize = @toInt(@arg("points","100"));

foreach item,i in [0:loop-1] {
	points_out[i] = gen_plots(wrapper, fsize);
}

data pi <"final.out">;
script analyser <"analyse.sh">;
pi = analyse_data (analyser, points_out);
