/**
 * <p>
 * Matrix Multiplies a Row x Col
 * </p>
 * 
 * <pre>
 * Matrix A     Matrix B
 * 
 * a1 a2 a3     b1 b2 b3
 * a4 a5 a6  X  b4 b5 b6
 * a7 a8 a9     b7 b8 b9
 * </pre>
 * <pre>
 * The format is:
 * 
 * java MMRowXCol a1 a2 a3 b1 b4 b7
 * </pre>
 */

public class MMRowXCol {
    public static void main(String[] args) {
        if (args == null || args.length % 2 != 0) {
            throw new IllegalArgumentException("Args must be non-null and even");
        }
        int sum = 0;
        for (int i = 1; i <= args.length / 2; i++) {
            int r = i - 1;
            int c = args.length / 2 + r;

            sum += (intgr(args, r) * intgr(args, c));
        }
        System.out.print(sum);
    }

    public static int intgr(String[] args, int i) {
        return Integer.parseInt(args[i]);
    }
}