type messagefile;

app (messagefile t) greeting() {
        echo "hello" stdout=@filename(t);
}

messagefile outfile <my_first_mapper>;

outfile = greeting();
