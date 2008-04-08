
type giffile;

string directions[];

giffile gifx <single_file_mapper;file=@strcat("atlas-",directions[0],".gif")>;
giffile gifx <single_file_mapper;file=@strcat("atlas-",directions[1],".gif")>;
giffile gifx <single_file_mapper;file=@strcat("atlas-",directions[2],".gif")>;
