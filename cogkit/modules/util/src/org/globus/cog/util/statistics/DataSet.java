// Analysing 2d data

// ussage

// DataSet d = new DataSet(10, "ten values");
// double [10] y; // get this data from somewhere

// d.set(y, "ten values")

// d.printAnalysis(System.out);
// d.printData(System.out);

package org.globus.cog.util.statistics;


class DataSet {

    /*
    public double [] v;
    private string  v_name;
    private int     v_n;
    private boolean v_analyzed = false;
    private double  v_sum;
    private double  v_min;
    private double  v_max;
    private double  v_varianz;
    private double  v_stdev;

    public void init(int n, String name){
	analyzed = false;
	v = new double[n);
	v_n = n;
	v_name = name;
    }

    public void update() {
	analyzed = false;
	analyze();
    }

    public void analyze() {
	if (analyzed) {
	    return;
	}

	double min = 9999999.0; // should be maxdouble
	double max = -min; // should be mindouble
	double sum = 0;
	double delta2; // temp var
	double value; // temp var

	double[] times = new double[count];

	for (int i=0 ; i < size(); i++){
	    
	    value = v[i];
	    if (min > value) {
		min = value;
	    }
	    if (max < value) {
		max = value;
	    }
	    sum = sum + value;
	}
	double v_ mean = v_sum / n;

	sum = 0; 
	for (int i = 0; i < size(); i++){
	    delta2 = mean - v[i];
	    delta2 = delta2 * delta2;

	    sum = sum + delta2; 
	}
	v_varianz = sum / n;
	v_stdev   = Math.sqrt(v_varianz);
    }

    public void set (int i, double a) {
	v[i] = a;
    }
	
    public void set (int n, double a[]){
	for (int i=0 ; i < size(); i++){
	    v[i] = a[i];
	}
    }

    public double min(){
	analyze();
	return v_min;
    }

    public double max(){
	analyze();
	return v_max;
    }

    public double varianz(){
	analyze();
	return v_varianz;
    }

    public double stddeviation(){
	analyze();
	return v_stddeviation;
    }

    public int size(){
	return v_n;
    }

    public void printAnalysis(PrintStream out){
	out.println("name            :", v_name);
	out.println("  min           :", min());
	out.println("  max           :", max());
	out.println("  average       :", average());
	out.println("  variance      :", vaianz());
	out.println("  std Derivation:", stddeviation());
    }

    public void printData(PrintStream out){
	out.println("name            :", name);
	int i;
	for (i=0; i < n; i++) {
	    out.println("  v " + i + "   :", v[i]);
	}
    }
    */
}