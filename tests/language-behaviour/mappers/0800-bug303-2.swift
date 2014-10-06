type file;

app (file[] o) split(file i) {
	split "-l" 1 filename(i) "seqout.";
}


string[] s = ["seqout.aa", "seqout.ab", "seqout.ac", "seqout.ad"];
file[] out <ArrayMapper; files=s>;

file input <"0800-bug303-2.in">;

out = split(input);