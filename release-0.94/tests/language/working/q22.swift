type messagefile {} 
type countfile {} 

(countfile t) countwords (messagefile f) {   
    app {
        wc "-w" @filename(f) stdout=@filename(t);
    }
}

string inputNames[] = ["one.txt", "two.txt", "three.txt"];
string outputNames[] = ["one.count", "two.count", "three.count"];

messagefile inputfiles[] <array_mapper;files=inputNames>;
countfile outputfiles[] <array_mapper;files=outputNames>;

outputfiles[0] = countwords(inputfiles[0]);
outputfiles[1] = countwords(inputfiles[1]);
outputfiles[2] = countwords(inputfiles[2]);

