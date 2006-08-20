/**
 * <p>
 * GoWorld prints "hello world." iteration times and sleeps sleep between each
 * iteration.
 * </p>
 */

public class GoWorld {
    /* need this for the set method, otherwise could use Integer */
    static class MyInt {
        private int i;

        public MyInt(int i) {
            setIntValue(i);
        }
        public int intValue() {
            return i;
        }
        public void setIntValue(int i) {
            this.i = i;
        }
    }

    public static void main(String[] args) {
        MyInt iterations = new MyInt(3);
        MyInt sleep = new MyInt(2);
        MyInt arg = iterations;

        /* quick getopt */
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if ("-help".equals(args[i])) {
                    help();
                    return;
                } else if ("-i".equals(args[i])) {
                    arg = iterations;
                } else if ("-s".equals(args[i])) {
                    arg = sleep;
                } else {
                    try {
                        int integerValue = Integer.parseInt(args[i]);
                        arg.setIntValue(integerValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                        help();
                    }
                }
            }
        }
        /* run our program */
        goWorld(iterations.intValue(), sleep.intValue());
    }

    public static void goWorld(int iterations, int sleep) {
        System.out.println("GoWorld Iterations: '" + iterations + "' Sleep: '"
                + sleep + "'");
        for (int index = 0; index < iterations; index++) {
            System.out.println("hello world");
            try {
                /* sleep is in ms so multiply by 1000 */
                Thread.sleep(sleep * 1000);
            } catch (InterruptedException e) {
                /* must catch exceptions for java */
            }
        }
    }

    public static void help() {
        System.out.println("GoWorld -i ITERATIONS -s SLEEP");
        System.out.println(" -help         - displays help for this program");
        System.out
                .println(" -i ITERATIONS - an integer specifying the number of times to print 'hello world'; default 3");
        System.out
                .println(" -s SLEEP      - an integer specifying the number of seconds to sleep between each iteration default 2");
    }
}