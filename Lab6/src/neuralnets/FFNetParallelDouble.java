/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import algorithms.shared.MatrixOperationsSM;
import datastructures.Matrix;
import distributedmodel.Node;

import javax.sound.midi.SysexMessage;
import java.util.function.DoubleBinaryOperator;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class FFNetParallelDouble extends FFNetParallel{
    
    private final int _NThreads;
    
    
    public FFNetParallelDouble(Node node, int wholeTrainDataSize,
            int nThreads, int nInputs, int nHiddenUnits)
    {
        super(node, wholeTrainDataSize, nInputs, nHiddenUnits);
        _NThreads = nThreads;
    }
    
    public FFNetParallelDouble(Node node, int wholeTrainDataSize,
            int nThreads, int nInputs, int nHiddenUnits, long seed)
    {
        super(node, wholeTrainDataSize, nInputs, nHiddenUnits, seed);
        _NThreads = nThreads;
    }
   
    
    @Override
    protected Matrix forwardPass(Matrix input)
    {
        Matrix inputToHidden = MatrixOperationsSM.multiplyMtx(_InW , input, 1, _NThreads).addInPlaceRepeatedColumn(_InBiasW);
        _HiddenState = inputToHidden.applyFunctionElementwise(_AFHidden.getActivationFun());
        Matrix inputToOutput =  MatrixOperationsSM.multiplyMtx(_LayerW, _HiddenState, 1, _NThreads).addInPlaceRepeatedColumn(_LayerBiasW);
        Matrix output = inputToOutput.applyFunctionElementwise(_AFOut.getActivationFun());
        return output;
    }
    
    
    
    @Override
    protected void backwardPass(Matrix lastDelta)
    {
        Matrix derivHiddenActiv = _HiddenState.applyFunctionElementwise(_AFHidden.getGradientFun());
        _deltasAtHidden = MatrixOperationsSM.multiplyMtx(_LayerW.transpose(), lastDelta, 1, _NThreads)
                .timesElementByElementInPlace(derivHiddenActiv);
    }
    
    
    @Override
    protected void calculateGradients(Matrix outputs, Matrix trainDataIn, Matrix trainDataOut)
    {
        int batchsize = trainDataIn.getNCols();
        
        DoubleBinaryOperator sum = (double v1, double v2) -> { return v1+v2; };
        
        Matrix lastDelta = outputs.subtract(trainDataOut);
        
        backwardPass(lastDelta);
        
        _LayerWGrad = lastDelta
                        .timesByTranspose(_HiddenState)
                        .divide(batchsize);
        _LayerBiasWGrad = lastDelta
                            .reduceRows(sum)
                            .divide(batchsize);
        _InWGrad = _deltasAtHidden
                        .timesByTranspose(trainDataIn)
                        .divide(batchsize);
        _InBiasWGrad = _deltasAtHidden
                        .reduceRows(sum)
                        .divide(batchsize);
    }
    
}
