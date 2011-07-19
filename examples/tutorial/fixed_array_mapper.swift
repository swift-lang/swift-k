type messagefile; 
type countfile; 

app (countfile t) countwords (messagefile f) {   
   wc "-w" @filename(f) stdout=@filename(t);
}

string inputNames = "fixed_array_mapper.1.txt fixed_array_mapper.2.txt fixed_array_mapper.3.txt";
string outputNames = "fixed_array_mapper.1.count fixed_array_mapper.2.count fixed_array_mapper.3.count";

messagefile inputfiles[] <fixed_array_mapper;files=inputNames>;
countfile outputfiles[] <fixed_array_mapper;files=outputNames>;

outputfiles[0] = countwords(inputfiles[0]);
outputfiles[1] = countwords(inputfiles[1]);
outputfiles[2] = countwords(inputfiles[2]);
