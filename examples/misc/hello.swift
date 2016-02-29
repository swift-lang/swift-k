// Define file as a type
type file;

// A simple app that echo's a string to a file
app (file output) echo (string s) {
   echo s stdout=@output ;
}

// Map a filename to a variable of type file
file output <"hello.out">

// Call the app
output = echo("hi");
