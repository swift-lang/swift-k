type logfile;

app (logfile fout) cat(logfile fin) { 
    cat stdin=@fin stdout=@fout;
}

logfile infile <"063-filename_extraction.in">;
logfile outfile <"063-filename_extraction.out">;

outfile = cat(infile);
