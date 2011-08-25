
/**
 * Simple Hydra test
 * No I/O
 *
 * usage: mpi-sleep <duration>
 * */

#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#include <mpi.h>

#include "./profile.h"

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

const int OUTPUT_MAX = 10;
int output_count = 0;

bool write_header = false;

int main(int argc, char* argv[])
{
  profile_init(100);

  int duration = -1;
  char* inputs[INPUT_MAX];
  char* outputs[OUTPUT_MAX];

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
        if (output_count >= OUTPUT_MAX)
          crash("too many outputs!");
        outputs[output_count++] = optarg;
        debug("output: %s\n", optarg);
        break;
      case 'v':
        write_header = true;
        break;
    }
  }

  MPI_Init(&argc, &argv);

  profile_entry(MPI_Wtime(), strdup("job start"));

  debug("mpi-sleep\n");

  MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
  MPI_Comm_rank(MPI_COMM_WORLD, &mpi_rank);

  profile_entry(MPI_Wtime(), strdup("rank"));

  startup();

  if (optind >= argc)
  {
    if (mpi_rank == 0)
      print_help();
    return EXIT_FAILURE;
  }

  int count = sscanf(argv[optind], "%d", &duration);
  if (count != 1)
  {
    printf("Did not receive integer duration argument!\n");
    return EXIT_FAILURE;
  }

  // INTRO BARRIER
  debug("barrier1\n");
  profile_entry(MPI_Wtime(), strdup("barrier1 start"));
  MPI_Barrier(MPI_COMM_WORLD);
  profile_entry(MPI_Wtime(), strdup("barrier1 done"));
  debug("sleep %i\n", duration);

  // SLEEP
  sleep(duration);

  // WRITE OUTPUTS
  if (output_count > 0)
    profile_entry(MPI_Wtime(), strdup("write start"));
  for (int i = 0; i < output_count; i++)
  {
    char tmp[128];
    sprintf(tmp, "%s-%i", outputs[i], mpi_rank);
    debug("write to: %s\n", tmp);
    FILE* file = fopen(tmp, "w");
    check(file, "could not write to file!");
    if (write_header)
      fprintf(file, "rank: %i/%i\n", mpi_rank, mpi_size);
    fclose(file);
  }
  if (output_count > 0)
    profile_entry(MPI_Wtime(), strdup("write done"));

  // FINAL BARRIER
  debug("barrier2\n");
  profile_entry(MPI_Wtime(), strdup("barrier2 start"));

  MPI_Barrier(MPI_COMM_WORLD);
  profile_entry(MPI_Wtime(), strdup("barrier2 done"));

  debug("job done\n");
  MPI_Finalize();
  profile_write(mpi_rank, stdout);
  profile_finalize();
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
  printf("usage: <duration>\n");
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
