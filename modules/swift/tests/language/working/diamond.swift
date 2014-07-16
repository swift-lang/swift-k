type file {};

(file f) generate (float p1) {
    app {
		generate "-aTOP -T4" "-p" p1 "-o" @f;
    }
}

(file f2) process (file f1, string name, float p2) {
    app {
		process "-a" name "-T4" "-p" p2 "-i" @f1 "-o" @f2;
    }
}

(file f3) combine (file f1, file f2) {
    app {
		combine "-aBOTTOM -T4" "-i" @f1 @f2 "-o" @f3;
    }
}

(file fd) diamond (float p1, float p2) {
    file fa;
    file fb; 
	file fc;
    
    fa = generate(p1);
    fb = process(fa, "LEFT", p2);
    fc = process(fa, "RIGHT", p2);
    fd = combine(fb, fc);
}

file final<"FINAL">;
final = diamond(1.0, 100.0);
