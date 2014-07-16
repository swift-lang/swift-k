type file;

(file f) echo (int i) {
app { echo i stdout=@f; }
}

int greetings = 2;
file hw = echo(greetings);
