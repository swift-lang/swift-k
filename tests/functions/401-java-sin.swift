type file;

app (file result) sin(float x2) { 
    echo @strcat(@java("java.lang.Math", "sin", x2)) stdout=@filename(result);
}

float x = 0.5;

file output<"401-java-sin.out">;
output = sin(x);
