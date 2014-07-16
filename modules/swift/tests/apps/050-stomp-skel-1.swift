
type file;

app (file output) model(file input)
{
  cp @input @output;
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
  output = model(inputs[iter]);
  inputs[iter+1] = output;
} until (iter > iterations);
