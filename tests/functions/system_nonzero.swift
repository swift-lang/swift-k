// THIS-SCRIPT-SHOULD-FAIL

type file;

string results[] = system("false");
foreach r in results {
   tracef("%s\n", r);
}
