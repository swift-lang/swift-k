
type file;

app (file o) catnap (string _delay, file i)
{
 catnap _delay @i stdout=@o;
}

string delay=@arg("s","1");

file data[]<array_mapper; files=["/tmp/indir/data1.txt","/tmp/indir/data2.txt","/tmp/indir/data4.txt","/tmp/indir/data4.txt"]>;

file out[]<array_mapper; files=["outdir/out1.data","outdir/out2.data","outdir/out3.data","outdir/out4.data"]>;

foreach o, j in out {
 out[j] = catnap(delay,data[j]);
}

