
type file;

app (file o) catnap (string _delay, file i)
{
 catnap _delay @i stdout=@o;
}

string delay=@arg("s","1");

file out[]<array_mapper; files=["/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/out1.data","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/out2.data","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/out3.data","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/out4.data"]>;

file data[]<array_mapper; files=["/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/data1.txt","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/data2.txt","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/data4.txt","/home/ketan/swift-install/0.93/cog/modules/swift/tests/cdm/absolute/data4.txt"]>;

foreach o, j in out {
 out[j] = catnap(delay,data[j]);
}

