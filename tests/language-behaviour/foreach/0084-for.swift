type file;                                                                    


(file t) echo(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

                                                                                
file f1[] <filesys_mapper;pattern="0083-for*.in",location=".">;                       
                                                                                
foreach i1 in f1 {                                                              
}                                                                               
