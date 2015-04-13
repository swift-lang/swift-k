type file;

app (file out, file log) count_words (file input, file wordcount_script)
{
  bash @wordcount_script @input stdout=@out stderr=@log;
}

app (file out, file log) merge (file s[], file merge_script)
{
  bash @merge_script filenames(s) stdout=@out stderr=@log;
}

int nbooks = toInt(arg("nbooks", "5"));

file wordcount_script <"wordcount.sh">;
file merge_script     <"merge.sh">;

file books[]    <filesys_mapper; location="inputs", suffix="txt">;
file wc_inter[];

foreach book,i in books
{
  file wc_out <single_file_mapper; file=strcat("output/sim_",i,".out")>;
  file wc_log <single_file_mapper; file=strcat("output/sim_",i,".log")>;
  (wc_out, wc_log) = count_words (books[i], wordcount_script);
  wc_inter[i] = wc_out;
}

file merge_out<"output/all_books.out">;
file merge_log<"output/all_books.log">;
(merge_out, merge_log) = merge(wc_inter, merge_script);

