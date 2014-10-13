type file;

// The app will return a file "done" with string done when complete
app (file out, file err, file done) fibonacci (file script, int count, int sleep, file checkpoint)
{
    bash @script "-c" count "-s" 1 "-e" 10000 "-f" @done "-p" @checkpoint stdout=@out stderr=@err;
}

// The app will return a file "done" with string done when complete.
// With the -x option you can set the failure rate, and see simulated failures in the app
// and how swift handles those failures
app (file out, file err, file done) fibonacci_randfail (file script, int count, int sleep, file checkpoint)
{
    bash @script "-x" 0.3 "-c" count "-s" 1 "-e" 10000 "-f" @done "-p" @checkpoint stdout=@out stderr=@err;
}

// Create empty files for initial conditions
app (file out) empty ()
{
    touch @out;
}

file wrapper <"fibonacci.sh">;
int  nsim   = toInt(arg("nsim","1"));
int  nsteps = toInt(arg("nsteps","5"));

file  out[][] <simple_mapper; location="output", prefix="fibonacci", suffix=".out">;
file  err[][] <simple_mapper; location="output", prefix="fibonacci", suffix=".err">;
file done[][] <simple_mapper; location="output", prefix="fibonacci", suffix=".done">;
string isdone[][];

foreach i in [1:nsim]
{
    // Set the initial conditions to enter the iterate loop
    isdone[i][0] = "notdone";
    // Set an empty file to be fed to the initial run of app
    out[i][0] = empty();

    iterate steps {
        //tracef("At step : %i\n", steps);
        (out[i][steps+1], err[i][steps+1], done[i][steps+1]) = fibonacci_randfail (wrapper , 10, 1, out[i][steps]);
        //(out[i][steps+1], err[i][steps+1], done[i][steps+1]) = fibonacci (wrapper , 10, 1, out[i][steps]);
        isdone[i][steps+1] = readData( done[i][steps+1] );
        //tracef("Content of isdone at step %i : %s\n", steps, isdone[i][steps+1]);
    } until ( isdone[i][steps] == "done" );
}

