type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

string fn = "0781-singlefilemapper-name-expression";

messagefile outfile <single_file_mapper;file=@strcat(fn,".","out")>;

outfile = write();

