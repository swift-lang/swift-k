type file;

app (file o) copy(file i) {
  cp @i @o;
}

file input<"file-0.txt">;

iterate i {
  string si = @strcat("file-",i,".txt");
  int j = i+1;
  string sj = @strcat("file-",j,".txt");
  file fi<single_file_mapper;file=si>;
  file fj<single_file_mapper;file=sj>;
  fj = copy(fi);
} until(i==5);
