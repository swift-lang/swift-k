type file {};

file files[]<filesys_mapper;pattern="*">;

foreach f in files {
   trace(f);
}

