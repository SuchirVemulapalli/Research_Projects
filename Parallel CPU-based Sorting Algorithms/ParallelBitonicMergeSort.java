import java.util.Arrays;
import java.util.concurrent.*;
import java.util.Random;

public class ParallelBitonicMergeSort {

    public static void main(String[] args) {
        testParallelBitonicSort();
    }

    public static void testParallelBitonicSort() {
        long[] durations = new long[5];

        for (int i = 0; i < 5; i++) {
            int[] arr = generateRandomArray(100);
            arr = padArrayToPowerOf2(arr);
            long startTime = System.nanoTime();

            ParallelBitonicMergeSort sorter = new ParallelBitonicMergeSort();
            sorter.parallelBitonicSort(arr, true);

            long endTime = System.nanoTime();
            durations[i] = (endTime - startTime);

            System.out.println("\nRun " + (i + 1) + " - Time taken: " + durations[i] + " nanoseconds");
            System.out.println("Sorted: " + isSorted(arr));
        }

        Arrays.sort(durations);
        long medianDuration;
        if (durations.length % 2 == 0) {
            medianDuration = (durations[durations.length / 2 - 1] + durations[durations.length / 2]) / 2;
        } else {
            medianDuration = durations[durations.length / 2];
        }

        System.out.println("\nMedian runtime over 5 runs: " + medianDuration + " nanoseconds");
    }



    public static int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(1000);
        }
        return arr;
    }

    public static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }

    public void parallelBitonicSort(int[] arr, boolean direction) {
        int n = arr.length;
        ForkJoinPool pool = new ForkJoinPool(8);
        pool.invoke(new ParallelBitonicSortTask(arr, 0, n, direction));
        pool.shutdown();
    }

    private class ParallelBitonicSortTask extends RecursiveAction {
        private int[] arr;
        private int start;
        private int length;
        private boolean direction;

        public ParallelBitonicSortTask(int[] arr, int start, int length, boolean direction) {
            this.arr = arr;
            this.start = start;
            this.length = length;
            this.direction = direction;
        }

        @Override
        protected void compute() {
            if (length > 1) {
                int k = length / 2;
                invokeAll(
                        new ParallelBitonicSortTask(arr, start, k, true),
                        new ParallelBitonicSortTask(arr, start + k, k, false)
                );
                parallelBitonicMerge(arr, start, length, direction);
            }
        }

        private void parallelBitonicMerge(int[] arr, int start, int length, boolean direction) {
            if (length > 1) {
                int k = length/2;
                for (int i = start; i < start + k; i++) {
                    compareAndSwap(arr, i, i + k, direction);
                }
                invokeAll(
                        new ParallelBitonicSortTask(arr, start, k, direction),
                        new ParallelBitonicSortTask(arr, start + k, k, direction)
                );
            }
        }

        private void compareAndSwap(int[] arr, int i, int j, boolean direction) {
            if (direction == (arr[i] > arr[j])) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }




    }

    private static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }

    public static int[] padArrayToPowerOf2(int[] array) {
        int originalLength = array.length;
        int nearestPowerOf2 = 1;
        while (nearestPowerOf2 < originalLength) {
            nearestPowerOf2 <<= 1;
        }
        if (nearestPowerOf2 == originalLength) {
            return array; 
        }
        int[] paddedArray = new int[nearestPowerOf2];
        System.arraycopy(array, 0, paddedArray, 0, originalLength);
        return paddedArray;
    }
}