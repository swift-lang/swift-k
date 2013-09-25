type messagefile;

(messagefile t) greeting(int n) { 
    app {
        echo n stdout=@filename(t);
    }
}

int n = 5000;
messagefile outfile <ext; exec="07552-ext-mapper-numeric.sh.in", ssss=n>;

outfile = greeting(n);

