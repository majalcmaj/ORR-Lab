/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package algorithms.shared;

import datastructures.Matrix;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class MatrixOperationsSM {


    public static Matrix multiplyMtx(Matrix a, Matrix b,
                            int nThreadsPerRow, int nThreadsPerCol)
    {
        if(a.getNCols() != b.getNRows()){
            throw new RuntimeException("Matrices inner sizes mismatch");
        }
        if (a.getNRows()%nThreadsPerRow != 0){
            throw new RuntimeException("nThreadsPerRow has to divide "
                    + "the number of rows of result a.");
        }
        if (b.getNCols()%nThreadsPerCol != 0){
            throw new RuntimeException("nThreadsPerCol has to divide "
                    + "the number of columns of result b.");
        }

        Thread[][] workers = new Thread[nThreadsPerRow][nThreadsPerCol];
        final Matrix result = new Matrix(a.getNRows(), b.getNCols());

        int colsPerThread = a.getNRows() / nThreadsPerCol;
        int rowsPerThread = b.getNCols() / nThreadsPerRow;
        for(int i = 0 ; i < nThreadsPerRow ; i ++) {
            for(int j = 0 ; j < nThreadsPerCol ; j++) {
                workers[i][j] = new Worker(a, b, result, i, j, rowsPerThread, colsPerThread);
                workers[i][j].start();
            }
        }
        for(int i = 0 ; i < nThreadsPerRow ; i ++) {
            for(int j = 0 ; j < nThreadsPerCol ; j++) {
                try {
                    workers[i][j].join();
                }catch(InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }


        return result;
    }
    public static class Worker extends Thread {
        private Matrix a;
        private Matrix b;
        private Matrix result;
        private int myRowsIndex;
        private int myColsIndex;
        private int rowsPerThread;
        private int colsPerThread;

        public Worker(Matrix a, Matrix b, Matrix result, int myXIndex, int myYIndex, int rowsPerThread, int colsPerThread) {
            this.a = a;
            this.b = b;
            this.result = result;
            this.myRowsIndex = myYIndex * rowsPerThread;
            this.myColsIndex = myXIndex * colsPerThread;
            this.rowsPerThread = rowsPerThread;
            this.colsPerThread = colsPerThread;
        }

        @Override
        public void run() {
            int iters = a.getNCols();
            for(int y = 0 ; y < rowsPerThread ; y++) {
                for(int x = 0 ; x < colsPerThread ;x++) {
                    double sum = 0;
                    for(int i = 0 ; i < iters ; i ++) {
                        sum += a.getElem(y + myRowsIndex, i) * b.getElem(i, x + myColsIndex);
                    }
                    result.setElem(y + myRowsIndex, x + myColsIndex, sum);
                }
            }
        }
    }
    
}
