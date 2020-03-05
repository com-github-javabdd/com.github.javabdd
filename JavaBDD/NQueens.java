import net.sf.javabdd.*;

/**
 * @author John Whaley
 */
public class NQueens {
    static BDDFactory B;

    static boolean TRACE;
    static int N; /* Size of the chess board */
    static BDD[][] X; /* BDD variable array */
    static BDD queen; /* N-queen problem expressed as a BDD */
    static BDD solution; /* One solution */

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("USAGE:  java NQueens N");
            return;
        }
        N = Integer.parseInt(args[0]);
        if (N <= 0) {
            System.err.println("USAGE:  java NQueens N");
            return;
        }
        
        TRACE = true;
        long time = System.currentTimeMillis();
        runTest();
        freeAll();
        time = System.currentTimeMillis() - time;
        System.out.println("Time: "+time/1000.+" seconds");
        BDDFactory.CacheStats cachestats = B.getCacheStats();
        if (cachestats != null && cachestats.uniqueAccess > 0) {
            System.out.println(cachestats);
        }
        B.done();
        B = null;
    }

    public static double runTest() {

        if (B == null) {
            /* Initialize with reasonable nodes and cache size and NxN variables */
            String numOfNodes = System.getProperty("bddnodes");
            int numberOfNodes;
            if (numOfNodes == null)
                numberOfNodes = (int) (Math.pow(4.42, N-6))*1000;
            else
                numberOfNodes = Integer.parseInt(numOfNodes);
            String cache = System.getProperty("bddcache");
            int cacheSize;
            if (cache == null)
                cacheSize = 1000;
            else
                cacheSize = Integer.parseInt(cache);
            numberOfNodes = Math.max(1000, numberOfNodes);
            B = BDDFactory.init(numberOfNodes, cacheSize);
        }
        if (B.varNum() < N * N) B.setVarNum(N * N);

        queen = B.universe();

        int i, j;

        /* Build variable array */
        X = new BDD[N][N];
        for (i = 0; i < N; i++)
            for (j = 0; j < N; j++)
                X[i][j] = B.ithVar(i * N + j);

        /* Place a queen in each row */
        for (i = 0; i < N; i++) {
            BDD e = B.zero();
            for (j = 0; j < N; j++) {
                e.orWith(X[i][j].id());
            }
            queen.andWith(e);
        }

        /* Build requirements for each variable(field) */
        for (i = 0; i < N; i++)
            for (j = 0; j < N; j++) {
                if (TRACE) System.out.print("Adding position " + i + "," + j+"   \r");
                build(i, j);
            }

        solution = queen.satOne();
        
        double result = queen.satCount();
        /* Print the results */
        if (TRACE) {
            System.out.println("There are " + (long) result + " solutions.");
            double result2 = solution.satCount();
            System.out.println("Here is "+(long) result2 + " solution:");
            solution.printSet();
            System.out.println();
        }

        return result;
    }

    public static void freeAll() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                X[i][j].free();
        queen.free();
        solution.free();
    }
    
    static void build(int i, int j) {
        BDD a = B.universe(), b = B.universe(), c = B.universe(), d = B.universe();
        int k, l;

        /* No one in the same column */
        for (l = 0; l < N; l++) {
            if (l != j) {
                BDD u = X[i][l].apply(X[i][j], BDDFactory.nand);
                a.andWith(u);
            }
        }

        /* No one in the same row */
        for (k = 0; k < N; k++) {
            if (k != i) {
                BDD u = X[i][j].apply(X[k][j], BDDFactory.nand);
                b.andWith(u);
            }
        }

        /* No one in the same up-right diagonal */
        for (k = 0; k < N; k++) {
            int ll = k - i + j;
            if (ll >= 0 && ll < N) {
                if (k != i) {
                    BDD u = X[i][j].apply(X[k][ll], BDDFactory.nand);
                    c.andWith(u);
                }
            }
        }

        /* No one in the same down-right diagonal */
        for (k = 0; k < N; k++) {
            int ll = i + j - k;
            if (ll >= 0 && ll < N) {
                if (k != i) {
                    BDD u = X[i][j].apply(X[k][ll], BDDFactory.nand);
                    d.andWith(u);
                }
            }
        }
        
        c.andWith(d);
        b.andWith(c);
        a.andWith(b);
        queen.andWith(a);
    }

}
