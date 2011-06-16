type messagefile;

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"0053-toint.out">;

string left = "010";
string right = "99";

outfile = greeting(@toint(left) + @toint(right));

