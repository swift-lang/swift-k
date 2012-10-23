type file;

(file t) a(file i) { 
    app {
        helperA @filename(t);
    }
}

(file t) b(file i) { 
    app {
        helperB @filename(t);
    }
}

(file t) c(file i) { 
    app {
        helperC @filename(t);
    }
}

q(file i, int n) {
file t <single_file_mapper;file=@strcat("restart-",n,".out")>;
switch(n) {
case 1: t=a(i);
case 2: t=b(i);
case 3: t=c(i);
}
}

file J <"restart.in">;

q(J,1);
q(J,2);
q(J,3);

