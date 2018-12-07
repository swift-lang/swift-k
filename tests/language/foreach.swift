(string[] _names) nek_png_names(int _nstart, int _nend)
{
  foreach i in [_nstart:_nend]{
    _names[i-_nstart] = sprintf("%i", i);
  }
}

string[] names;
int start = 0;
int end = 10;

names = nek_png_names(start, end);
trace(names);

