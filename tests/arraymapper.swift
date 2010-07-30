type file {};

file files[]<simple_mapper;pattern="*">;

foreach f in files {
  trace(2);
  // tracef("file: %s\n", @filename(f));
}
