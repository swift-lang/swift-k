type file;

app (file t) echo_array (string s[][]) {
        echo s[0][0] s[1][0] s[1][1] stdout=@filename(t);
}

string greetings[][];

greetings[0][0] = "left";
greetings[0][1] = "right";
greetings[1][0] = "up";
greetings[1][1] = "down";

file hw <"array_multidimensional_index.out">;

hw = echo_array(greetings);

