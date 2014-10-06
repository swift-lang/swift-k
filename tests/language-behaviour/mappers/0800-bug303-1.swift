type file;

app (file[] o) split(file i) {
	split "-l" 1 filename(i) "seqout.";
}

file[] out <FixedArrayMapper; files="seqout.aa, seqout.ab, seqout.ac, seqout.ad">;

file input <"0800-bug303-1.in">;

out = split(input);