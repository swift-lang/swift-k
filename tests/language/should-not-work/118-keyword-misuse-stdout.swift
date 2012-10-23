 //THIS-SCRIPT-SHOULD-FAIL
type file {}                                           


app (messagefile stdout, messagefile b) greeting(string m) {
        echo m stdout=@filename(a) stderr=@filename(b);
}

file e;
file f;

(e,f) = greeting("hello world");


