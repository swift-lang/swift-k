type messagefile;

app (messagefile a, messagefile b) greeting(string m) { 
        echo m stdout=@filename(a) stderr=@filename(b);
}

messagefile firstfile <"121-multi-return-vars.first.out">;
messagefile secondfile <"121-multi-return-vars.second.out">;

(firstfile, secondfile) = greeting("hi");

