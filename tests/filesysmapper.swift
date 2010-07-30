type file {};

file files[]<filesys_mapper; patter="*">;

foreach f in files {
   tracef("file: %s\n", @filename(f));
}
