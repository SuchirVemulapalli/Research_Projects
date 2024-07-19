import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort extends RecursiveAction {
    private final int[] array;
    private final int left;
    private final int right;

    public ParallelMergeSort(int[] array) {
        this(array, 0, array.length - 1);
    }
    public ParallelMergeSort(int[] array, int left, int right) {
        this.array = array;
        this.left = left;
        this.right = right;
    }

    @Override
    protected void compute() {
        if (left < right) {
            int middle = (left + right) / 2;

            // Sort the first and the second half in parallel
            invokeAll(new ParallelMergeSort(array, left, middle),
                    new ParallelMergeSort(array, middle + 1, right));

            merge(left, middle, right);
        }
    }

    private void merge(int left, int middle, int right) {
        int[] leftTmpArray = Arrays.copyOfRange(array, left, middle + 1);
        int[] rightTmpArray = Arrays.copyOfRange(array, middle + 1, right + 1);

        int leftIndex = 0, rightIndex = 0;
        int mergedIndex = left;

        while (leftIndex < leftTmpArray.length && rightIndex < rightTmpArray.length) {
            if (leftTmpArray[leftIndex] <= rightTmpArray[rightIndex]) {
                array[mergedIndex++] = leftTmpArray[leftIndex++];
            } else {
                array[mergedIndex++] = rightTmpArray[rightIndex++];
            }
        }

        while (leftIndex < leftTmpArray.length) {
            array[mergedIndex++] = leftTmpArray[leftIndex++];
        }

        while (rightIndex < rightTmpArray.length) {
            array[mergedIndex++] = rightTmpArray[rightIndex++];
        }
    }
}