type file;
type script;


app (file out) combine (script x, file a, file b)
{
	bash @x @a @b stdout=@out;
}

(file out) recombiner (script x, file[] array)
{
	file array1[];
	file array2[];
	tracef("length of array :%i \n", @length(array));
	switch (@length(array)){
	       case 1:
	       	    tracef("Case : 1 element %s \n", @filename(array[0]));
	       	    out = array[0];
	       case 2:
	       	    tracef("Case : 2 elements [%s,%s] \n", @filename(array[0]), @filename(array[1]));
	       	    out = combine(x, array[0], array[1]);
	       default:
		    tracef("Case : Default \n");
		    foreach f,index in array{
		    	if ( (index %% 2) == 0 ){
			   array1[index%/2] = f;
			}else{
			   array2[index%/2] = f;
			}
		    }		    
		    out = combine(x, recombiner(x, array1), recombiner(x, array2));
	}
}

app (file out) fake (script dummy, file[] array)
{
	bash @dummy @array stdout=@out;
}

wrap(script dummy, file[] array)
{
	int pivot = @length(array);
	file out1 <"temp1">;
	file out2 <"temp2">;
	out1 = fake(dummy, array[0:pivot%/2]);
	out2 = fake(dummy, array[pivot%/2:pivot]);
}

script combiner <"add.sh">;
file result <"final_result">;
#file inputs[] <simple_mapper; prefix="input_", suffix=".txt">;
file inputs[] <filesys_mapper; prefix="input_", suffix=".txt">;

foreach i,ind in inputs{
	tracef("File[%i] : %s\n", ind,@filename(i));
}

script dummy <"dummy.sh">
slice(dummy, inputs);
#result = recombiner(combiner, inputs);
