type messagefile;

(messagefile a, messagefile b) greeting(string m) { 
    app {
        echo m stdout=@filename(a) stderr=@filename(b);
    }
}

(messagefile firstfile, messagefile secondfile) = greeting("hi");

