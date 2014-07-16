type file;

app (file o) echo (string input)
{
  echo input stdout=@o;
}


main()
{
  foreach i in [0:0] {
    int ia[];
    if(true) {
      foreach j in [0:1] {
        if (j==0) {
          ia[j] = 100;
        }
        else {
          ia[j] = 101;
        }
      }
      sub(ia);
    }
  }
}

sub(int iap[])
{
  file f <"nested_array1.out">;
  f = echo(@sprintf("in sub: iap=%q",iap));
}

main();

