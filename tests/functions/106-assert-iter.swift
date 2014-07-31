
// THIS-SCRIPT-SHOULD-FAIL

iterate i
{
  tracef("i: %i\n", i);
  assert(i != 4, "I_IS_4");
} until (false);
