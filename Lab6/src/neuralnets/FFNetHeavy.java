/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import datastructures.Matrix;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.jfree.data.xy.XYSeries;
 
/** A class representing a simple feed forward neural network (sometimes called
 * multi-layer perceptron - MLP) for regression problems.
 * The net has one hidden layer of neurons (with logistic activation function)
 * and a single output neuron with linear activation function
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class FFNetHeavy {
    
    /** Weights at input to hidden connections.
     * Size: [nHidden , nFeatures]
     */
    private Matrix _InW;
    private Matrix _InWGrad;    //gradients wrt corresponding weights
    private Matrix _InWVelocity;  //momentum change (used in momentum method)
    private Matrix _InWLRGains; //for GDLALR method (local LR gains)
    
    public Matrix getInW(){
        return _InW;
    }
    public void setInW(Matrix inWeights){
        _InW = inWeights;
    }
    
    /** Weights at bias to hidden connections.
     * Size: [nHidden, 1]
     */
    private Matrix _InBiasW;
    private Matrix _InBiasWGrad;    //gradients wrt corresponding weights
    private Matrix _InBiasWVelocity;  //momentum change (used in momentum method)
    private Matrix _InBiasWLRGains; //for GDLALR method (local LR gains)
    
    public Matrix getInBiasW(){
        return _InBiasW;
    }
    public void setInBiasW(Matrix inBiasWeights){
        _InBiasW = inBiasWeights;
    }
    
    /** Weights at hidden to output connections
     * Size: [1, nHidden]
     */
    private Matrix _LayerW;
    private Matrix _LayerWGrad;    //gradients wrt corresponding weights
    private Matrix _LayerWVelocity;  //momentum change (used in momentum method)
    private Matrix _LayerWLRGains; //for GDLALR method (local LR gains)
    
    public Matrix getLayerW(){
        return _LayerW;
    }
    public void setLayerW(Matrix layerWeights){
        _LayerW = layerWeights;
    }
    
    /** Weights at bias to hidden connections
     * Size: [1, 1]
     */
    private Matrix _LayerBiasW;
    private Matrix _LayerBiasWGrad;    //gradients wrt corresponding weights
    private Matrix _LayerBiasWVelocity;  //momentum change (used in momentum method)
    private Matrix _LayerBiasWLRGains; //for GDLALR method (local LR gains)
    
    public Matrix getLayerBiasW(){
        return _LayerBiasW;
    }
    public void setLayerBiasW(Matrix layerBiasWeights){
        _LayerBiasW = layerBiasWeights;
    }
    
    
    /** Activation unit of the hidden units (logistic by default)
     * 
     */
    private ActivationUnit _AFHidden;
    
    public ActivationUnit getHiddenActivFunc(){
        return _AFHidden;
    }
    public void setHiddenActivFunc(ActivationUnit af){
        _AFHidden = af;
    }
    
    /** Actication unit of the output unit (purelin by default)
     * 
     */
    private ActivationUnit _AFOut;
    
    public ActivationUnit getOutputActivFunc(){
        return _AFOut;
    }
    public void setOutputActivFunc(ActivationUnit af){
        _AFOut = af;
    }
    
    
    private Matrix _HiddenState;            //[nHiddenUnits x batchsize]      
    private Matrix _deltasAtHidden;         //[nHiddenUnits x batchsize]

    
    public enum TrainMethod {
        GD,         //gradient descent
        GDM,        //gradient descent with momentum
        GDCM,       //classical momentum
        NAG,        //Nesterov accelerated gradient
        GDLALR,     //gradient descent with local adaptive learning rates and momentum
        RPROP       // rprop
    }
    
    
    public class TrainParam
    {
        public static final double DEFAULT_LR = 0.01;
        public static final double DEFAULT_MOMENTUM = 0.9;
        public static final int DEFAULT_MAX_EPOCHS = 1000;
        public static final double DEFAULT_PERFORMANCE_GOAL = 1e-4;
        public static final int DEFAULT_SHOW_INTERVAL = 50;
        
        public double learningRate;
        public double momentum;
        public int maxEpochs;
        public double performanceGoal;
        public int showInterval;
        
        public TrainParam(){
            learningRate = DEFAULT_LR;
            momentum = DEFAULT_MOMENTUM;
            maxEpochs = DEFAULT_MAX_EPOCHS;
            performanceGoal = DEFAULT_PERFORMANCE_GOAL;
            showInterval = DEFAULT_SHOW_INTERVAL;
        }
    }
    
    private final TrainParam _TrainParam;
    
    public TrainParam getTrainParam(){
        return _TrainParam;
    }
    
    private TrainMethod _TrainMethod;
    
    public TrainMethod getTrainMethod(){
        return _TrainMethod;
    }
    public void setTrainMethod(TrainMethod method){
        _TrainMethod = method;
    }
    
    private static final double DEFAULT_INIT_WEIGHTS_STD = 0.01;
    private static final TrainMethod DEFAULT_TRAIN_METHOD = TrainMethod.RPROP;
    
    
    
    /** Constructor. Initializes weights to random weights.
     * 
     * @param nInputs       - number of inputs to the net (=number of features)
     * @param nHiddenUnits  - humber of units in the hidden layer
     */
    public FFNetHeavy(int nInputs, int nHiddenUnits){
        double std = DEFAULT_INIT_WEIGHTS_STD;
        _InW = new Matrix(nHiddenUnits, nInputs,
                NNUtils.generateNormalRandomData(nHiddenUnits*nInputs, std));
        _InBiasW = new Matrix(nHiddenUnits, 1,
                NNUtils.generateNormalRandomData(nHiddenUnits, std));
        _LayerW = new Matrix(1, nHiddenUnits,
                NNUtils.generateNormalRandomData(nHiddenUnits, std));
        _LayerBiasW = new Matrix(1, 1,
                NNUtils.generateNormalRandomData(1, std));
        _AFHidden = ActivationUnit.getLogisticUnit();
        _AFOut = ActivationUnit.getPurelinUnit();
        _TrainParam = new TrainParam();
        _TrainMethod = DEFAULT_TRAIN_METHOD;
        initializeVelocitiesForMomentumMethod();
        initializeGainsForLocalAdaptiveLRMethod();
    }
    
    
    /** Constructor. Initializes weights to random weights using specified seed.
     * 
     * @param nInputs       - number of inputs to the net (=number of features)
     * @param nHiddenUnits  - humber of units in the hidden layer
     * @param seed          - seed for rng
     */
    public FFNetHeavy(int nInputs, int nHiddenUnits, long seed){
        double std = DEFAULT_INIT_WEIGHTS_STD;
        long nextSeed = seed;
        
        _InW = new Matrix(nHiddenUnits, nInputs,
                NNUtils.generateNormalRandomData(nHiddenUnits*nInputs, std, nextSeed));
        nextSeed += _InW.getNRows() * _InW.getNCols();
        
        _InBiasW = new Matrix(nHiddenUnits, 1,
                NNUtils.generateNormalRandomData(nHiddenUnits, std, nextSeed));
        nextSeed += _InBiasW.getNRows() * _InBiasW.getNCols();
        
        _LayerW = new Matrix(1, nHiddenUnits,
                NNUtils.generateNormalRandomData(nHiddenUnits, std, nextSeed));
        nextSeed += _LayerW.getNRows() * _LayerW.getNCols();
        
        _LayerBiasW = new Matrix(1, 1,
                NNUtils.generateNormalRandomData(1, std, nextSeed));
        
        _AFHidden = ActivationUnit.getLogisticUnit();
        _AFOut = ActivationUnit.getPurelinUnit();
        _TrainParam = new TrainParam();
        _TrainMethod = DEFAULT_TRAIN_METHOD;
        initializeVelocitiesForMomentumMethod();
        initializeGainsForLocalAdaptiveLRMethod(); 
    }
    
    
    private void initializeVelocitiesForMomentumMethod() {
        _InWVelocity = new Matrix(_InW.getNRows(), _InW.getNCols());
        _InBiasWVelocity = new Matrix(_InBiasW.getNRows(), _InBiasW.getNCols());
        _LayerWVelocity = new Matrix(_LayerW.getNRows(), _LayerW.getNCols());
        _LayerBiasWVelocity = new Matrix(_LayerBiasW.getNRows(), _LayerBiasW.getNCols());
    }
    
    private void initializeGainsForLocalAdaptiveLRMethod() {
        _InWLRGains = new Matrix(_InW.getNRows(), _InW.getNCols()).addInPlace(1.0);
        _InBiasWLRGains = new Matrix(_InBiasW.getNRows(), _InBiasW.getNCols()).addInPlace(1.0);
        _LayerWLRGains = new Matrix(_LayerW.getNRows(), _LayerW.getNCols()).addInPlace(1.0);
        _LayerBiasWLRGains = new Matrix(_LayerBiasW.getNRows(), _LayerBiasW.getNCols()).addInPlace(1.0);
    }
    
    
    /** Train the net using standard gradient descent method.
     * 
     * @param trainDataIn       - input train data    [nFeats x nTrainObj]
     * @param trainDataOut      - output train data   [1 x nTrainObj]
     * @return                  - XYSeries with performance history to plot capabilities
     */
    public XYSeries train(Matrix trainDataIn, Matrix trainDataOut){
        
        XYSeries performanceHistory = new XYSeries("Performance");
        
        int epoch = 0;
        
        while(true){
            
            Matrix outputs = forwardPass(trainDataIn);
            
            double performance = EvaluationMeasures.mse(trainDataOut, outputs);
            if(performance < _TrainParam.performanceGoal){
                performanceHistory.add(epoch, performance);
                System.out.format("Performance goal met after %d epochs.\n", epoch);
                break;
            }
            
            if(epoch%_TrainParam.showInterval == 0){
                performanceHistory.add(epoch, performance);
                System.out.format("Epoch %d, trainMSE = %8.6f\n", epoch, performance);
            }
            
            if(_TrainMethod != TrainMethod.NAG){
                calculateGradients(outputs, trainDataIn, trainDataOut); //backpropagation
            }
            
            //some form of gradient descent
            switch(_TrainMethod){
                case GD:
                    updateWeightsGD();
                    break;
                case GDM:
                    updateWeightsGDM();
                    break;
                case GDCM:
                    updateWeightsGDCM();
                    break;
                case NAG:
                    updateWeightsNAG(outputs, trainDataIn, trainDataOut);
                    break;
                case GDLALR:
                    updateWeightsGDLALR();
                    break;
                case RPROP:
                    updateWeightsRPROP();
                    break;
                default:
                    updateWeightsRPROP();
            }
            epoch++;
                
            if(epoch > _TrainParam.maxEpochs){
                System.out.println("Maximum epochs reached.");
                break;
            }
            
        }
        return performanceHistory;
    }
    
    
    /** returns the output of the net, when the input is given
     * 
     * @param input        - input to the net
     * @return                  - output of the net
     */
    public double predict(double[] input){
        Matrix output = predict(new Matrix(input.length, 1, input));
        return output.getElem(0, 0);
    }
    
    
    /** Returns outputs of the net to a series of test objects
     * 
     * @param testDataIn    - input test data       [nFeats x nTestObj]
     * @return              - output of the net     [1 x nTestObj]
     */
    public Matrix predict(Matrix testDataIn){
        return forwardPass(testDataIn);
    }
    
    
    
    /**
     * 
     * @param input         [nInputs x batchsize]
     * @return              [nOutputs x batchsize]
     */
    private Matrix forwardPass(Matrix input)
    {
        Matrix inputToHidden = _InW.times(input).addInPlaceRepeatedColumn(_InBiasW);
        _HiddenState = inputToHidden.applyFunctionElementwise(_AFHidden.getActivationFun());
        Matrix inputToOutput = _LayerW.times(_HiddenState).addInPlaceRepeatedColumn(_LayerBiasW);
        Matrix output = inputToOutput.applyFunctionElementwise(_AFOut.getActivationFun());
        return output;
    }
    
    
    private void backwardPass(Matrix lastDelta)
    {
        Matrix derivHiddenActiv = _HiddenState.applyFunctionElementwise(_AFHidden.getGradientFun());
        _deltasAtHidden = _LayerW.transpose()
                            .times(lastDelta)
                            .timesElementByElementInPlace(derivHiddenActiv);
    }
    
    
    
    
    
   /** The backpropagation algorithm to calculate gradients of the cost function
    * wrt all the parameters.
    * 
    * @param trainDataIn 
    */
    private void calculateGradients(Matrix outputs, Matrix trainDataIn, Matrix trainDataOut)
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
    
    
    /** Updates weights using gradient descent with momentum.
     * (This is equivalent to GDCM)
     * 
     */
    private void updateWeightsGDM()
    {
        _InWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_InWGrad);
        _InBiasWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_InBiasWGrad);
        _LayerWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_LayerWGrad);
        _LayerBiasWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_LayerBiasWGrad);
        
        _InW.subtractInPlace(_InWVelocity.times(_TrainParam.learningRate));
        _InBiasW.subtractInPlace(_InBiasWVelocity.times(_TrainParam.learningRate));
        _LayerW.subtractInPlace(_LayerWVelocity.times(_TrainParam.learningRate));
        _LayerBiasW.subtractInPlace(_LayerBiasWVelocity.times(_TrainParam.learningRate));
    }
    
    
    /** Updates weights using gradient descent with classical momentum.
     * 
     */
    private void updateWeightsGDCM()
    {
        _InWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_InWGrad.timesInPlace(_TrainParam.learningRate));
        _InBiasWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_InBiasWGrad.timesInPlace(_TrainParam.learningRate));
        _LayerWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_LayerWGrad.timesInPlace(_TrainParam.learningRate));
        _LayerBiasWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_LayerBiasWGrad.timesInPlace(_TrainParam.learningRate));
        
        _InW.addInPlace(_InWVelocity);
        _InBiasW.addInPlace(_InBiasWVelocity);
        _LayerW.addInPlace(_LayerWVelocity);
        _LayerBiasW.addInPlace(_LayerBiasWVelocity);
    }
    
    /** Updates weights using Nesterov accelerated gradient.
     * See: "On the importance of initialization and momentum in deep learning"
     * 
     */
    private void updateWeightsNAG(Matrix outputs, Matrix trainDataIn, Matrix trainDataOut)
    {
        Matrix inWcopy = new Matrix(_InW);
        Matrix inBiasWcopy = new Matrix(_InBiasW);
        Matrix layerWcopy = new Matrix(_LayerW);
        Matrix layerBiasWcopy = new Matrix(_LayerBiasW);
        
        _InW.addInPlace(_InWVelocity);
        _InBiasW.addInPlace(_InBiasWVelocity);
        _LayerW.addInPlace(_LayerWVelocity);
        _LayerBiasW.addInPlace(_LayerBiasWVelocity);
        
        calculateGradients(outputs, trainDataIn, trainDataOut);
        
        _InWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_InWGrad.timesInPlace(_TrainParam.learningRate));
        _InBiasWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_InBiasWGrad.timesInPlace(_TrainParam.learningRate));
        _LayerWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_LayerWGrad.timesInPlace(_TrainParam.learningRate));
        _LayerBiasWVelocity.timesInPlace(_TrainParam.momentum)
                .subtractInPlace(_LayerBiasWGrad.timesInPlace(_TrainParam.learningRate));
        
        _InW = inWcopy.addInPlace(_InWVelocity);
        _InBiasW = inBiasWcopy.addInPlace(_InBiasWVelocity);
        _LayerW = layerWcopy.addInPlace(_LayerWVelocity);
        _LayerBiasW = layerBiasWcopy.addInPlace(_LayerBiasWVelocity);
                
    }
    
    
    
    
    /** Updates weights using simple gradient descent.
     * 
     */
    private void updateWeightsGD()
    {
        _InW.subtractInPlace(_InWGrad.times(_TrainParam.learningRate));
        _InBiasW.subtractInPlace(_InBiasWGrad.times(_TrainParam.learningRate));
        _LayerW.subtractInPlace(_LayerWGrad.times(_TrainParam.learningRate));
        _LayerBiasW.subtractInPlace(_LayerBiasWGrad.times(_TrainParam.learningRate));
    }
    
    
    /** Update weights using gradient descent with local adaptive learning rates
     *  and momentum
     */
    private void updateWeightsGDLALR()
    {
        _InWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_InWGrad);
        _InBiasWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_InBiasWGrad);
        _LayerWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_LayerWGrad);
        _LayerBiasWVelocity.timesInPlace(_TrainParam.momentum).addInPlace(_LayerBiasWGrad);
        
        updateLRLocalGains(_InWLRGains, _InWVelocity, _InWGrad);
        updateLRLocalGains(_InBiasWLRGains, _InBiasWVelocity, _InBiasWGrad);
        updateLRLocalGains(_LayerWLRGains, _LayerWVelocity, _LayerWGrad);
        updateLRLocalGains(_LayerBiasWLRGains, _LayerBiasWVelocity, _LayerBiasWGrad);
        
        _InW.subtractInPlace(_InWVelocity.timesElementByElement(_InWLRGains.times(_TrainParam.learningRate)));
        _InBiasW.subtractInPlace(_InBiasWVelocity.timesElementByElement(_InBiasWLRGains.times(_TrainParam.learningRate)));
        _LayerW.subtractInPlace(_LayerWVelocity.timesElementByElement(_LayerWLRGains.times(_TrainParam.learningRate)));
        _LayerBiasW.subtractInPlace(_LayerBiasWVelocity.timesElementByElement(_LayerBiasWLRGains.times(_TrainParam.learningRate)));
    }
    
    
    private void updateWeightsRPROP()
    {
        updateLocalStepSizes(_InWLRGains, _InWVelocity, _InWGrad);
        updateLocalStepSizes(_InBiasWLRGains, _InBiasWVelocity, _InBiasWGrad);
        updateLocalStepSizes(_LayerWLRGains, _LayerWVelocity, _LayerWGrad);
        updateLocalStepSizes(_LayerBiasWLRGains, _LayerBiasWVelocity, _LayerBiasWGrad);
        
        DoubleUnaryOperator signum = (double v) -> {
            return (v > 0.0)? 1.0 : -1.0;
        };
        
        _InW.subtractInPlace(_InWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_InWLRGains));
        _InBiasW.subtractInPlace(_InBiasWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_InBiasWLRGains));
        _LayerW.subtractInPlace(_LayerWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_LayerWLRGains));
        _LayerBiasW.subtractInPlace(_LayerBiasWGrad.applyFunctionElementwiseInPlace(signum).timesElementByElement(_LayerBiasWLRGains));
        
        _InWVelocity = new Matrix(_InWGrad);
        _InBiasWVelocity = new Matrix(_InBiasWGrad);
        _LayerWVelocity = new Matrix(_LayerWGrad);
        _LayerBiasWVelocity = new Matrix(_LayerBiasWGrad);
        
    }
    
    
    private void updateLocalStepSizes(Matrix gains, Matrix velocity, Matrix grad)
    {
        double positiveFactor = 1.2;
        double negativeFactor = 0.5;
        double minGain = 0.000001;
        double maxGain = 30;
        
        Matrix tmp = velocity.timesElementByElement(grad);
        for(int i = 0; i < tmp.getNRows(); ++i){
            for(int j = 0; j < tmp.getNCols(); ++j){
                double oldGain = gains.getElem(i, j);
                double newGain = (tmp.getElem(i, j) > 0.0)? //if the same direction
                        oldGain*positiveFactor : oldGain*negativeFactor;
                if (newGain < minGain){
                    newGain = minGain;
                }
                if (newGain > maxGain){
                    newGain = maxGain;
                }
                gains.setElem(i, j, newGain);
            }
        }
    }
    
    
    private void updateLRLocalGains(Matrix gains, Matrix velocity, Matrix grad)
    {
        double additiveFactor = 0.05;
        double multiplicativeFactor = 0.95;
        double minGain = 0.01;
        double maxGain = 100;
        
        Matrix tmp = velocity.timesElementByElement(grad);
        for(int i = 0; i < tmp.getNRows(); ++i){
            for(int j = 0; j < tmp.getNCols(); ++j){
                double oldGain = gains.getElem(i, j);
                double newGain = (tmp.getElem(i, j) > 0.0)? //if the same direction
                        oldGain+additiveFactor : oldGain*multiplicativeFactor;
                if (newGain < minGain){
                    newGain = minGain;
                }
                if (newGain > maxGain){
                    newGain = maxGain;
                }
                gains.setElem(i, j, newGain);
            }
        }
    }
    
    
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("Feed-forward neural network:\n");
        sb.append("Input to hidden weights:\n");
        sb.append(getInW().toString());
        sb.append("\nTo hidden bias weights:\n");
        sb.append(getInBiasW().toString());
        sb.append("\nHidden to output weights:\n");
        sb.append(getLayerW().toString());
        sb.append("\nTo output bias weights:\n");
        sb.append(getLayerBiasW().toString());
        return sb.toString();
    }

}
