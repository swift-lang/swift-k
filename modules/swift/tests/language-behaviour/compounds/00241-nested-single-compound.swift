type file;

app (file t) greeting(string m) { 
        echo m stdout=@filename(t);
}

(file first) compound() {
  first = greeting("f");
}

(file first) compoundB() {
  first = compound();
}

file a <"00241-nested-single-compound.out">;

a = compoundB();


