type messagefile;

app (messagefile o) write(messagefile i) {
  echo @filename(i) stdout=@filename(o);
}

messagefile infile <"071-singlefilemapper-input.in">;
messagefile outfile <"071-singlefilemapper-input.out">;

outfile = write(infile);

