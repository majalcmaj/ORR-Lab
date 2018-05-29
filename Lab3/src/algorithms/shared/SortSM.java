/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.shared;

import java.sql.Time;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class SortSM {
    public static final long TIME_SLEEP = 5;
    public static void serialQuicksort(double[] array){
        serialQuicksort(array, 0, array.length-1);
    }


    private static void serialQuicksort(double[] array, int sPos, int ePos){
        if(sPos < ePos){
            int partitionPoint = partitionPhase(array, sPos, ePos);
            
            //Recurential calls
            serialQuicksort(array, sPos, partitionPoint-1);
            serialQuicksort(array, partitionPoint+1, ePos);
        }
    }


    private static int partitionPhase(double[] array, int sPos, int ePos) {
        double pivot = array[ePos];
        int i = sPos - 1;
        for (int j = sPos; j<ePos; ++j){

            if(array[j]>=pivot){
                ++i;
                double tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
            }
        }
        int q = i+1;    //q is the partition point
        array[ePos] = array[q];
        array[q] = pivot;
        return q;
    }
    
    
    private static class QuickSortAction extends RecursiveAction{
        static final int THRESHOLD = 10;
        final double[] array;
        final int sPos, ePos;
        private QuickSortAction(double[] array) {
            this.array = array;
            sPos = 0;
            ePos = array.length-1;
        }

        private QuickSortAction(double[] array, int sPos, int ePos) {
            this.array = array;
            this.sPos = sPos;
            this.ePos = ePos;
        }

        @Override
        protected void compute() {
            if (ePos - sPos < THRESHOLD)
                serialQuicksort(array, sPos, ePos);
            else {
                int partitionPoint = partitionPhase(array, sPos, ePos);

                //Recurential calls
                invokeAll(new QuickSortAction(array, sPos, partitionPoint-1),
                        new QuickSortAction(array, partitionPoint+1, ePos));
            }
        }

    }
    
    public static void QuicksortDescendingForkJoin(double[] array){
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new QuickSortAction(array));
    }
    
    
    
    
    
    private static class MaxToHead extends RecursiveAction{
        static final int THRESHOLD = 10;
        final double[] array;
        final int sPos;
        final int ePos;
        
        MaxToHead(double[] array, int sPos, int ePos){
            this.array = array; this.sPos = sPos; this.ePos = ePos;
        }
        MaxToHead(double[] array){
            this(array, 0, array.length-1);
        }
        
        @Override
        protected void compute() {
            if ((ePos - sPos) < THRESHOLD){
                computeDirectly();
            }
            else{
                int midPos = (ePos+sPos)/2;
                
                invokeAll(new MaxToHead(array, sPos, midPos-1),
                          new MaxToHead(array, midPos, ePos));
                
                array[sPos] = Math.max(array[sPos], array[midPos]);
            }
        
        }
        
        private void computeDirectly(){
            double maxValue = array[sPos];
            for(int i=sPos+1; i<ePos; ++i){
                if(array[i]>maxValue){
                    maxValue = array[i];
                }
            }
            array[sPos] = maxValue;  //maxValue to the first place
        }
        
    }
    
    
    public static void MaxToHeadForkJoin(double[] array){
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new MaxToHead(array));
    }
    
    
    
    
    
}
