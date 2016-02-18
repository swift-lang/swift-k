// THIS-SCRIPT-SHOULD-FAIL

type file;

string results[] = system("fdjskflsdk");
foreach r in results {
   tracef("%s\n", r);
}
