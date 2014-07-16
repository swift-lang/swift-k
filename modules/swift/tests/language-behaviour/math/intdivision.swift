type file;
file outfile <"intdivision.out">;

app (file o) echo (string s) {
   echo s stdout=@o;
}

int x = 7 %/ 5;                    // expect x = 1
float y = 1.0*@toFloat(x);         // expect y = 1.0
string z = @strcat(y);             

string msg = @strcat("x = ", x, ", y = ", y, ", z = ", z);
outfile = echo(msg);
