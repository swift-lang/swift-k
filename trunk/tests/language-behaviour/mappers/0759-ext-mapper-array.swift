type file;

(file t[]) write() { 
    app {
        touch "0759-ext-mapper-array.a.out" "0759-ext-mapper-array.q.out";
    }
}

file outfile[] <ext; exec="./0759-ext-mapper-array.sh.in">;

outfile = write();

