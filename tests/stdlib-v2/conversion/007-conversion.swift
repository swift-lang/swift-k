import "stdlib.v2";

assertEqual(toInt(1.1), 1);
assertEqual(toFloat(2), 2.0);

assertEqual(parseInt("123"), 123);
assertEqual(parseInt("10", base = 8), 8);
assertEqual(parseInt("ff", base = 16), 255);

assertEqual(parseFloat("1.23"), 1.23);
assertEqual(parseFloat("1e-2"), 0.01);

assertEqual(toString(16), "16");
assertEqual(toString(16.2), "16.2");

assertEqual(toString(true), "true");
assertEqual(toString(false), "false");