#include <stdio.h>
#include <mpi.h>
#include <unistd.h>
#include <fcntl.h>

int main (argc, argv)
int argc;
char *argv[];
{
  int rank, size;

  MPI_Init (&argc, &argv);  /* starts MPI */

  char *ifile = argv[1];
  char *ofile = argv[2];
  int sleeptime = atoi(argv[3]);

  MPI_Comm_rank (MPI_COMM_WORLD, &rank); /* get current MPI process id (rank) */
  MPI_Comm_size (MPI_COMM_WORLD, &size); /* get number of MPI processes */

  if (rank==0) {
    printf("ifile=%s ofile=%s sleeptime=%d\n", ifile, ofile, sleeptime);

    int ifd = open(ifile,O_RDONLY);
    int ofd = open(ofile,O_WRONLY|O_CREAT,0664);
    char buf[1024*1024];
    for(;;) {
      int rc = read(ifd, buf, sizeof(buf));
      if (rc <= 0) {
        close(ifd);
        close(ofd);
        break;
      }
      write(ofd, buf, rc);
    }
  }

  sleep(sleeptime);

  char host[512];
  gethostname(host, 512);
  printf( "Hello from process %d of %d on %s\n", rank, size, host);
  MPI_Finalize();
  return 0;
}
