/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import datastructures.Matrix;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import org.jfree.data.xy.XYSeries;

/** An abstract class representing a simple feed forward neural network
 * (sometimes called multi-layer perceptron - MLP) for regression problems.
 * The net has one hidden layer of neurons (with logistic activation function)
 * and a single output neuron with linear activation function.
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public abstract class FFNet {
    
    protected final int _NInputs;       //number of inputs to the network
    protected final int _NHiddenUnits;  //number of hidden units
    
    
    /** Weights at input to hidden connections.
     * Size: [nHidden , nFeatures]
     */
    protected Matrix _InW;
    protected Matrix _InWGrad;        //gradients wrt corresponding weights
    
    public Matrix getInW(){
        return _InW;
    }
    public void setInW(Matrix inWeights){
        _InW = inWeights;
    }
    
    /** Weights at bias to hidden connections.
     * Size: [nHidden, 1]
     */
    protected Matrix _InBiasW;
    protected Matrix _InBiasWGrad;        //gradients wrt corresponding weights
    
    public Matrix getInBiasW(){
        return _InBiasW;
    }
    public void setInBiasW(Matrix inBiasWeights){
        _InBiasW = inBiasWeights;
    }
    
    /** Weights at hidden to output connections
     * Size: [1, nHidden]
     */
    protected Matrix _LayerW;
    protected Matrix _LayerWGrad;    //gradients wrt corresponding weights
    
    public Matrix getLayerW(){
        return _LayerW;
    }
    public void setLayerW(Matrix layerWeights){
        _LayerW = layerWeights;
    }
    
    /** Weights at bias to hidden connections
     * Size: [1, 1]
     */
    protected Matrix _LayerBiasW;
    protected Matrix _LayerBiasWGrad;    //gradients wrt corresponding weights
    
    public Matrix getLayerBiasW(){
        return _LayerBiasW;
    }
    public void setLayerBiasW(Matrix layerBiasWeights){
        _LayerBiasW = layerBiasWeights;
    }
    
    
    /** Activation unit of the hidden units (logistic)
     */
    protected ActivationUnit _AFHidden;
    
    public ActivationUnit getHiddenActivFunc(){
        return _AFHidden;
    }
    public void setHiddenActivFunc(ActivationUnit af){
        _AFHidden = af;
    }
    /** Actication unit of the output unit (purelin)
     */
    protected final ActivationUnit _AFOut;
    
    
    protected Matrix _HiddenState;            //[nHiddenUnits x batchsize]      
    protected Matrix _deltasAtHidden;         //[nHiddenUnits x batchsize]

   
    public class TrainParam
    {
        public static final double DEFAULT_LR = 0.1;
        public static final int DEFAULT_MAX_EPOCHS = 10000;
        public static final double DEFAULT_PERFORMANCE_GOAL = 1e-4;
        public static final int DEFAULT_SHOW_INTERVAL = 100;
        
        public double learningRate;
        public double momentum;
        public int maxEpochs;
        public double performanceGoal;
        public int showInterval;
        
        //rprop parameters
        public double positiveFactor = 1.5;
        public double negativeFactor = 0.5;
        public double minGain = 0.000001;
        public double maxGain = 30;
        public double initial_step_size = 0.001;
        
        public TrainParam(){
            learningRate = DEFAULT_LR;
            maxEpochs = DEFAULT_MAX_EPOCHS;
            performanceGoal = DEFAULT_PERFORMANCE_GOAL;
            showInterval = DEFAULT_SHOW_INTERVAL;
        }
    }
    
    protected final TrainParam _TrainParam;
    
    public TrainParam getTrainParam(){
        return _TrainParam;
    }
    
    /** XYSeries with performance train history to plot capabilities
     * 
     */
    protected final XYSeries _PerformanceHistory;
    public XYSeries getPerformanceHistory(){
        return _PerformanceHistory;
    }
    
    protected static final double DEFAULT_INIT_WEIGHTS_STD = 0.01;
    
    
    /** Constructor. Initializes weights to random weights.
     * 
     * @param nInputs       - number of inputs to the net (=number of features)
     * @param nHiddenUnits  - humber of units in the hidden layer
     */
    public FFNet(int nInputs, int nHiddenUnits){
        
        _NInputs = nInputs;
        _NHiddenUnits = nHiddenUnits;
        _AFHidden = ActivationUnit.getLogisticUnit();
        _AFOut = ActivationUnit.getPurelinUnit();
        _TrainParam = new TrainParam();
        _PerformanceHistory = new XYSeries("Train MSE");
        
        initializeWeights(DEFAULT_INIT_WEIGHTS_STD, new Random());
    }
    
    
    /** Constructor. Initializes weights to random weights using specified seed.
     * 
     * @param nInputs       - number of inputs to the net (=number of features)
     * @param nHiddenUnits  - humber of units in the hidden layer
     * @param seed          - seed for rng
     */
    public FFNet(int nInputs, int nHiddenUnits, long seed){
        
        _NInputs = nInputs;
        _NHiddenUnits = nHiddenUnits;
        _AFHidden = ActivationUnit.getLogisticUnit();
        _AFOut = ActivationUnit.getPurelinUnit();
        _TrainParam = new TrainParam();
        _PerformanceHistory = new XYSeries("Performance");
        
        initializeWeights(DEFAULT_INIT_WEIGHTS_STD, new Random(seed));
    }
    
    
    private void initializeWeights(double std, Random r) {
        _InW = new Matrix(_NHiddenUnits, _NInputs,
                NNUtils.generateNormalRandomData(_NHiddenUnits*_NInputs, std, r));
        _InBiasW = new Matrix(_NHiddenUnits, 1,
                NNUtils.generateNormalRandomData(_NHiddenUnits, std, r));
        _LayerW = new Matrix(1, _NHiddenUnits,
                NNUtils.generateNormalRandomData(_NHiddenUnits, std, r));
        _LayerBiasW = new Matrix(1, 1,
                NNUtils.generateNormalRandomData(1, std, r));
    }
    
    /** Train the net using standard gradient descent method.
     * 
     * @param trainDataIn       - input train data    [nFeats x nTrainObj]
     * @param trainDataOut      - output train data   [1 x nTrainObj]
     */
    public abstract void train(Matrix trainDataIn, Matrix trainDataOut);
    
    
    
    protected double calculatePerformanceEvaluation(Matrix trainDataOut, Matrix outputs)
    {
        return EvaluationMeasures.mse(trainDataOut, outputs);
    }
    
    
    protected void addToPerformanceHistory(int epoch, double performance)
    {
        _PerformanceHistory.add(epoch, performance);
    }
    
    
    protected void printInfo(int epoch, double performance)
    {
        System.out.format("Epoch %d, trainMSE = %8.6f\n", epoch, performance);
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
    protected Matrix forwardPass(Matrix input)
    {
        Matrix inputToHidden = _InW.times(input).addInPlaceRepeatedColumn(_InBiasW);
        _HiddenState = inputToHidden.applyFunctionElementwise(_AFHidden.getActivationFun());
        Matrix inputToOutput = _LayerW.times(_HiddenState).addInPlaceRepeatedColumn(_LayerBiasW);
        Matrix output = inputToOutput.applyFunctionElementwise(_AFOut.getActivationFun());
        return output;
    }
    
    
    protected void backwardPass(Matrix lastDelta)
    {
        Matrix derivHiddenActiv = _HiddenState.applyFunctionElementwise(_AFHidden.getGradientFun());
        _deltasAtHidden = _LayerW.transpose()
                            .times(lastDelta)
                            .timesElementByElementInPlace(derivHiddenActiv);
    }
    
    
    
   /** The backpropagation algorithm to calculate gradients of the cost function
    * wrt all the parameters.
    * 
     * @param outputs
    * @param trainDataIn 
     * @param trainDataOut 
    */
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
    
    
    /** Checks whether the weight matrices of both nets are equal.
     * 
     * @param other
     * @return 
     */
    public boolean hasEqualWeights(FFNet other)
    {
        if (!_InW.isEqual(other._InW)){ return false;}
        if (!_InBiasW.isEqual(other._InBiasW)) { return false;}
        if (!_LayerW.isEqual(other._LayerW)) { return false; }
        return _LayerBiasW.isEqual(other._LayerBiasW);
    }
    
    /** Checks whether the weight matrices of both nets are approximately equal.
     * 
     * @param other
     * @param eps       - the absolute tolerance between single corresponding
     *                    elements in matrices
     * @return 
     */
    public boolean hasApproximatelyEqualWeights(FFNet other, double eps)
    {
        if (!_InW.isEqualApproximately(other._InW, eps)){ return false;}
        if (!_InBiasW.isEqualApproximately(other._InBiasW, eps)) { return false;}
        if (!_LayerW.isEqualApproximately(other._LayerW, eps)) { return false; }
        return _LayerBiasW.isEqualApproximately(other._LayerBiasW, eps);
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
