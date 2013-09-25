type messagefile; 

app (messagefile t) greeting (string s[]) {   
     echo s[0] s[1] s[2] stdout=@filename(t);
}

messagefile outfile <"arrays.txt">;

string words[] = ["how","are","you"];

outfile = greeting(words);

