
// THIS-SCRIPT-SHOULD-FAIL

foreach i in [1:5]
{
  tracef("i: %i\n", i);
  assert(i != 4, "I_IS_4");
}
