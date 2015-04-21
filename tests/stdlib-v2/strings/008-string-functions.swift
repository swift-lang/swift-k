import "stdlib.v2";

assertEqual(string[] a, string[] b) {
	assertEqual(length(a), length(b), msg = "Array sizes don't match");
	foreach i in [0 : length(a) - 1] {
		assertEqual(a[i], b[i]);
	}
}

assertEqual(strcat("left", 1, "right"), "left1right");
assertEqual(strcat("oneArg"), "oneArg");
assertEqual(strcat("two", "Args"), "twoArgs");

assertEqual(length("0123456789"), 10);
assertEqual(length(""), 0);

string[] expected1 = ["one", "two", "three"];
string[] actual1 = split("one two three", " ");
assertEqual(actual1, expected1);

string[] expected11 = ["one", "two", "three", "four"];
string[] actual11 = split("one  two   three four", " ");
assertEqual(actual11, expected11);

string[] expected2 = ["one", "two three"];
string[] actual2 = split("one two three", " ", 2);
assertEqual(actual2, expected2);

string[] expected3 = ["one", "two", "three"];
string[] actual3 = splitRe("one   two  	three", "\\s+");
assertEqual(actual3, expected3);

string[] expected4 = ["one", "two  	three"];
string[] actual4 = splitRe("one   two  	three", "\\s+", 2);
assertEqual(actual4, expected4);

assertEqual(trim(" 	bla\n"), "bla");

assertEqual(substring("blabla", 3), "bla");
assertEqual(substring("blabla", 3, 5), "bl");

assertEqual(toUpper("blaBla"), "BLABLA");
assertEqual(toLower("BLAbLA"), "blabla");

assertEqual(join(["one", "two", "three"], " "), "one two three");
string[] empty = [];
assertEqual(join(empty, ""), "");

assertEqual(replaceAll("one two three two", "two", "four"), "one four three four");
assertEqual(replaceAll("one two three two", "two", "four", 1, 8), "one four three two");

assertEqual(replaceAllRe("one two three two", "([nr]e)", "x$1x"), "oxnex two thxrexe two");
assertEqual(replaceAllRe("one two three two", "t[wh]", "x", 1, 12), "one xo xree two");

assertEqual(indexOf("one two three two", "two", 0), 4);
assertEqual(indexOf("one two three two", "four", 0), -1);

assertEqual(indexOf("one two three two", "two", 6, 16), 14);
assertEqual(indexOf("one two three two", "two", 6, 10), -1);

assertEqual(lastIndexOf("one two three two", "two", -1), 14);
assertEqual(lastIndexOf("one two three two", "four", -1), -1);

assertEqual(lastIndexOf("one two three two", "two", 13, 0), 4);

assert(matches("aaabbccdd", "[ab]+[cd]+"));

string[] expected5 = ["one", "two", "three"];
string[] actual5 = findAllRe("one two three", "(\\w+)");

assertEqual(actual5, expected5);
