#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include <assert.h>

#define error(format, ...) fprintf(stderr, format , __VA_ARGS__)


int run_server(int myrank, int nprocs)
{
    printf("I am server!\n");
    return 0;
}

int run_client(int myrank, int nprocs)
{
    printf("[%d] I am client!\n", myrank);
    return 0;
}

int main(int argc, char **argv)
{
    int nprocs;
    int myrank;
    int stat;
    int namelen;
    char processor_name[MPI_MAX_PROCESSOR_NAME];

    MPI_Init(&argc, &argv);
    stat = MPI_Comm_size(MPI_COMM_WORLD, &nprocs);
    if ( stat != 0 ) error ("MPI_Comm_size returned an error code : %d", stat);

    stat = MPI_Comm_rank(MPI_COMM_WORLD, &myrank);
    if ( stat != 0 ) error ("MPI_Comm_rank returned an error code : %d", stat);

    MPI_Get_processor_name(processor_name, &namelen);

    printf("Process %d on %s out of %d\n", myrank, processor_name, nprocs);
    //printf("Nprocs: %d, Nrank: %d \n", nprocs, myrank);

    if ( myrank == 0 ){
        run_server(myrank, nprocs);
    }else{
        run_client(myrank, nprocs);
    }

    MPI_Finalize();
    return 0;
}
