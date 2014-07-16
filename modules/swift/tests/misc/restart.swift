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

file J <"restart.in">;
file K <"restart-1.out">;
file L <"restart-2.out">;
file M <"restart-3.out">;

K=a(J);
L=b(K);
M=c(L);

