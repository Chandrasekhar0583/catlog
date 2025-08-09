import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.*;

public class PolynomialSecretFinder {

    public static void main(String[] args) throws Exception {
        // Read JSON file content
        String content = new String(Files.readAllBytes(Paths.get("./input2.json")));
        JSONObject json = new JSONObject(content);

        JSONObject keys = json.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");

        int[] x = new int[k];
        BigDecimal[] y = new BigDecimal[k];

        int index = 0;
        for (String key : json.keySet()) {
            if (key.equals("keys")) continue;
            if (index >= k) break;

            JSONObject root = json.getJSONObject(key);
            int base = Integer.parseInt(root.getString("base"));
            String val = root.getString("value");

            x[index] = Integer.parseInt(key);
            BigInteger valInt = new BigInteger(val, base);
            y[index] = new BigDecimal(valInt);

            index++;
        }

        BigDecimal[][] A = new BigDecimal[k][k];
        int degree = k - 1;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int power = degree - j;
                A[i][j] = BigDecimal.valueOf(powInt(x[i], power));
            }
        }

        BigDecimal[] coeffs = solveLinearSystem(A, y);
        System.out.println("Constant term c = " + coeffs[k - 1].toBigInteger().toString());
    }

    static long powInt(int base, int exp) {
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }

    static BigDecimal[] solveLinearSystem(BigDecimal[][] A, BigDecimal[] b) {
        int n = b.length;
        BigDecimal[][] M = new BigDecimal[n][n + 1];
        MathContext mc = new MathContext(30);

        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = b[i];
        }

        for (int pivot = 0; pivot < n; pivot++) {
            int max = pivot;
            for (int i = pivot + 1; i < n; i++) {
                if (M[i][pivot].abs().compareTo(M[max][pivot].abs()) > 0) max = i;
            }
            BigDecimal[] temp = M[pivot];
            M[pivot] = M[max];
            M[max] = temp;

            BigDecimal pivotVal = M[pivot][pivot];
            for (int j = pivot; j <= n; j++) {
                M[pivot][j] = M[pivot][j].divide(pivotVal, mc);
            }

            for (int i = pivot + 1; i < n; i++) {
                BigDecimal factor = M[i][pivot];
                for (int j = pivot; j <= n; j++) {
                    M[i][j] = M[i][j].subtract(factor.multiply(M[pivot][j], mc), mc);
                }
            }
        }

        BigDecimal[] x = new BigDecimal[n];
        for (int i = n - 1; i >= 0; i--) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i + 1; j < n; j++) {
                sum = sum.add(M[i][j].multiply(x[j]));
            }
            x[i] = M[i][n].subtract(sum);
        }
        return x;
    }
}
