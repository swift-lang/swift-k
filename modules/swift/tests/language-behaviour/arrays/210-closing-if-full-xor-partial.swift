type file;

app (file o) echo ()
{
   echo "foo.txt" stdout=@o;
}

(file fileList[]) createFileArray() {
  foreach i in [1:5] {
     fileList[i]=echo();
  }
}

file myFileArray[];



 if (1 == 1) {
   myFileArray = createFileArray();
 } else {
   myFileArray[0] = echo();
}