import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static int supplyCountM, demandCountN;
    static int[] supplyValues, demandValues;
    static int[][] costValues;

    static int[] A;
    static int[][] gama;
    static int[] u;
    static int[] v;
    static int[] vIndex;
    static int[][] D;
    static int dimension;
    static int psi; // ψ

    public static void main(String[] args) throws IOException {

        readInput();
        initialize();
        iterate();

    }

    static void readInput() throws IOException {

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("Enter the file name: ");
            String fileName = scanner.nextLine();

            Path path = Path.of(fileName);

            if (Files.notExists(path)) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {

                String[] countLine = reader.readLine().split("\\s+");

                supplyCountM = Integer.parseInt(countLine[0]);
                demandCountN = Integer.parseInt(countLine[1]);

                supplyValues = new int[supplyCountM];
                demandValues = new int[demandCountN];
                costValues = new int[supplyCountM][demandCountN];

                for (int i = 0; i < supplyCountM; i++) {
                    supplyValues[i] = Integer.parseInt(reader.readLine());
                }

                for (int i = 0; i < demandCountN; i++) {
                    demandValues[i] = Integer.parseInt(reader.readLine());
                }

                for (int i = 0; i < supplyCountM; i++) {

                    String[] costLine = reader.readLine().split("\\s+");

                    for (int j = 0; j < demandCountN; j++) {
                        costValues[i][j] = Integer.parseInt(costLine[j]);
                    }

                }

            }

        }

    }

    static void initialize() {

        // Step 0.1

        A = new int[supplyCountM + demandCountN];

        System.arraycopy(demandValues, 0, A, 0, demandCountN);

        for (int i = 0; i < supplyCountM; i++) {
            A[demandCountN + i] = supplyValues[i] * -1;
        }

        System.out.println("A = " + Arrays.toString(A));

        // Step 0.2 and 0.3

        gama = new int[supplyCountM + demandCountN][2];

        u = new int[supplyCountM];
        v = new int[demandCountN];
        vIndex = new int[demandCountN];

        Arrays.fill(u, 0);

        System.out.println("u = " + Arrays.toString(u));

        Arrays.fill(v, Integer.MAX_VALUE);

        for (int i = 0; i < demandCountN; i++) {
            for (int j = 0; j < supplyCountM; j++) {
                if (costValues[j][i] < v[i]) {
                    v[i] = costValues[j][i];
                    vIndex[i] = j;
                    gama[i] = new int[]{j, i};
                }
            }
        }

        System.out.println("v = " + Arrays.toString(v));
        System.out.println("vIndex = " + Arrays.toString(vIndex));

        for (int i = 0; i < supplyCountM; i++) {
            gama[demandCountN + i] = new int[]{i, -1};
        }

        System.out.println("Γ = " + Arrays.deepToString(gama));

        // Step 0.4

        dimension = supplyCountM + demandCountN;

        D = new int[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {

                if (i == j && i < demandCountN) {
                    D[i][j] = 1;
                } else if (i < demandCountN
                        && j >= demandCountN
                        && j == demandCountN + vIndex[i]) {
                    D[i][j] = -1;
                } else if (i == j && j >= demandCountN) {
                    D[i][j] = -1;
                }

            }
        }

        System.out.println("D = ");
        printMatrix(D);

        // Computing the objective (ψ)

        psi = computeObjective(v, u);

        System.out.println("ψ = " + psi);

    }

    static void iterate() {

        System.out.println("# Iterating...");

        // Step 1.1

        int[] Y = dotProduct(A, D);

        System.out.println("Y = " + Arrays.toString(Y));

        // Step 1.2

        int k = indexOfMinValue(Y);

        System.out.println("k = " + k);

        // Step 1.3

        if (Y[k] >= 0) {
            System.out.println("Solution is optimal!");
            return;
        }

        // Step 2.1

        int[] Q = new int[demandCountN];

        for (int i = 0; i < demandCountN; i++) {
            Q[i] = D[i][k];
        }

        System.out.println("Q = " + Arrays.toString(Q));

        int[] P = new int[supplyCountM];

        for (int j = 0; j < supplyCountM; j++) {
            P[j] = D[demandCountN + j][k];
        }

        System.out.println("P = " + Arrays.toString(P));

        // Step 2.2

        int[][] theta = new int[supplyCountM][demandCountN];
        int minTheta = Integer.MAX_VALUE;
        int s = 0, t = 0;

        for (int i = 0; i < supplyCountM; i++) {
            for (int j = 0; j < demandCountN; j++) {

                if (D[i][j] != 0) {
                    continue;
                }

                if (P[i] - Q[j] <= 0) {
                    continue;
                }

                theta[i][j] = costValues[i][j] + u[i] - v[j];

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

        // Step 3.1

        for (int l = 0; l < dimension; l++) {

            // Step 3.1.1

            D[l][k] *= -1;

            // Step 3.1.2

            for (int r = 0; r < dimension; r++) {
                if (r == k) continue;
                D[l][r] += (D[s + demandCountN][r] - D[t][r]) * D[l][k];
            }

        }

        System.out.println("D = ");
        printMatrix(D);

        // Step 3.2

        gama[k] = new int[]{s, t};

        System.out.println("Γ = " + Arrays.deepToString(gama));

        // Step 3.3

        for (int i = 0; i < supplyCountM; i++) {
            u[i] = u[i] - theta[s][t] * P[i];
        }

        for (int j = 0; j < demandCountN; j++) {
            v[j] = v[j] - theta[s][t] * Q[j];
        }

        psi = computeObjective(v, u);

        System.out.println("ψ = " + psi);

        iterate();

    }

    static int computeObjective(int[] v, int[] u) {

        int firstSummation = 0;

        for (int j = 0; j < demandCountN; j++) {
            firstSummation += demandValues[j] * v[j];
        }

        int secondSummation = 0;

        for (int i = 0; i < supplyCountM; i++) {
            secondSummation += supplyValues[i] * u[i];
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
