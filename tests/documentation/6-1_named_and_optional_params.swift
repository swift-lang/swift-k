type messagefile;
app (messagefile t) greeting (string s="hello") {
	echo s stdout=@filename(t);
}
messagefile french <"french.txt">;
messagefile english <"english.txt">;

french = greeting(s="bonjour");
english = greeting();
