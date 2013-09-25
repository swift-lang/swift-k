type vector {
    int columns[];
}
type matrix {
    vector rows[];
}

matrix m;
m = readStructured("readStructured03.in");
