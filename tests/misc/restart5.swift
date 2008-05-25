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

(file r) q(file i, int n) {
file t;
switch(n) {
case 1: t=a(i); r=c(t);
case 2: t=b(i); r=c(t);
case 3: t=c(i); r=c(t);
}
}

file J <"restart.in">;

file X[];

file Y[];

foreach i in [1:3] {
  X[i] = q(J,i);
}

foreach x,j in X {
Y[j] = q(x,j);
}

