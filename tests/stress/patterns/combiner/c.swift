# 2^10-1 
int N=15;

int tree_results[][];
tree_results[0] = [0:N:1];

(int res) combine (int a, int b)
{
	res = a + b;     
}

# add adjacent elements and put them in the next array
#  0 1 2 3 4 5 6 7
#   1   5   9   13
#     6       22
#        28
iterate depth
{	
  tracef("depth = %i \n", depth);  
  foreach v,i in tree_results[depth]{
    if ( i%%2 == 0 ){
      //tracef("array item : %i\n", v);
      tree_results[depth+1][i%/2] = combine(tree_results[depth][i], tree_results[depth][i+1]);
    }
  }
}until (depth == 1);



# Print the contents of the array at a particular depth: dep
int dep = 1;
foreach v, i in tree_results[dep]{
	tracef("Results_tree[%i][%i] : %i\n",dep, i, v);
}
