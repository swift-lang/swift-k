type file;  
  
(file f) touch() {  
  app {  
    touch @f;  
  }  
}  
  
(file f) processL(file inp) {  
  app {  
    echo "processL" stdout=@f;  
  }  
}  
  
(file f) processR(file inp) {  
  app {  
    broken "process" stdout=@f;  
  }  
}  
  
(file f) join(file left, file right) {  
  app {   
    echo "join" @left @right stdout=@f;  
  }   
}  
  
file f = touch();  
  
file g = processL(f);  
file h = processR(f);  
  
file i = join(g,h);  
