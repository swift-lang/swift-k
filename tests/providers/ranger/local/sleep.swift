// *_THIS-SCRIPT-SHOULD-FAIL_*
app sleep (int i)
{
  sleep i;
}

sleep(@toint(@arg("n","300")));
