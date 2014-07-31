type file;

app (file editedParams) setTemps ( file inParams )
{
cat stdin=@inParams stdout=@editedParams;
}

file inParams<single_file_mapper; file="00261-function-param-file.in" >;

string config [] = readData( setTemps(inParams ) );

trace(0,config[0]);
trace(1,config[1]);

