/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.shared;

import algorithms.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class BasicCommunicationSharedMem{
    
    /** Uses array.length /2 threads to reduce all elements in array into 
     * array[0] using binary associative operator op in Theta(array.length)
     * time.
     * 
     * @param array - array of values to reduce
     * @param op
     */
    public static void reduce(double[] array, DoubleBinaryOperator op)
    {
        int nThreads = array.length / 2;
        CyclicBarrier barrier = new CyclicBarrier(nThreads);
        Thread[] workers;
        workers = new Thread[nThreads];
        
        class Reducer implements Runnable{
            final int myIdx;
            final CyclicBarrier barrier;

            public Reducer(int myIdx, CyclicBarrier b){
                this.myIdx = myIdx;
                this.barrier = b;
            }

            @Override
            public void run() {

                int mask = 1;
                int powerOfTwo = 1;
                for(int i=0; i<Utils.binlog(array.length); ++i){
                    if ( (myIdx & mask) == 0 ){   //if lower i bits of idx are 0
                        if ( (myIdx & powerOfTwo) == 0 ){
                            double v1 = array[myIdx ^ powerOfTwo];
                            array[myIdx] = op.applyAsDouble(array[myIdx], v1);
                        }
                    }

                    mask ^= powerOfTwo;   //set bit i of mask to 1
                    powerOfTwo <<= 1;

                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        Logger.getLogger(BasicCommunicationSharedMem
                                .class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
        
        //start all the threads
        for(int i = 0; i<nThreads; ++i){
            workers[i] = new Thread(new Reducer(2*i, barrier));
            workers[i].start();
        }
        
        //wait for all the threads to finish
        for(int i = 0; i<nThreads; ++i){
            try {
                workers[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicCommunicationSharedMem.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
