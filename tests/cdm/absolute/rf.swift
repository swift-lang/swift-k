
type file;

app (file o) catnap (string delay, file i)
{
 catnap delay @i stdout=@o;
}

string delay=@arg("s","1");

file out[]<array_mapper; files=["/home/ketan/cdm_tests/out1.data","/home/ketan/cdm_tests/out2.data","/home/ketan/cdm_tests/out3.data","/home/ketan/cdm_tests/out4.data"]>;

file data[]<array_mapper; files=["data1.txt","data2.txt","data4.txt","data4.txt"]>;

foreach o, j in out {
 out[j] = catnap(delay,data[j]);
}

