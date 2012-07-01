package infovis.util;

import java.util.Arrays;

/**
 * Utility functions for arrays.
 * 
 * @author Leo Woerteler
 */
public final class ArrayUtil {

  /** Hidden default constructor. */
  private ArrayUtil() {
    // no-op
  }

  /**
   * Fills the given two-dimensional {@code double} array with the value
   * {@code val}.
   * 
   * @param arr array to fill
   * @param val value to fill the array with
   * @return filled array
   */
  public static double[][] fill(final double[][] arr, final double val) {
    for(final double[] line : arr) {
      Arrays.fill(line, val);
    }
    return arr;
  }

  /**
   * Copies the given two-dimensional {@code double} array.
   * 
   * @param arr array to copy
   * @return deep-copy of the array
   */
  public static double[][] copy(final double[][] arr) {
    final double[][] copy = arr.clone();
    for(int i = 0; i < copy.length; i++) {
      copy[i] = copy[i].clone();
    }
    return copy;
  }

  /**
   * Swaps two entries in a <code>T</code> array.
   * 
   * @param <T> The type of the array.
   * @param arr array
   * @param i position of first element
   * @param j position of second element
   */
  public static <T> void swap(final T[] arr, final int i, final int j) {
    final T temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

  /**
   * Swaps two entries in a <code>double</code> array.
   * 
   * @param arr array
   * @param i position of first element
   * @param j position of second element
   */
  public static void swap(final double[] arr, final int i, final int j) {
    final double temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

}
