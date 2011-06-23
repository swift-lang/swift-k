type messagefile;

app (messagefile t) greeting() {
  echo "hello" stdout=@filename(t);
}

iterate i {
  messagefile outfile;
  outfile = greeting();
} until(i>10);


