type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile outfile[] <simple_mapper;
                     prefix="085-iterate.",
                     suffix=".out">;

iterate i {
  int j = i;
  outfile[i] = greeting();
} until(j>10);


