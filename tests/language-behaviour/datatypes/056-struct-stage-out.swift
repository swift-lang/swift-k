type file;

type struct {
        file b; 
        file c; 
}

app (struct of) echo() {
        echo "foo" stdout=@filename(of.b) stderr=@filename(of.c);
}

struct s <ext; exec="056-struct-stage-out.mapper.sh">;

s = echo();
