type vector {  
   int columns[];   
}    

type matrix {  
   vector rows[];   
}    

type file;

app (file o) echo(int s) {
    echo s stdout=@o;
}


matrix m; 

m = readData2("readData2.in"); 

int s;

s = m.rows[0].columns[0] +
        m.rows[0].columns[1] +
        m.rows[0].columns[2] +
        m.rows[1].columns[0] +
        m.rows[1].columns[1] +
        m.rows[1].columns[2] ; 

file out <"readData2.out">;
out = echo(s);

