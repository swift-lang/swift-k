
app (external e) sleep (int i)
{
  sleep i;
}

(string t) mkstring (string s, int i, external e)
{
  t = @sprintf("%s: %i%k", s, i, e);
}

string a[];

string s1 = @sprintf("delayed: array%k", a);
tracef("%s\n", s1);

foreach i in [1:5]
{
  int delay = (7-i)*2;
  external e1 = sleep(delay);
  a[i] = mkstring("delayed", delay, e1);
  tracef("%s\n", a[i]);
}

string s2 = @sprintf("ready");
tracef("%s\n", s2);
