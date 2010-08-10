type file {};

app (file f) generate (string s) {
  echo s stdout=@f;
}

app (file f2) process (file f1, string text) {
  append @f1 text @f2;
}

app (file f3) combine (file f1, file f2) {
  cat @f1 @f2 stdout=@f3;
}

(file fd) diamond (string p1, string p2) {
    file fa<"fa.txt">;
    file fb<"fb.txt">;
    file fc<"fc.txt">;

    fa = generate("TOP");
    fb = process(fa, p1);
    fc = process(fa, p2);
    fd = combine(fb, fc);
}

file fd<"fd.txt">;
fd = diamond("LEFT", "RIGHT");
