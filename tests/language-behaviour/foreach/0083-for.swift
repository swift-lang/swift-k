type file;                                                                    


(file t) echo(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

                                                                                
file f1[] <filesys_mapper;pattern="0083-for*.in",location=".">;                       
                                                                                
foreach i1 in f1 {                                                              
  string fn = @filename(i1);
  file o <single_file_mapper;file=@strcat(fn,".out")>;
  o = echo(fn);
}                                                                               
