type messagefile;

type struct {
messagefile eerste;
messagefile twede;
};

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

struct outfiles <ext; exec="./07554-ext-mapper-struct.sh.in">;

outfiles.eerste = write("1st");
outfiles.twede = write("2nd");

