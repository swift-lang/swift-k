type file;

app (file editedParams) cat ( file inParams )
{
cat stdin=@inParams stdout=@editedParams;
}

file inParams<single_file_mapper; file="00262-function-param-file-many.in" >;

string config [] = readData( cat(cat(cat(inParams ) )));

trace(0,config[0]);
trace(1,config[1]);

