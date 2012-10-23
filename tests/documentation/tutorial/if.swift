type messagefile;

app (messagefile t) greeting (string s) {
   echo s stdout=@filename(t);
}

messagefile outfile <"if.txt">;

boolean morning = true;

if(morning) {
  outfile = greeting("good morning");
} else {
  outfile = greeting("good afternoon");
}

