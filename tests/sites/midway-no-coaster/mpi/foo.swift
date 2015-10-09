type file;

app (file o) run_app (file i)
{
    mpi_sum "-i" @i "-o" @o 3;
}

file input<"100-input.txt">;

foreach i in [0:4] {
    file out<single_file_mapper;file=@strcat("transform-",i,".out")>;
    file err<single_file_mapper;file=@strcat("transform-",i,".err")>;
    (out,err) = run_app(input);
}
