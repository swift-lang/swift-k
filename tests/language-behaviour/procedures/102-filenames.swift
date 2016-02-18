type file;

app (file outf) echoNames1(file[] infs) {
	args filename(infs) stdout = filename(outf);
}

app (file outf) echoNames2(file[] infs) {
	args filenames(infs) stdout = filename(outf);
}

file[] infs <FixedArrayMapper; files=["102-filenames.1.in", "102-filenames.2.in", "102-filenames.3.in"]>;

file outf1 <"102-filenames.1.out">;
file outf2 <"102-filenames.2.out">;

outf1 = echoNames1(infs);
outf2 = echoNames2(infs);

