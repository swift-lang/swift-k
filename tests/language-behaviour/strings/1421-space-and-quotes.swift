
// this checks how spaces and quotes get passed through to underlying
// executables

type messagefile;

app (messagefile t) p() { 
       touch @filename(t);
}

messagefile outfileC <"1421-space-and-quotes.2\" space .out">;
outfileC = p();

messagefile outfileD <"1421-space-and-quotes.3' space .out">;
outfileD = p();

messagefile outfileE <"1421-space-and-quotes.2 sp\"ace .out">;
outfileE = p();

messagefile outfileF <"1421-space-and-quotes.3 sp'ace .out">;
outfileF = p();

messagefile outfileG <"1421-space-and-quotes.2' sp\"ac\"e .out">;
outfileG = p();

messagefile outfileH <"1421-space-and-quotes.3' sp'ac'e .out">;
outfileH = p();

messagefile outfileI <"1421-space-and-quotes.''' ' ''' '' '''''' ' \"\"\"\"\"\"\"\"\" '    '  ' \"\"\"' \"'\".out">;
outfileI = p();
