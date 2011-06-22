type file;                                                                    
app (file t) echo(string m) { 
        echo m stdout=@filename(t);
}
	// file f1[] <filesys_mapper;pattern="0083-for*.in",location=".">;                       
file f1 <"0032-strcat-mapper-param.1">;
string fn = @filename(f1);
file o <single_file_mapper;file=@strcat(fn,".out")>;
o = echo(fn);
