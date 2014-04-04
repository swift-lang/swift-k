type file;

# *_THIS-SCRIPT-SHOULD-FAIL_*

string results[] = system("false");
foreach r in results {
   tracef("%s\n", r);
}
