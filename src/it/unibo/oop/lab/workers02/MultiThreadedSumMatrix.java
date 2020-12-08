package it.unibo.oop.lab.workers02;

import java.util.ArrayList;
import java.util.List;

public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthreads;

    public MultiThreadedSumMatrix(final int n) {
        this.nthreads = n;
    }

    private static final class Worker extends Thread {
        private final double[][] matrix;
        private final int startRow;
        private final int nRows;
        private long res;   //= 0 by default

        private Worker(final double[][] matrix, final int startRow, final int nRows) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.nRows = nRows;
        }

        public void run() {
            System.out.println("Working from position [" + this.startRow + "][0] to position [" + (this.startRow + this.nRows - 1) + "][" + (this.matrix.length - 1) + "]");
            for (int i = this.startRow; i < this.matrix.length && i < this.startRow + this.nRows; i++) {
                for (int j = 0; j < this.matrix[i].length; j++) {
                    this.res += this.matrix[i][j];
                }
            }
        }

        public long getResult() {
            return this.res;
        }
    }

    public double sum(final double[][] matrix) {
        final int rows = matrix.length % this.nthreads + matrix.length / this.nthreads;
        /*
         * Create list of workers
         */
        final List<Worker> workers = new ArrayList<>(this.nthreads);
        for (int start = 0; start < matrix.length; start += rows) {
            workers.add(new Worker(matrix, start, rows));
        }
        /*
         * Start them
         */
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        long sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }

}
