
type file;

app (file o) catnap (string _delay, file i)
{
 catnap _delay @i stdout=@o;
}

string delay=@arg("s","1");

file out[]<array_mapper; files=["./outdir/out1.data","./outdir/out2.data","./outdir/out3.data","./outdir/out4.data"]>;

file data[]<array_mapper; files=["/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/indir/data1.txt","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/indir/data2.txt","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/indir/data4.txt","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/indir/data4.txt"]>;

foreach o, j in out {
 out[j] = catnap(delay,data[j]);
}

