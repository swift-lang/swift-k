type messagefile;
app (messagefile t) write() { 
	echo @filename(t) stdout=@filename(t);
}
messagefile outfile <"070-singlefilemapper.out">;
outfile = write();
