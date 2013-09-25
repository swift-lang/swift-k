type file;
type script;

app (file out, file err) comb_data (script comb, file array[])
{
    bash @comb @array stdout=@out stderr=@err;
}

# This stage is	done without any file pointers as the
# data movement	is expected to be minimal.
(file final) kway (script comb, file array[])
{
	int K_way = 10;
	file next_level[];
        int len	= @length(array);
	file bucket[][];
	foreach index, i in [0:len-1]
	{
		int bucketid = index %/ K_way;
                bucket[bucketid][index %% K_way] = array[index];
		tracef(" %s going to bucket:%i \n", @array[index], bucketid);
        }

	file bucket_out[] <simple_mapper; prefix="bucket", suffix=".out">;
	file bucket_err[] <simple_mapper; prefix="bucket", suffix=".err">;

	foreach id in [0:(len%/K_way)]
	{
                (bucket_out[id], bucket_err[id]) = comb_data (comb, bucket[id]);
        }
	file tmp_err <"tmp_err">;
	if ( @length(bucket_out) > K_way ){
	     final = kway(comb, bucket_out);
	}else{
	     (final, tmp_err) = comb_data(comb, bucket_out);
	}
}

file inputs[] <filesys_mapper; prefix="input_", suffix=".txt">;
file final <"final_result">;

foreach i,ind in inputs{
        tracef("File[%i] : %s\n", ind,@filename(i));
}

script dummy <"dummy.sh">;
final = kway(dummy,inputs);
