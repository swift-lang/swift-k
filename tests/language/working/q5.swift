type messagefile {} 

(messagefile t) greeting (string s[]) {   
    app {
        echo s[0] s[1] s[2] stdout=@filename(t);
    }
}

messagefile outfile <"q5out.txt">;

string words[] = ["how","are","you"];

outfile = greeting(words);

