type file;

/*
   Test case added to confirm Bug 1028 - readData in a foreach loop fails to compile.
   https://bugzilla.mcs.anl.gov/swift/show_bug.cgi?id=1028

   This test is expected to fail to compile till bug is resolved.
*/

app (file o) mysim (int i)
{
  echo i stdout=@filename(o);
}

int nums[];
foreach i in [0:9] {
  file f <single_file_mapper; file=@strcat("output/sim_",i,".out")>;
  f = mysim(i);
  nums[i] = readData(f);
}

file result <"readData3.out">;
result = mysim(nums[9]);
