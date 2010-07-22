type file {};

file files[]<filesys_mapper;pattern="*">;

foreach f in files {
   print(f);
}

