type file;

(file t) a(file i) {
    app {
        helperA @t @i "IN A";
    }
}

(file t) b(file i) {
    app {
        helperB @t @i "IN B";
    }
}

(file t) c(file i) {
    app {
        helperC @t @i "IN C";
    }
}

(file r) q(file i, int n) {
    file t;
    if(n<5) { t=a(i); } else  { t=b(i); }
    r=c(t);
}

file J <"restart.in">;

file X[];

file Y[] <simple_mapper;prefix="restart-iterate.",suffix=".out">;

iterate iv {
    X[iv] = q(J,iv);
} until(iv>10);

foreach x,j in X {
    Y[j] = q(x,j);
}

