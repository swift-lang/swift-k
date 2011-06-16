type file;

(file t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

(file first) compound() {
  first = greeting("f");
}

(file first) compoundB() {
  first = compound();
}

file a <"00241-nested-single-compound.out">;

a = compoundB();


