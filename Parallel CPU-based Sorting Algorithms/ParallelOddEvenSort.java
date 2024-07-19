import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ParallelOddEvenSort {

    public static void main(String[] args) {

        long[] durations = new long[5]; 

        for (int i = 0; i < 5; i++) {
            int[] array = generateRandomArray(20000);
            long startTime = System.nanoTime();
            oddEvenSortParallel(array, 8); 
            long endTime = System.nanoTime();
            durations[i] = endTime - startTime; 
            System.out.println("Run " + (i + 1) + " - Execution Time: " + durations[i] + " nanoseconds");
            System.out.println("Sorted: " + isSorted(array));
            array = generateRandomArray(20000); 
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

    public static void oddEvenSortParallel(int[] array, int numThreads) {
        int n = array.length;
        AtomicBoolean sorted = new AtomicBoolean(false);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch phaseLatch = new CountDownLatch(2 * numThreads); 

        while (!sorted.get()) {
            sorted.set(true);

            // Odd phase
            for (int i = 0; i < numThreads; i++) {
                final int threadNum = i;
                CountDownLatch finalPhaseLatch = phaseLatch;
                executor.submit(() -> {
                    for (int j = 2 * threadNum + 1; j < n - 1; j += 2 * numThreads) {
                        if (array[j] > array[j + 1]) {
                            sorted.set(false);
                            swap(array, j, j + 1);
                        }
                    }
                    finalPhaseLatch.countDown(); 
                });
            }

            // Even phase
            for (int i = 0; i < numThreads; i++) {
                final int threadNum = i;
                CountDownLatch finalPhaseLatch1 = phaseLatch;
                executor.submit(() -> {
                    for (int j = 2 * threadNum; j < n - 1; j += 2 * numThreads) {
                        if (array[j] > array[j + 1]) {
                            sorted.set(false);
                            swap(array, j, j + 1);
                        }
                    }
                    finalPhaseLatch1.countDown(); 
                });
            }

            try {
                phaseLatch.await(); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            
            phaseLatch = new CountDownLatch(2 * numThreads);
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            
        }
    }


    public static int[] generateRandomArray(int size) {
        int[] array = new int[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = rand.nextInt(1000); 
        }
        return array;
    }

    public static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    private static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }
}

