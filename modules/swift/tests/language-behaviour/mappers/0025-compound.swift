type file;
(file t) echo(int m) {                                                          
 app {                                                                          
   echo m stdout=@filename(t);                                                  
 }                                                                              
}                                                                               
                                                                                
(file t) nested_echo(int m) {                                                   
 t = echo (m);                                                                  
}                                                                               
                                                                                
file a[]<simple_mapper;prefix="0025-compound.out">;                                       
a[0] = nested_echo(0); 

