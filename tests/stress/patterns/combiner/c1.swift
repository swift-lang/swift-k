
# 2^10-1 
int N=15;

int tree_results[][];
tree_results[0] = [0:N:1];

iterate depth
{	
  tracef("depth = %i \n", depth);  
  /*
  foreach v,i in tree_results[depth]{
    tree_results[depth+1][i] = tree_results[depth][i]+ 1;
  }
  */
  tree_results[depth+1] = [0:N:1];
  
}until (depth == 1);

int dep = 1;
foreach v, i in tree_results[dep]{
	tracef("Results_tree[%i][%i] : %i\n",dep, i, v);
}
