
type file;

app (file intermediate) p() {
  slowint stdout=@intermediate;
}

file f = p();


// TODO need two cases (at least) - one with assignment in declaration
// and one with assignment decoupled from declaration

// the decoupled case gets further along that the single-statement
// case, at time of writing.

// int i = @extractint(f);

int i;
i = @extractint(f);

trace(i);

