
app (external e) sleep (int i)
{
  sleep i;
}

(string t) mkstring (string s, int i, external e)
{
  t = @sprintf("%s: %i%k", s, i, e);
}

foreach i in [1:5]
{
  int delay = (7-i)*2;
  external e1 = sleep(delay);
  string s1 = mkstring("delayed", delay, e1);
  tracef("%s\n", s1);
}

string s2 = @sprintf("ready");
tracef("%s\n", s2);
