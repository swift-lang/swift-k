type file {};

file files[]<filesys_mapper; pattern="*.dat">;

foreach f in files {
   tracef("file: %s\n", @filename(f));
}
