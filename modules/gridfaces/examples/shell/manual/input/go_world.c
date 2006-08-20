#include <stdio.h>
#include <stdlib.h>

void get_args(int argc, char** argv, int* a_value, int* b_value)
{
    int i;

    /* Start at i = 1 to skip the command name. */

    for (i = 1; i < argc; i++) {

	/* Check for a switch (leading "-"). */

	if (argv[i][0] == '-') {

	    /* Use the next character to decide what to do. */

	    switch (argv[i][1]) {

		case 'i':	*a_value = atoi(argv[++i]);
				break;

		case 's':	*b_value = atoi(argv[++i]);
				break;

		default:	fprintf(stderr,
				"Unknown switch %s\n", argv[i]);
	    }
	}
    }
}

main(int argc, char** argv)
{
    /* Set defaults for all parameters: */
    int count;
    int iterations = 3;
    int sleepTime = 2;

    get_args(argc, argv, &iterations, &sleepTime);

    printf("Iterations: '%d' | sleepTime: '%d'\n", iterations, sleepTime);

    for(count=0;count<iterations;count++) {
      printf("hello world.\n");
      sleep(sleepTime);
    }
    printf("\n-----\nprogram terminated.\n");

}
