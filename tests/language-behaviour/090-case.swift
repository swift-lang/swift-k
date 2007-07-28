type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"090-case.out">;


string message;

switch(8) {
  case 3:
    message="first message";
    break;
  case 8:
    message="eighth message";
    break;
  case 57:
    message="last message";
    break;
  default:
    message="no message at all...";
    break;
}

outfile = greeting(message);

