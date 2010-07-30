type file {};

file files[]<simple_mapper;pattern="*">;

foreach f in files {
  tracef("file: %s\n", @filename(f));
}
