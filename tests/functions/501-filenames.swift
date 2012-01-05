type file;

app (file o) echo ( file files[] )
{
   echo @filenames(files) stdout=@o;
}

string s[] = [ "a.txt", "b.txt", "c.txt" ];
file f[] <array_mapper;files=s>;
file output<"501-filenames.out">;
output = echo(f);
