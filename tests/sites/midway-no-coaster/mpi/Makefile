CC=mpicc

CFLAGS = -Wall -g

SRC = $(wildcard *.c)

DEPS = timer.h
OBJS = $(SRC:.c=.o)

EXEC=mpi_sum

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

$(EXEC) : $(OBJS)
	$(CC) -o $@ $^ $(CFLAGS)

clean:
	rm -f $(OBJS) $(EXEC) *~ *out *err
