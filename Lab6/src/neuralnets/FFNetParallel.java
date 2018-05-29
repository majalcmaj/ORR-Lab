/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import algorithms.distributed.BasicCommunication;
import datastructures.Matrix;
import distributedmodel.Node;
import java.util.function.BinaryOperator;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class FFNetParallel extends FFNetSequential {

    protected final Node _Node;
    protected final int _WholeTrainDataSize;
    
    
    
    
    public FFNetParallel(Node node, int wholeTrainDataSize,
            int nInputs, int nHiddenUnits)
    {
        super(nInputs, nHiddenUnits);
        _Node = node;
        _WholeTrainDataSize = wholeTrainDataSize;
        
    }
    
    public FFNetParallel(Node node, int wholeTrainDataSize,
            int nInputs, int nHiddenUnits, long seed)
    {
        super(nInputs, nHiddenUnits, seed);
        _Node = node;
        _WholeTrainDataSize = wholeTrainDataSize;
    }
    

    @Override
    public void train(Matrix trainDataIn, Matrix trainDataOut)
    {
        int epoch = 0;
        double performance;
        
        while(true){
            
            //------------------------FORWARD-PASS------------------------------
            
            Matrix outputs = forwardPass(trainDataIn);
            
            //----------------------CHECK-PERFORMANCE---------------------------
            //          (communication reduce-broadcast pattern)
            performance = calculatePerformanceEvaluation(trainDataOut, outputs);
            
            if(performance < _TrainParam.performanceGoal)
            {
                if(_Node.getMyId() == 0){
                    addToPerformanceHistory(epoch, performance);
                    System.out.format("Performance goal met after %d epochs.\n", epoch);
                }
                break;
            }
            
            if(epoch%_TrainParam.showInterval == 0 && _Node.getMyId() == 0){
                addToPerformanceHistory(epoch, performance);
                printInfo(epoch, performance);
            }
            
            calculateGradients(outputs, trainDataIn, trainDataOut); //backpropagation
            
            //communication phase 1: accumulate gradients in node 0
            accumulateGrads(_WholeTrainDataSize);

            //update of the weights is done only in one node
            if (_Node.getMyId() == 0){
                updateWeightsRPROP();
            }
            
            //communication phase 2: broadcast the updated weights
            broadcastWeights();
            
            epoch++;
                
            if(epoch > _TrainParam.maxEpochs){
                if(_Node.getMyId() == 0){
                    System.out.println("Maximum epochs reached.");
                }
                break;
            }
            
        }
    }
    
    
    
    @Override
    protected double calculatePerformanceEvaluation(Matrix trainDataOut, Matrix outputs)
    {
        double localPerf = EvaluationMeasures.sse(trainDataOut, outputs);
        
        double performance = BasicCommunication.reduceWithBarrier(_Node,
                        localPerf, (double p1, double p2) -> {
                            return p1+p2;                         
                        })/_WholeTrainDataSize;

        return BasicCommunication.broadcastWithBarrier(_Node,performance);
    }
    
    
    
    
    
    /** This method contains a communication phase between nodes in a distributed
     * system, that aims at accumulating gradients calculated in each node on a
     * disjoint subset of the train dataset into node 0.
     *
     */
    class SumMatrices implements BinaryOperator<double[]> {
        @Override
        public double[] apply(double[] doubles1, double[] doubles2) {
            Matrix mat1 = Matrix.deserialize(doubles1);
            Matrix mat2 = Matrix.deserialize(doubles2);
            Matrix result = mat1.add(mat2);
            return result.serialize();
        }
    }
    protected void accumulateGrads(int wholeTrainDataSize)
    {
        SumMatrices sum = new SumMatrices();
        Matrix inBiasWGrad = Matrix.deserialize(BasicCommunication.reduceWithBarrier(this._Node, this._InBiasWGrad.serialize(), sum));
        Matrix layerBiasWGrad = Matrix.deserialize(BasicCommunication.reduceWithBarrier(this._Node, this._LayerBiasWGrad.serialize(), sum));
        Matrix layerWGrad = Matrix.deserialize(BasicCommunication.reduceWithBarrier(this._Node, this._LayerWGrad.serialize(), sum));
        Matrix inWGrad = Matrix.deserialize(BasicCommunication.reduceWithBarrier(this._Node, this._InWGrad.serialize(), sum));

        inBiasWGrad.divide((double)wholeTrainDataSize);
        layerBiasWGrad.divide((double)wholeTrainDataSize);
        layerWGrad.divide((double)wholeTrainDataSize);
        inWGrad.divide((double)wholeTrainDataSize);
        if(this._Node.getMyId() == 0) {
            this._InBiasWGrad = inBiasWGrad;
            this._LayerBiasWGrad = layerBiasWGrad;
            this._LayerWGrad = layerWGrad;
            this._InWGrad = inWGrad;
        }
    }
    
    
    
    /** This method contains a communication phase between nodes in a distributed
     * system, that aims at broadcasting the updated weights (calculated in node
     * 0) to all the nodes in the system.
     * 
     */
    protected void broadcastWeights()
    {
        this._InBiasW = Matrix.deserialize(BasicCommunication.broadcast(this._Node, this._InBiasW.serialize()));
        this._LayerBiasW = Matrix.deserialize(BasicCommunication.broadcast(this._Node, this._LayerBiasW.serialize()));
        this._LayerW = Matrix.deserialize(BasicCommunication.broadcast(this._Node, this._LayerW.serialize()));
        this._InW = Matrix.deserialize(BasicCommunication.broadcast(this._Node, this._InW.serialize()));
    }
    
}
