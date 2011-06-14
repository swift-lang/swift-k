type file {}

(file t) echo_array (string s[][]) {
    app {
        echo s[0][0] s[1][0] s[1][1] stdout=@filename(t);
    }
}

string greetings[][] = [ [ "left", "right" ], ["up", "down"]];

file hw <"array_multidimensional_index.out">;

hw = echo_array(greetings);

