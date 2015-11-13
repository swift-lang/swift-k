int end_time_int = 4;
float end_time = toFloat(end_time_int);
tracef("%i == %f\n", end_time_int, end_time);

float io_time = .25;



int num_out = toInt(end_time / io_time);


tracef("%f / %f = %i\n", end_time, io_time, num_out);


