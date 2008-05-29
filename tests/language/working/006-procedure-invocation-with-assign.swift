type file;

(file f) echo (int i) {
app { echo i stdout=@f; }
}

int greetings;
file hw=echo(greetings);

