type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

string fn = "078-singlefilemapper-name-expression.out";

messagefile outfile <single_file_mapper;file=fn>;

outfile = write();

