/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import datastructures.Matrix;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class FFNetSequential extends FFNet{

    private Matrix _InWPrevGrad;        //previous gradients
    private Matrix _InWStepSizes;
    
    private Matrix _InBiasWPrevGrad;    //previous gradients
    private Matrix _InBiasWStepSizes;
    
    private Matrix _LayerWPrevGrad;     //previous gradients
    private Matrix _LayerWStepSizes;
    
    private Matrix _LayerBiasWPrevGrad;  //previous gradients
    private Matrix _LayerBiasWStepSizes;
    
    
    public FFNetSequential(int nInputs, int nHiddenUnits) {
        super(nInputs, nHiddenUnits);
        initializeAuxiliaries();
    }

    public FFNetSequential(int nInputs, int nHiddenUnits, long seed) {
        super(nInputs, nHiddenUnits, seed);
        initializeAuxiliaries();
    }
    
    
    private void initializeAuxiliaries() {
        _InWPrevGrad = new Matrix(_InW.getNRows(), _InW.getNCols());
        _InBiasWPrevGrad = new Matrix(_InBiasW.getNRows(), _InBiasW.getNCols());
        _LayerWPrevGrad = new Matrix(_LayerW.getNRows(), _LayerW.getNCols());
        _LayerBiasWPrevGrad = new Matrix(_LayerBiasW.getNRows(), _LayerBiasW.getNCols());
        
        _InWStepSizes = new Matrix(_InW.getNRows(), _InW.getNCols()).add(_TrainParam.initial_step_size);
        _InBiasWStepSizes = new Matrix(_InBiasW.getNRows(), _InBiasW.getNCols()).add(_TrainParam.initial_step_size);
        _LayerWStepSizes = new Matrix(_LayerW.getNRows(), _LayerW.getNCols()).add(_TrainParam.initial_step_size);
        _LayerBiasWStepSizes = new Matrix(_LayerBiasW.getNRows(), _LayerBiasW.getNCols()).add(_TrainParam.initial_step_size);
    }
    
    @Override
    public void train(Matrix trainDataIn, Matrix trainDataOut)
    {
        int epoch = 0;
        double performance;
        
        while(true){
            
            Matrix outputs = forwardPass(trainDataIn);
            
            performance = calculatePerformanceEvaluation(trainDataOut, outputs);
            
            if(performance < _TrainParam.performanceGoal){
                addToPerformanceHistory(epoch, performance);
                System.out.format("Performance goal met after %d epochs.\n", epoch);
                break;
            }
            
            if(epoch%_TrainParam.showInterval == 0){
                addToPerformanceHistory(epoch, performance);
                printInfo(epoch, performance);
            }
            
            calculateGradients(outputs, trainDataIn, trainDataOut); //backpropagation
            
            updateWeightsRPROP();
            
            epoch++;
                
            if(epoch > _TrainParam.maxEpochs){
                System.out.println("Maximum epochs reached.");
                break;
            }
            
        }
    }
    
    
    
    protected void updateWeightsRPROP()
    {
        updateLocalStepSizes(_InWStepSizes, _InWPrevGrad, _InWGrad);
        updateLocalStepSizes(_InBiasWStepSizes, _InBiasWPrevGrad, _InBiasWGrad);
        updateLocalStepSizes(_LayerWStepSizes, _LayerWPrevGrad, _LayerWGrad);
        updateLocalStepSizes(_LayerBiasWStepSizes, _LayerBiasWPrevGrad, _LayerBiasWGrad);
        
        DoubleUnaryOperator signum = (double v) -> {
            return (v > 0.0)? 1.0 : -1.0;
        };
        
        _InW.subtractInPlace(_InWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_InWStepSizes));
        _InBiasW.subtractInPlace(_InBiasWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_InBiasWStepSizes));
        _LayerW.subtractInPlace(_LayerWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_LayerWStepSizes));
        _LayerBiasW.subtractInPlace(_LayerBiasWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_LayerBiasWStepSizes));
        
        _InWPrevGrad = new Matrix(_InWGrad);            //copy constructor
        _InBiasWPrevGrad = new Matrix(_InBiasWGrad);    //copy constructor
        _LayerWPrevGrad = new Matrix(_LayerWGrad);
        _LayerBiasWPrevGrad = new Matrix(_LayerBiasWGrad);
        
    }
    
    
    private void updateLocalStepSizes(Matrix stepSizes, Matrix prevGrad, Matrix grad)
    {
        Matrix tmp = prevGrad.timesElementByElement(grad);
        for(int i = 0; i < tmp.getNRows(); ++i){
            for(int j = 0; j < tmp.getNCols(); ++j){
                double oldStep = stepSizes.getElem(i, j);
                double newStep = (tmp.getElem(i, j) > 0.0)? //if the same direction
                    oldStep*_TrainParam.positiveFactor : oldStep*_TrainParam.negativeFactor;
                if (newStep < _TrainParam.minGain){ newStep = _TrainParam.minGain; }
                if (newStep > _TrainParam.maxGain){ newStep = _TrainParam.maxGain; }
                stepSizes.setElem(i, j, newStep);
            }
        }
    }
    
    
}
