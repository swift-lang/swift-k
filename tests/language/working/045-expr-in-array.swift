// see thread rooted at
// <fec1351f0702060809p7264eddfsc63232f0a2d0d78d@mail.gmail.com>
// it's unclear whether this should work or not, from that thread
// (at least as much had progressed at time of writing)

int subjectNo[], trialTypes[], outputs[];

foreach t,j in subjectNo {                                       
     foreach s,i in trialTypes {                                      
       outputs[i*j]=3;                                                     
   }                                                                         
} 
