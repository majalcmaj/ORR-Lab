/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package algorithms.shared;

import datastructures.Matrix;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class MatrixOperationsSM {
    
    
    public static Matrix multiplyMtx(Matrix a, Matrix b,
                            int nThreadsPerRowDimension, int nThreadsPerColDimension)
    {
        if(a.getNCols() != b.getNRows()){
            throw new RuntimeException("Matrices inner sizes mismatch");
        }
        if (a.getNRows()%nThreadsPerRowDimension != 0){
            throw new RuntimeException("nThreadsPerRow has to divide "
                    + "the number of rows of matrix a.");
        }
        if (b.getNCols()%nThreadsPerColDimension != 0){
            throw new RuntimeException("nThreadsPerCol has to divide "
                    + "the number of columns of matrix b.");
        }
        
        Thread[][] workers = new Thread[nThreadsPerRowDimension][nThreadsPerColDimension];
        
        final CyclicBarrier barrier = new CyclicBarrier(nThreadsPerRowDimension*nThreadsPerColDimension);
        final Matrix result = new Matrix(a.getNRows(), b.getNCols());
        
        class MtxMultiplier implements Runnable{
            final int myRowStart;
            final int myRowEnd;
            final int myColStart;
            final int myColEnd;

            public MtxMultiplier(int myRidx, int myCidx)
            {
                myRowStart = (result.getNRows()/nThreadsPerRowDimension)*myRidx;
                myRowEnd = (result.getNRows()/nThreadsPerRowDimension)*(myRidx+1);
                myColStart = (result.getNCols()/nThreadsPerColDimension)*myCidx;
                myColEnd = (result.getNCols()/nThreadsPerColDimension)*(myCidx+1);
            }

            @Override
            public void run() {
//                System.out.format("myRowStart=%d, myRowEnd=%d, myColStart=%d, myColEnd=%d\n", myRowStart, myRowEnd, myColStart, myColEnd);
                for(int row = myRowStart; row < myRowEnd; ++row){
                    for(int col = myColStart; col < myColEnd; ++col){
                        double tmp = 0;
                        for(int k = 0; k < a.getNCols(); ++k){
                            tmp += a.getElem(row, k) * b.getElem(k, col);
                        }
                        result.setElem(row, col, tmp);
                    }
                }
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ex) {
                    Logger.getLogger(MatrixOperationsSM.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
        
        
        //start all the threads
        for(int row = 0; row<nThreadsPerRowDimension; ++row){
            for(int col = 0; col < nThreadsPerColDimension; ++col){
                workers[row][col] = new Thread(new MtxMultiplier(row, col));
                workers[row][col].start();
            }
        }
        
        //wait for all the threads to finish
        for(int row = 0; row<nThreadsPerRowDimension; ++row){
            for(int col = 0; col < nThreadsPerColDimension; ++col){
                try {
                workers[row][col].join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(BasicCommunicationSharedMem.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }
    
    
}
