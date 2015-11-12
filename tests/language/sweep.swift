/* Inputs: standard dictionary, user file, and list of viscosities */
int max_dof = 256;
//float[] orders = [4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0];
int[] orders = [4];
//float[] elms = [64.0, 40.0, 32.0, 26.0, 20.0, 18.0, 16.0];
int[] elms = [8];

int[int] dealias;
dealias[4]  =  6;
dealias[6]  = 10;
dealias[8]  = 12;
dealias[10] = 16;
dealias[12] = 18;
dealias[14] = 22;
dealias[16] = 24;

int nwrite=128;


int nodes = 128;
int mode = 32;
int job_wall = 60;

int nstep = 48;
int job_step = 1;
int io_step = 1;

float io_time = 0.25;
float job_time = 1.0;
int outputs_per_job = 4;

int j0 = 0;
string analysis = "RTI";
int post_nodes = 0;

int aspect = 8;

foreach order,i in orders {
  foreach elm,ii in elms {
    if (order * elm <= max_dof) {

      string override = sprintf("{ \"order\": %i, \"dealiasing_order\": %i, \"shape_mesh\": [%i, %i, %i], \"procs\": %i, \"io_files\": %i, \"io_time\": %f }", 
                                order, dealias[order], elm, elm, elm*aspect, mode*nodes, -nwrite, io_time);       
    }
  }
}
