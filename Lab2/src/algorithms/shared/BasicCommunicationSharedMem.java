/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.shared;

import algorithms.Utils;

import javax.rmi.CORBA.Util;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class BasicCommunicationSharedMem{
    
    
    public static void scanSerial(double[] array, DoubleBinaryOperator op){
        
        for(int i = 1; i < array.length; ++i){
            array[i]  = op.applyAsDouble(array[i-1], array[i]);
            
            try {
                Thread.sleep(software.SoftwareSM
                        .DEFAULT_SIMULATION_UNIT_PROCESSING_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(BasicCommunicationSharedMem.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /** This should be a Hillis/Steele algorithm for parallel scan computation.
     * 
     * @param array
     * @param op 
     */
    public static void scan(double[] array, DoubleBinaryOperator op){
        
        class Scanner implements Runnable{
            final int myIdx;
            final CyclicBarrier barrier;

            public Scanner(int myIdx, CyclicBarrier b){
                this.myIdx = myIdx;
                this.barrier = b;
            }

            @Override
            public void run() {
                int elementsCount = array.length;
                int epochsCount = 0;
                while(elementsCount > 0) {
                    epochsCount++;
                    elementsCount >>= 1;
                }

                for(int i = 0 ; i < epochsCount ; i ++) {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        Logger.getLogger(BasicCommunicationSharedMem.class.getName())
                                .log(Level.SEVERE, null, ex);
                        return;
                    }
                    double myVal = array[myIdx];
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        Logger.getLogger(BasicCommunicationSharedMem.class.getName())
                                .log(Level.SEVERE, null, ex);
                        return;
                    }
                    int sendToIdx = myIdx + (1 << i);
                    if(sendToIdx < array.length) {
                        array[sendToIdx] += myVal;
                    }
                }
            }
        }
        
        //construct threads and a barrier
        int nThreads = array.length - 1;
        CyclicBarrier barrier = new CyclicBarrier(nThreads);
        Thread[] workers = new Thread[nThreads];

        System.out.println(nThreads);
        //start all the threads
        for(int i = 0; i<nThreads; ++i){
            workers[i] = new Thread(new Scanner(i, barrier));
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
        System.gc();
    }
    
}
