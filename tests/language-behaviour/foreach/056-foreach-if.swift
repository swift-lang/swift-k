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

messagefile outfiles[];

// try this test earlier on, though
foreach s, index in array {
  if( index %% 2 == 1) {
     outfiles[index] = greeting(index, s);
  }
}

