
/**
 * Simple Hydra test
 *
 * usage: mpi-cp <input> <output>
 *
 * Rank 0 reads the input file and sends it to rank 1.
 * Rank 1 recvs the data and writes the output.
 */

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

#include <mpi.h>

#define max(a, b) (((a) > (b)) ? (a) : (b))
#define min(a, b) (((a) < (b)) ? (a) : (b))

void print_help(void);
void read_input(const char *restrict filename, char** data, int* size);
void write_output(const char *restrict filename, char* data, int size);
void startup(void);

const int chunk = 64*1024;

int main(int argc, char* argv[])
{
  int mpi_size, mpi_rank;

  MPI_Init(&argc, &argv);

  MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
  MPI_Comm_rank(MPI_COMM_WORLD, &mpi_rank);

  startup();

  // system("/bin/hostname");
  // printf("size: %i\n", mpi_size);
  // printf("rank: %i\n", mpi_rank);

  char* data;
  int size;

  if (argc != 3)
  {
    print_help();
    return 1;
  }

  if (mpi_rank == 0)
  {
    read_input(argv[1], &data, &size);
    printf("size: %i\n", size);
    MPI_Send(&size, 1, MPI_INT,
             1, 1, MPI_COMM_WORLD);
    MPI_Send(data, size, MPI_CHAR,
             1, 1, MPI_COMM_WORLD);
  }
  else
  {
    MPI_Status status;

    MPI_Recv(&size, 1, MPI_INT,
             0, 1, MPI_COMM_WORLD, &status);
    printf("size: %i\n", size);

    data = malloc(size);
    assert(data);
    MPI_Recv(data, size, MPI_CHAR,
             0, 1, MPI_COMM_WORLD, &status);

    write_output(argv[2], data, size);
  }
  free(data);

  MPI_Finalize();
  return EXIT_SUCCESS;
}

void startup()
{
  // system("/bin/hostname");
  // printf("size: %i\n", mpi_size);
  // printf("rank: %i\n", mpi_rank);
  char* pwd = getenv("PWD");
  printf("PWD: %s\n", pwd);
  fflush(NULL);
}

/**
   Allocates memory for *data
*/
void read_input(const char* restrict filename, char** data, int* size)
{
  char* result;
  FILE *file;

  printf("read: %s\n", filename);

  struct stat filestat;

  int error = stat(filename, &filestat);
  assert(error == 0);
  int s = filestat.st_size;
  int actual, c;

  result = malloc(s);

  file = fopen(filename, "r");
  assert(file);

  int count = 0;
  while (count < s)
  {
    c = min(chunk, s-count);
    actual = fread(result+count, 1, c, file);
    count += actual;
  }

  fclose(file);

  *size = s;
  *data = result;
}

void write_output(const char* restrict filename,
                   char* data, int size)
{
  FILE *file;

  int actual, c;

  printf("write: %s\n", filename);

  file = fopen(filename, "w");
  assert(file);

  int count = 0;
  while (count < size)
  {
    c = min(chunk, size-count);
    actual = fwrite(data+count, 1, c, file);
    count += actual;
  }

  fclose(file);
}

void print_help()
{
  printf("usage: <input> <output>\n");
}
