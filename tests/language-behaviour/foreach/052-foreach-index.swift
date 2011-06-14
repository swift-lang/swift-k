type messagefile {}

(messagefile t) greeting(int ix, string l) { 
    app {
        echo @strcat("The filename is: ", @filename(t),
                     ", the loop var is: ", l,
                     "and the index is: ", ix)
             stdout=@filename(t);
    }
}

string array[] = ["red", "green", "blue"];

// this highlights a bug, at least after all commits
// that 'index' is being treated as a string, not as
// an integer...

// try this test earlier on, though
foreach s, index in array {
  messagefile outfile <
      single_file_mapper;
      file=@strcat("052-foreach-index.",s,".out")
     >;
  outfile = greeting(index, s);
}

