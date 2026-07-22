import java.util.ArrayList;
import java.util.List;

public class JavaExercises {

    /** Returns an array [1, 2, 3, 4, 5, 6] */
    public static int[] makeDice() {
        return new int[]{1, 2, 3, 4, 5, 6};
    }

    /** Returns the order depending on the customer. */
    public static String[] takeOrder(String customer) {
        if ("Ergun".equals(customer)) {
            return new String[]{"beyti", "pizza", "hamburger", "tea"};
        } else if ("Erik".equals(customer)) {
            return new String[]{"sushi", "pasta", "avocado", "coffee"};
        } else {
            return new String[3];
        }
    }

    /** Returns the positive difference between max and min. */
    public static int findMinMax(int[] array) {
        int min = array[0];
        int max = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) min = array[i];
            if (array[i] > max) max = array[i];
        }

        return max - min;
    }

    /** Uses recursion to compute hailstone sequence. */
    public static List<Integer> hailstone(int n) {
        return hailstoneHelper(n, new ArrayList<>());
    }

    private static List<Integer> hailstoneHelper(int x, List<Integer> list) {
        list.add(x);

        if (x == 1) {
            return list;
        }

        int next = (x % 2 == 0) ? x / 2 : x * 3 + 1;
        return hailstoneHelper(next, list);
    }
}