type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"090-case.out">;


string message;

switch(8) {
  case 3:
    message="first message";
  case 8:
    message="eighth message";
  case 57:
    message="last message";
  default:
    message="no message at all...";
}

trace(message);
outfile = greeting(message);
