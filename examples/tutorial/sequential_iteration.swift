type counterfile;  
  
app (counterfile t) echo(string m) {   
    echo m stdout=@filename(t);  
}  
  
app (counterfile t) countstep(counterfile i) {  
    wcl @filename(i) @filename(t);  
}  
  
counterfile a[]  <simple_mapper;prefix="sequential_iteration.foldout">;  
  
a[0] = echo("793578934574893");  
  
iterate v {  
  a[v+1] = countstep(a[v]);  
 trace("extract int value ",@extractint(a[v+1]));  
} until (@extractint(a[v+1]) <= 1);  
