type messagefile {}

(messagefile t) greeting() { 
    app {
        echo "test" stdout=@filename(t);
    }
}

string array[] = ["red", "green", "blue"];

foreach s in array {
  messagefile outfile <single_file_mapper;file=@strcat("050-foreach.",s,".out")>;
  outfile = greeting();
}

