type matrix {
    string rows[];
}

matrix m;
m = readStructured("readStructured02.in");

foreach item,index in m.rows {
    tracef("Row[%i] : %s \n", index, item);
}
