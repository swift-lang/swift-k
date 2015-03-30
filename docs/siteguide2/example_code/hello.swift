type file;

app (file out) echoToFile (string str)
{
    echo str stdout=@out;
}

int count = toInt(arg("N", "4"));

file output[] <simple_mapper; location="outputs", prefix="hello.", suffix=".out">;

foreach index in [1:count]
{
    output[index] = echoToFile ("Hello World!");
}

