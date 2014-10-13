type file;

// The app will return a file "done" with string done when complete
app (file out, file err, file done) fibonacci_v2 (file script, int count, int sleep, file checkpoint)
{
    //    bash @script "-c" count "-s" sleep "-p" @checkpoint stdout=@out stderr=@err;
    bash @script "-c" count "-s" 1 "-e" 10000 "-f" @done "-p" @checkpoint stdout=@out stderr=@err;
}

// This app will be called a limted number of times by swift
app (file out, file err) fibonacci_v1 (file script, int count, int sleep, file checkpoint)
{
    //    bash @script "-c" count "-s" sleep "-p" @checkpoint stdout=@out stderr=@err;
    bash @script "-c" count "-p" @checkpoint stdout=@out stderr=@err;
}

app (file out) empty ()
{
    touch @out;
}

file wrapper <"fibonacci.sh">;
int nsim   = toInt(arg("nsim","1"));
int nsteps = toInt(arg("nsteps","5"));

/*
file out[][] <simple_mapper; location="output", prefix="fibonacci", suffix=".out">;
file err[][] <simple_mapper; location="output", prefix="fibonacci", suffix=".err">;

foreach i in [1:nsim]
{
    out[i][0] = empty();
    iterate steps {
        tracef("At step : %s\n", steps);
        (out[i][steps+1], err[i][steps+1]) = fibonacci_v1 (wrapper , 10, 1, out[i][steps]);
    } until ( steps == nsteps );
}
*/

file out2[][] <simple_mapper; location="output2", prefix="fibonacci", suffix=".out">;
file err2[][] <simple_mapper; location="output2", prefix="fibonacci", suffix=".err">;
file done2[][] <simple_mapper; location="output2", prefix="fibonacci", suffix=".done">;
string isdone[][];
foreach i in [1:nsim]
{
    isdone[i][0] = "notdone";
    out2[i][0] = empty();
    iterate steps {
        tracef("At step : %i\n", steps);
        (out2[i][steps+1], err2[i][steps+1], done2[i][steps+1]) = fibonacci_v2 (wrapper , 10, 1, out2[i][steps]);
        isdone[i][steps+1] = readData( done2[i][steps+1] );
        tracef("isdone at step %i : %s \n", steps, isdone[i][steps+1]);
    } until ( isdone[i][steps] == "done" );
}

