import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This Java implementation demonstrates the Dual-Matrix Approach for solving
 * the Transportation Problem, as proposed by Ping Ji and K.F. Chu in their
 * paper, "A Dual-Matrix Approach to the Transportation Problem," published in
 * the Asia-Pacific Journal of Operational Research, Volume 19, 2002, pages 35-45.
 *
 * @author Luca Azalim
 */

public class Main {

    static int[] supply, demand, A, Y, Q, P, v, u, vIndex;
    static int[][] costs, gama, theta, D;
    static int m, n, iteration, dimension, psi, k = 0, s = 0, t = 0;

    static String ANSI_RESET = "\033[0m", ANSI_YELLOW = "\033[33m", ANSI_GREEN = "\033[32m";

    public static void main(String[] args) throws IOException {

        readInput();
        initialize();
        iterate();

        System.out.println(ANSI_GREEN + "# Total transportation cost: " + psi);

        for (int i = 0; i < gama.length; i++) {
            System.out.println("# Route " + Arrays.toString(gama[i]) + " -> " + Y[i]);
        }

        System.out.print(ANSI_RESET);

    }

    static void readInput() throws IOException {

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("Enter the input file name: ");
            String fileName = scanner.nextLine();

            Path path = Path.of("input", fileName);

            if (Files.notExists(path)) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {

                String[] countLine = reader.readLine().split("\\s+");

                m = Integer.parseInt(countLine[0]);
                n = Integer.parseInt(countLine[1]);

                supply = new int[m];
                demand = new int[n];
                costs = new int[m][n];

                for (int i = 0; i < m; i++) {
                    supply[i] = Integer.parseInt(reader.readLine());
                }

                for (int i = 0; i < n; i++) {
                    demand[i] = Integer.parseInt(reader.readLine());
                }

                for (int i = 0; i < m; i++) {

                    String[] costLine = reader.readLine().split("\\s+");

                    for (int j = 0; j < n; j++) {
                        costs[i][j] = Integer.parseInt(costLine[j]);
                    }

                }

            }

        }

    }

    static void initialize() {

        System.out.println(ANSI_YELLOW + "Step 0" + ANSI_RESET);

        // Step 0.1

        A = new int[m + n];

        System.arraycopy(demand, 0, A, 0, n);

        for (int i = 0; i < m; i++) {
            A[n + i] = supply[i] * -1;
        }

        System.out.println("A = " + Arrays.toString(A));

        // Step 0.2 and 0.3

        gama = new int[m + n][2];

        u = new int[m];
        v = new int[n];
        vIndex = new int[n];

        Arrays.fill(u, 0);

        System.out.println("u = " + Arrays.toString(u));

        Arrays.fill(v, Integer.MAX_VALUE);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {

                if (costs[j][i] < v[i]) {
                    v[i] = costs[j][i];
                    vIndex[i] = j;
                    gama[i] = new int[]{j, i};
                }

            }
        }

        System.out.println("v = " + Arrays.toString(v));
        System.out.println("vIndex = " + Arrays.toString(vIndex));

        for (int i = 0; i < m; i++) {
            gama[n + i] = new int[]{i, -1};
        }

        System.out.println("Γ = " + Arrays.deepToString(gama));

        // Step 0.4

        dimension = m + n;

        D = new int[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (i == j && i < n) {
                    D[i][j] = 1;
                } else if (i < n && j >= n && j == n + vIndex[i]) {
                    D[i][j] = -1;
                } else if (i == j) {
                    D[i][j] = -1;
                }

            }
        }

        System.out.println("D = ");
        printMatrix(D);

        psi = computeObjective(v, u);

        System.out.println("ψ = " + psi);

    }

    static void iterate() {

        System.out.println(ANSI_YELLOW + "# Step 1..3 (x" + ++iteration + ")" + ANSI_RESET);

        // Step 1.1

        Y = dotProduct(A, D);

        System.out.println("Y = " + Arrays.toString(Y));

        // Step 1.2

        k = indexOfMinValue(Y);

        System.out.println("k = " + k);

        // Step 1.3

        if (Y[k] >= 0) {
            System.out.println("Solution is optimal!");
            return;
        }

        // Step 2.1

        Q = new int[n];

        for (int i = 0; i < n; i++) {
            Q[i] = D[i][k];
        }

        System.out.println("Q = " + Arrays.toString(Q));

        P = new int[m];

        for (int j = 0; j < m; j++) {
            P[j] = D[n + j][k];
        }

        System.out.println("P = " + Arrays.toString(P));

        // Step 2.2

        theta = new int[m][n];
        int minTheta = Integer.MAX_VALUE;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {

                if (D[i][j] != 0 || P[i] - Q[j] <= 0) {
                    continue;
                }

                theta[i][j] = costs[i][j] + u[i] - v[j];

                // Step 2.3

                if (theta[i][j] < minTheta) {
                    minTheta = theta[i][j];
                    s = i;
                    t = j;
                }

            }
        }

        System.out.println("Θ = ");
        printMatrix(theta);

        System.out.println("s = " + s);
        System.out.println("t = " + t);
        System.out.println("Θst = " + theta[s][t]);

        update();

        iterate();

    }

    static void update() {

        // Step 3.1

        // Step 3.1.1

        for (int l = 0; l < dimension; l++) {
            D[l][k] *= -1;
        }

        // Step 3.1.2

        for (int l = 0; l < dimension; l++) {
            for (int r = 0; r < dimension; r++) {

                if (r == k) continue;
                D[l][r] += (D[s + n][r] - D[t][r]) * D[l][k];

            }
        }

        System.out.println("D = ");
        printMatrix(D);

        // Step 3.2

        gama[k] = new int[]{s, t};

        System.out.println("Γ = " + Arrays.deepToString(gama));

        // Step 3.3

        for (int i = 0; i < m; i++) {
            u[i] = u[i] - theta[s][t] * P[i];
        }

        System.out.println("u = " + Arrays.toString(u));

        for (int j = 0; j < n; j++) {
            v[j] = v[j] - theta[s][t] * Q[j];
        }

        System.out.println("v = " + Arrays.toString(v));

        psi = computeObjective(v, u);

        System.out.println("ψ = " + psi);

    }

    static int computeObjective(int[] v, int[] u) {

        int firstSummation = 0;

        for (int j = 0; j < n; j++) {
            firstSummation += demand[j] * v[j];
        }

        int secondSummation = 0;

        for (int i = 0; i < m; i++) {
            secondSummation += supply[i] * u[i];
        }

        return firstSummation - secondSummation;

    }

    static int[] dotProduct(int[] A, int[][] D) {

        int m = A.length;
        int n = D[0].length;

        int[] Y = new int[n];

        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                Y[j] += A[i] * D[i][j];
            }
        }

        return Y;

    }

    static int indexOfMinValue(int[] numbers) {

        int minValue = Integer.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < numbers.length; i++) {

            int n = numbers[i];

            if (n < minValue) {
                minValue = n;
                index = i;
            }

        }

        return index;

    }

    static void printMatrix(int[][] matrix) {

        for (int[] i : matrix) {

            for (int j : i) {
                System.out.print(j + "\t");
            }

            System.out.println();

        }

    }

}
