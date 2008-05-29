
type File;

(File f) myproc1 ( int i, string optional_arg = "default" ) {
  app { echo optional_arg stdout=@f; }
}


File out = myproc1 ( 100, optional_arg = "foo" );

