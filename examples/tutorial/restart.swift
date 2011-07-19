type file;  
  
app (file f) touch() {  
    touch @f;  
}  
  
app (file f) processL(file inp) {  
    echo "processL" stdout=@f;  
}  
  
app (file f) processR(file inp) {  
    broken "process" stdout=@f;  
}  
  
app (file f) join(file left, file right) {  
    echo "join" @left @right stdout=@f;  
}  
  
file f = touch();  
  
file g = processL(f);  
file h = processR(f);  
  
file i = join(g,h);  
