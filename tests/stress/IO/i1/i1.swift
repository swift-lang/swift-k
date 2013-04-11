type file;
type perlscript;

perlscript sumall_pl <"sumall.pl">;

app (file output) do_sum (file input, perlscript ps)
{
  perl @ps @filename(input) stdout=@filename(output);
}

# Constants and command line arguments
int nFiles       = @toInt(@arg("nfiles", "1000"));
string Indir     = @arg("Indir", "./input/");

# Input Dataset
file inputs[] <filesys_mapper; location=Indir, suffix=".inp">;
file outputs[] <simple_mapper; location=Indir, suffix=".out">;

foreach input,i in inputs {
    outputs[i] = do_sum(input, sumall_pl);
}
