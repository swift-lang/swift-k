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
  tracef("in sub: entered\n");
  tracef("in sub: iap=%q\n",iap);
}

main();
