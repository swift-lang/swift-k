
/**
 * Simple Hydra test
 * No I/O
 *
 * usage: mpi-sleep <duration>
 *
 * The copy of this program in the JETS repository has profiling
 * information that was removed here.
 *
 * */

#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#include <mpi.h>

// #include "./profile.h"

// There is a Makefile option to turn on debugging
#ifdef ENABLE_DEBUG
#define DEBUG(...) __VA_ARGS__
#define debug(...)                              \
  { printf("%i: ", mpi_rank); printf(__VA_ARGS__); }
#else
#define DEBUG(...)
#define debug(...)
#endif

#define max(a, b) (((a) > (b)) ? (a) : (b))
#define min(a, b) (((a) < (b)) ? (a) : (b))

void print_help(void);
void startup(void);
void crash(char* msg);
void check(void* value, char* msg);

int mpi_size, mpi_rank;

const int INPUT_MAX = 10;
int input_count = 0;

char output[1024];

bool write_header = false;

int main(int argc, char* argv[])
{
  int duration = -1;
  char* inputs[INPUT_MAX];

  int opt = -1;
  while ((opt = getopt(argc, argv, "i:o:v")) != -1)
  {
    switch (opt)
    {
      case 'i':
        if (input_count >= INPUT_MAX)
          crash("too many inputs!");
        inputs[input_count++] = strdup(optarg);
        break;
      case 'o':
        strcpy(output, optarg);
      case 'v':
        write_header = true;
        break;
    }
  }

  MPI_Init(&argc, &argv);

  debug("mpi-sleep\n");

  MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
  MPI_Comm_rank(MPI_COMM_WORLD, &mpi_rank);

  startup();

  if (optind >= argc)
  {
    if (mpi_rank == 0)
      print_help();
    MPI_Abort(MPI_COMM_WORLD, 1);
    return 1;
  }

  int count = sscanf(argv[optind], "%d", &duration);
  if (count != 1)
  {
    printf("Did not receive integer duration argument!\n");
    fflush(stdout);
    MPI_Abort(MPI_COMM_WORLD, 1);;
  }

  // INTRO BARRIER
  debug("barrier1\n");
  MPI_Barrier(MPI_COMM_WORLD);
  debug("sleep %i\n", duration);

  // SLEEP
  sleep(duration);

  // WRITE OUTPUT
  if (mpi_rank == 0)
  {
    debug("write to: %s\n", output);
    FILE* file = fopen(output, "w");
    check(file, "could not write to file!");
    if (write_header)
      fprintf(file, "rank: %i/%i\n", mpi_rank, mpi_size);
    fprintf(file, "howdy\n");
    fclose(file);
  }

  // FINAL BARRIER
  debug("barrier2\n");
  MPI_Barrier(MPI_COMM_WORLD);

  debug("job done\n");
  MPI_Finalize();
  return EXIT_SUCCESS;
}

void startup()
{
  char* pwd;
  FILE* file;
  char filename[1024];
  // DEBUG(system("/bin/hostname"));
  debug("size: %i\n", mpi_size);
  debug("rank: %i\n", mpi_rank);
  DEBUG(pwd = getenv("PWD"));
  debug("PWD: %s\n", pwd);
  DEBUG(fflush(NULL));
  // DEBUG(sprintf(filename, "mpi-sleep-%i.out", mpi_rank));
  // DEBUG(file = fopen(filename, "w"));
  // DEBUG(fprintf(file, "OK\n"));
  // DEBUG(fclose(file));
}

void print_help()
{
  printf("usage: <options> <duration>\n");
  fflush(stdout);
}

void crash(char* msg)
{
  printf("mpi-sleep: %s\n", msg);
  exit(1);
}

void check(void* value, char* msg)
{
  if (!value)
    crash(msg);
}
