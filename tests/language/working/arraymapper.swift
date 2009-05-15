type file {};

file files[]<simple_mapper;pattern="*">;

foreach f in files {
   trace(f);
}

