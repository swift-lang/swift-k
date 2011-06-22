
// this checks how spaces and quotes get passed through to underlying
// executables

type messagefile;

app (messagefile t) p() { 
       touch @filename(t);
}

messagefile outfile <"142-space-and-quotes.out">;
outfile = p();

messagefile outfileB <"142-space-and-quotes. space .out">;
outfileB = p();

messagefile outfileC <"142-space-and-quotes.2\" space \".out">;
outfileC = p();

messagefile outfileD <"142-space-and-quotes.3' space '.out">;
outfileD = p();

