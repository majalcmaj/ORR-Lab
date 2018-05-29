/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import datastructures.Matrix;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class EvaluationMeasures {
    
    /** Classification accuracy (singleLabel).
     * 
     * @param original      - decimal original labels [1, nTestObj]
     * @param predicted      - decimal predicted labels [1, nTestObj]
     * @return 
     */
    public static double accuracy(Collection<Integer> original, Collection<Integer> predicted)
    {
        if (original.size() != predicted.size()){
            throw new IllegalArgumentException("original and expected not of equal size");
        }
        
        int accumulator = 0;
        Iterator it1 = original.iterator();
        Iterator it2 = original.iterator();
        while(it1.hasNext()){
            if(it1.next() == it2.next()){
                accumulator++;
            }
        }
        return ((double)accumulator)*100.0/((double)original.size());
    }
    
    
    /** Classification accuracy (singleLabel).
     * 
     * @param original      - decimal original labels [1, nTestObj]
     * @param predicted      - decimal predicted labels [1, nTestObj]
     * @return 
     */
    public static double accuracy(Matrix original, Matrix predicted)
    {
        if (original.getNRows()!=1 || predicted.getNRows()!=1 || 
                original.getNCols() != predicted.getNCols()){
            throw new IllegalArgumentException("original and expected not of equal size");
        }
        
        int accumulator = 0;
        for(int i = 0; i<original.getNCols(); ++i){
            if(original.getElem(0, i) == predicted.getElem(0, i)){
                accumulator++;
            }
        }
        return ((double)accumulator)*100.0/((double)original.getNCols());
    }
    
    
    /** Mean squared error
     * 
     * @param desiredOutput      - desired output [nOutputs, nTestObj]
     * @param actualOutput      - actual output [nOutputs, nTestObj]
     * @return 
     */
    public static double mse(Matrix desiredOutput, Matrix actualOutput)
    {
        double sse = sse(desiredOutput, actualOutput);
        int nObj = desiredOutput.getNCols();
        return sse/(double)nObj;
    }
    
    
    /** Sum of squared errors.
     * 
     * @param desiredOutput
     * @param actualOutput
     * @return 
     */
    public static double sse(Matrix desiredOutput, Matrix actualOutput)
    {
        if (desiredOutput.getNRows()!= actualOutput.getNRows() || 
                desiredOutput.getNCols() != actualOutput.getNCols()){
            throw new IllegalArgumentException("desired and actual not of equal size");
        }
        
        double sse = 0.0;
        Matrix errors = actualOutput.subtract(desiredOutput);
        
        for(int r = 0; r < errors.getNRows(); ++r){
            for(int c = 0; c < errors.getNCols(); ++c){
                double e = errors.getElem(r, c);
                sse += e*e;
            }
        }
        return sse;
    }
    
    
    /** This is Hinton's version of cross-entropy. However, the version of Ng
     * wikipedia, from 'Neural Networks and deep learning' book and so on has 
     * additional logarithm component (i.e. -(1-t)log(1-y)). And this additional
     * component makes the gradient to be simply (y-t) (assuming softmax output
     * layer).
     * 
     * @param desiredOutput
     * @param actualOutput
     * @return 
     */
    public static double crossEntropy(Matrix desiredOutput, Matrix actualOutput)
    {
        if (desiredOutput.getNRows()!= actualOutput.getNRows() || 
                desiredOutput.getNCols() != actualOutput.getNCols()){
            throw new IllegalArgumentException("desired and actual not of equal size");
        }
        
        int nObj = desiredOutput.getNCols();
        int nOutputs = desiredOutput.getNRows();
        
        double ce = 0.0;
        double tiny = Math.exp(-30);
        
        for (int objIdx = 0; objIdx < nObj; ++objIdx){
            for (int outputIdx = 0; outputIdx < nOutputs; ++outputIdx){
                ce -= desiredOutput.getElem(outputIdx, nObj)
                        *Math.log(actualOutput.getElem(outputIdx, nObj) + tiny);
            }
        }
        
        return ce/(double)nObj;
    }
    
    
}
