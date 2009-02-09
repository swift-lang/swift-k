type messagefile;

(messagefile t) inner_greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

(string s) outer_greeting() {
  messagefile m <"ssss">;
  m = inner_greeting();
  s = readData(m);
}


// messagefile outfile <"089-nested-proc-call.swift">;

// outfile = greeting();

trace(outer_greeting());

