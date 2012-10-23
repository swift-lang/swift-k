type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

(messagefile first, messagefile second) compound() {
  first = greeting("f");
  second = greeting("s");

}

messagefile a <"0024-compound.Q.out">;
messagefile b <"0024-compound.R.out">;

(a,b) = compound();


