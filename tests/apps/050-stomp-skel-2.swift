
type file;

app (file output) stomp(file input)
{
  stomp @input @output;
}

// Creates the output and a few files named directory/pg-*.out
app (file output, file index) pg(file input, string directory)
{
  pg @input directory @output @index;
}

app (file output) sph(file tarfile, string entry)
{
  sph @tarfile @entry @output;
}

app (file output) gpg(file input[])
{
  // Put output first for easier command-line processing
  gpg @output @input;
}

(file output) model(file input, int iter)
{
  tracef("model input: %M\n", input);
  string directory = @strcat("run-", iter);
  tracef("directory: %s\n", directory);
  string stomp_out_name = @strcat(directory, "/stomp.out");
  tracef("stomp_out_name: %s\n", stomp_out_name);
  file stomp_out<single_file_mapper;file=stomp_out_name>;
  stomp_out = stomp(input);
  tracef("stomp_out: %M\n", stomp_out);
  string pg_tar_name = @strcat(directory, "/pg.tar");
  file pg_tar<single_file_mapper;file=pg_tar_name>;
  string pg_index_name = @strcat(directory, "/pg.index");
  file pg_index<single_file_mapper;file=pg_index_name>;
  (pg_tar, pg_index) = pg(stomp_out, directory);
  tracef("pg output: %M %M\n", pg_tar, pg_index);
  string pg_entry_names[] = readData(pg_index);
  file pg_entries[]<array_mapper;files=pg_entry_names>;
  string sph_transform = @strcat(directory, "/sph-\\1");
  file sph_outputs[]<structured_regexp_mapper;
                     source=pg_entries,
                     match=".*/pg-(.*)",
                     transform=sph_transform>;
  foreach pg_entry, i in pg_entries
  {
    tracef("sph: %M:%s -> %M\n",
           pg_tar, pg_entry_names[i], sph_outputs[i]);
    sph_outputs[i] = sph(pg_tar, pg_entry_names[i]);
  }
  output = gpg(sph_outputs);
  tracef("model output: %M\n", output);
}

int iterations = @toint(@arg("iters"));

file inputs[];
file input0<"data-0.txt">;
tracef("starting with: %M\n", input0);
inputs[0] = input0;

iterate iter {
  tracef("iter: %i\n", iter);
  string output_name = @strcat("data-", iter+1, ".txt");
  tracef("output_name: %s\n", output_name);
  file output<single_file_mapper;file=output_name>;
  output = model(inputs[iter], iter);
  inputs[iter+1] = output;
} until (iter > iterations);
