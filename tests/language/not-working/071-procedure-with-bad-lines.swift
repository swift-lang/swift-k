type messagefile {}

(messagefile t) greeting() {
  a()
  b()
}

messagefile outfile <"hello.txt">;

outfile = greeting();

