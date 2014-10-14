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

foreach sim in [1:nsim]{
    // Set the initial conditions to enter the iterate loop
    isdone[sim][0] = "notdone";
    // Set an empty file to be fed to the initial run of app
    out[sim][0] = empty();

    iterate steps {

        (out[sim][steps+1], err[sim][steps+1], done[sim][steps+1]) = fibonacci_randfail (wrapper , nsteps, 1, out[sim][steps]);
        isdone[sim][steps+1] = readData( done[sim][steps+1] );

    } until ( isdone[sim][steps] == "done" );
}

