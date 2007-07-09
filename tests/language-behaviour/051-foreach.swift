type messagefile {}

(messagefile t) greeting(string l) { 
    app {
        echo @strcat("The filename is: ", @filename(t),
                     " and the loop var is: ", l) 
             stdout=@filename(t);
    }
}

string array[] = ["red", "green", "blue"];

foreach s in array {
  messagefile outfile <
      single_file_mapper;
      file=@strcat("051-foreach.",s,".out")
     >;
  outfile = greeting(s);
}

