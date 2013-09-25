type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"091-case.out">;

int selector = 8;

trace(selector);

string message;

switch(selector) {
  case 3:
    message="first message";
  case 8:
    message="eighth message";
  case 57:
    message="last message";
  default:
    message="no message at all...";
}

outfile = greeting(message);

