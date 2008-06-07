type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

iterate i {
  messagefile outfile;
  outfile = greeting();
} until(i>10);


