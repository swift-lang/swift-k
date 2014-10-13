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

file  out[] <simple_mapper; location="output", prefix="fibonacci", suffix=".out">;
file  err[] <simple_mapper; location="output", prefix="fibonacci", suffix=".err">;
file done[] <simple_mapper; location="output", prefix="fibonacci", suffix=".done">;
string isdone[];

// Set the initial conditions to enter the iterate loop
isdone[0] = "notdone";
// Set an empty file to be fed to the initial run of app
out[0] = empty();

iterate steps {

    (out[steps+1], err[steps+1], done[steps+1]) = fibonacci_randfail (wrapper , nsteps, 1, out[steps]);
    //(out[steps+1], err[steps+1], done[steps+1]) = fibonacci (wrapper , 10, 1, out[steps]);
    isdone[steps+1] = readData( done[steps+1] );

} until ( isdone[steps] == "done" );


