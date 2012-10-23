
string alphabet[];
alphabet[0] = "a";
alphabet[1] = "b";
alphabet[2] = "c";
alphabet[3] = "d";
alphabet[4] = "e";

iterate i  
{
   tracef("Letter %i is: %s\n", i, alphabet[i]);
}  until(i == 5);
