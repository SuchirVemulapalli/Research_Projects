import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class TestParallelSort {
    public static void main(String[] args) {
        writeRandomArrays();
        int[][] arrays = {
                readArray("10.txt"),
                readArray("100.txt"),
                readArray("1000.txt"),
                readArray("10000.txt"),
                readArray("20000.txt"),
                readArray("100000.txt"),
                readArray("1000000.txt"),
        };

        for (int t = 1; t <= 8; t *= 2) {
            testSortClass(ParallelMergeSort.class, t, arrays);
            testSortClass(ParallelQuickSort.class, t, arrays);
        }
    }

    private static <T extends RecursiveAction> void testSortClass(Class<T> sorterClass, int threadCount, int[][] testArrays) {
        System.out.println("Testing " + sorterClass + " with " + threadCount + " threads");
        ForkJoinPool pool = new ForkJoinPool(threadCount);

        try {
            Constructor<T> ctor = sorterClass.getDeclaredConstructor(int[].class);

            for (int[] originalArray : testArrays) {
                System.out.println("- Sorting array of length " + originalArray.length);

                long[] times = new long[5];
                for (int i = 0; i < 10; i++) {
                    int[] arr = Arrays.copyOf(originalArray, originalArray.length);
                    RecursiveAction task = ctor.newInstance(arr);
                    long ns = testSortArray(pool, task);
                    if (i >= 10 - times.length) {
                        times[i - 10 + times.length] = ns;
                    }
                    if (!verifySorted(arr)) {
                        throw new RuntimeException();
                    }
                }
                Arrays.sort(times);
                System.out.println("  - Median of last " + times.length + " runs is " + times[times.length / 2] + "ns");
            }
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private static long testSortArray(ForkJoinPool pool, RecursiveAction task) {
        long start = System.nanoTime();
        pool.invoke(task);
        long end = System.nanoTime();
        return end - start;
    }

    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static boolean verifySorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                System.err.println(Arrays.toString(arr));
                System.err.println("Array is not sorted: A[" + (i-1) + "]=" + arr[i - 1] + " > A[" + i + "]=" + arr[i]);
                return false;
            }
        }
        return true;
    }

    private static void writeArray(String file, int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(", ");
            }
        }
        sb.append('\n');

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(sb.toString());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static int[] readArray(String file) {
        String[] numstr = null;
        try (Scanner scan = new Scanner(new File(file))) {
            numstr = scan.nextLine().split(", ");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int[] array = new int[numstr.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.parseInt(numstr[i]);
        }
        return array;
    }

    private static int[] generateRandomArray(long seed, int count) {
        Random r = new Random(seed);
        int max = (int) Math.pow(10, Math.ceil(Math.log10(count)));
        IntStream is = r.ints(count, 0, max);
        return is.toArray();
    }
    private static int[] generateShuffledArray(long seed, int count) {
        List<Integer> col = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            col.add(i);
        }
        Random r = new Random(seed);
        Collections.shuffle(col, r);
        int[] array = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = col.get(i);
        }
        return array;
    }
    private static void writeRandomArrays() {
        writeArray("10.txt", generateRandomArray(42, 10));
        writeArray("100.txt", generateRandomArray(42, 100));
        writeArray("1000.txt", generateRandomArray(42, 1000));
        writeArray("10000.txt", generateRandomArray(42, 10000));
        writeArray("20000.txt", generateRandomArray(42, 20000));
        writeArray("100000.txt", generateRandomArray(42, 100000));
        writeArray("1000000.txt", generateRandomArray(42, 1000000));
    }
}
