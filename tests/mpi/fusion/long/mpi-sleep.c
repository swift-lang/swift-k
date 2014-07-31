
/**
 * Simple Hydra test
 *
 * usage: mpi-sleep <duration>
 *
 * The copy of this program in the JETS repository has profiling
 * information that was removed here.
 *
 * */

#include <assert.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/utsname.h>
#include <unistd.h>

#include <mpi.h>

// #include "./profile.h"

// There is a Makefile option to turn on debugging
#ifdef ENABLE_DEBUG
#define DEBUG(...) __VA_ARGS__
#define debug(...)                              \
  { printf("%i: ", mpi_rank);                   \
    printf(__VA_ARGS__);                        \
    fflush(stdout); }
#else
#define DEBUG(...)
#define debug(...)
#endif

#define max(a, b) (((a) > (b)) ? (a) : (b))
#define min(a, b) (((a) < (b)) ? (a) : (b))

void print_help(void);
void startup(void);
void write_header(void);
void write_data(void);
void crash(char* msg);
void check(void* value, char* msg);

int mpi_size, mpi_rank;

const int INPUT_MAX = 10;
int input_count = 0;

char hostname[1024];
char output[1024];

bool enable_write_header = false;

int main(int argc, char* argv[])
{
  int duration = -1;
  char* inputs[INPUT_MAX];
  output[0] = '\0';

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
        enable_write_header = true;
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

  // WRITE HEADER
  if (enable_write_header)
    write_header();

  // SLEEP
  debug("sleep %i\n", duration);
  sleep(duration);

  // WRITE OUTPUT
  write_data();

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

  if (mpi_rank == 0)
    if (strlen(output) == 0)
    {
      printf("No output file!\n");
      exit(1);
    }

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

/**
   An output header written only by rank 0
*/
void write_header()
{
  if (mpi_rank == 0)
  {
    debug("write_header(): %s\n", output);
    FILE* file = fopen(output, "w");
    check(file, "could not write to file!");
    fprintf(file, "rank 0: header\n");
    fclose(file);
  }
}

void read_hostname()
{
  struct utsname name;
  if (uname (&name) == -1)
  {
    printf("Could not call uname()!\n");
    MPI_Abort(MPI_COMM_WORLD, 1);
  }

  strcpy(hostname, name.nodename);

  /*
  char* filename = "/etc/HOSTNAME";
  FILE* file = fopen(filename, "r");
  if (!file)
  {
    printf("Could not open: %s\n", filename);
    MPI_Abort(MPI_COMM_WORLD, 1);
  }
  fscanf(file, "%s", hostname);
  fclose(file);
  */
}

void write_data()
{
  debug("write_data(): %s\n", output);
  read_hostname();
  FILE* file = fopen(output, "a");
  check(file, "could not write to file!");
  fprintf(file, "rank: %i/%i %s\n",
          mpi_rank, mpi_size, hostname);
  fclose(file);
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
