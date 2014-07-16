int[auto] array;

foreach i in [1:100] {
    array << (i*2);
}


foreach item in array {
    tracef("Item : %i \n", item);
}
