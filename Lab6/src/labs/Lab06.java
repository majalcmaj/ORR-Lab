/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package labs;

import datastructures.Matrix;
import distributedmodel.DSConfig;
import distributedmodel.DistributedSystem;
import distributedmodel.Node;
import neuralnets.FFNet;
import neuralnets.FFNetHeavy;
import neuralnets.FFNetParallel;
import neuralnets.FFNetParallelDouble;
import neuralnets.FFNetSequential;
import neuralnets.NNUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

/** The aim of lab 06 is to parallelize neural network training.
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Lab06 extends BaseLabMixed{

    @Override
    public void testAll() {

        // DEMONSTRATION 1
//        net_sequential_RegressionTraining();
        // DEMONSTRATION 2
//        weightUpdateComparison_XORTraining();


        // TASK 1 - DISTRIBUTED PARALLIZATION
         compareParallelToSequential();
        // TASK 2 - SHARED-MEM IN-NODE PARALLIZATION
        compareDoubleParallelToParallel();
    }


    private void weightUpdateComparison_XORTraining()
    {
        //-----Training-data-creation--------------------
        Matrix trainDataIn = new Matrix(2, 4, new double[]{0, 1, 0, 1, 0, 0, 1, 1});
        Matrix trainDataOut = new Matrix(1, 4, new double[] {0, 1, 1, 0});

        int nInputs = 2;
        int nHiddenUnits = 5;

        double LR = 0.1;
        int maxEpochs = 500000;
        long seed = 36678;

        FFNetHeavy net = new FFNetHeavy(nInputs, nHiddenUnits, seed);
        net.getTrainParam().learningRate = LR;
        net.getTrainParam().maxEpochs = maxEpochs;
        net.setTrainMethod(FFNetHeavy.TrainMethod.GD);
//        net.setHiddenActivFunc(ActivationUnit.getTanhUnit());
        XYSeries histGD = net.train(trainDataIn, trainDataOut);
        histGD.setKey("GD");

        FFNetHeavy net2 = new FFNetHeavy(nInputs, nHiddenUnits, seed);
        net2.getTrainParam().learningRate = LR;
        net2.getTrainParam().maxEpochs = maxEpochs;
        net2.setTrainMethod(FFNetHeavy.TrainMethod.GDM);
//        net2.setHiddenActivFunc(ActivationUnit.getTanhUnit());
        XYSeries histGDM = net2.train(trainDataIn, trainDataOut);
        histGDM.setKey("GDM");

        FFNetHeavy net3 = new FFNetHeavy(nInputs, nHiddenUnits, seed);
        net3.getTrainParam().learningRate = LR;
        net3.getTrainParam().maxEpochs = maxEpochs;
        net3.setTrainMethod(FFNetHeavy.TrainMethod.GDLALR);
//        net3.setHiddenActivFunc(ActivationUnit.getTanhUnit());
        XYSeries histGDLALR = net3.train(trainDataIn, trainDataOut);
        histGDLALR.setKey("GDLALR");

        FFNetHeavy net4 = new FFNetHeavy(nInputs, nHiddenUnits, seed);
        net4.getTrainParam().learningRate = LR;
        net4.getTrainParam().maxEpochs = maxEpochs;
        net4.setTrainMethod(FFNetHeavy.TrainMethod.RPROP);
//        net4.setHiddenActivFunc(ActivationUnit.getTanhUnit());
        XYSeries histRPROP = net4.train(trainDataIn, trainDataOut);
        histRPROP.setKey("RPROP");

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(histGD);
        seriesCollection.addSeries(histGDM);
        seriesCollection.addSeries(histGDLALR);
        seriesCollection.addSeries(histRPROP);
        plotTrainHistory(seriesCollection, true, 50);

        Matrix predDataOut = net.predict(trainDataIn);
        System.out.format("Predicted values (net1):\n%s", predDataOut);
        Matrix predDataOut2 = net2.predict(trainDataIn);
        System.out.format("\nPredicted values (net2):\n%s", predDataOut2);
        Matrix predDataOut3 = net3.predict(trainDataIn);
        System.out.format("\nPredicted values (net3):\n%s", predDataOut3);
        Matrix predDataOut4 = net4.predict(trainDataIn);
        System.out.format("\nPredicted values (net4):\n%s", predDataOut4);
    }


    private void net_sequential_RegressionTraining()
    {
        //-----Training-data-creation--------------------
        double minRange = -1.0;
        double maxRange = 1.0;
        int trainExamples = 64;
        int testExamples = 256;

        Matrix trainDataIn = getRegressionDataIn(minRange, maxRange, trainExamples);
        Matrix trainDataOut = getRegressionDataOut(trainDataIn);

        int nInputs = 1;
        int nHiddenUnits = 20;

        long seed = 13210;
        FFNetSequential net = new FFNetSequential(nInputs, nHiddenUnits, seed);
        net.getTrainParam().maxEpochs = 20000;
        net.getTrainParam().showInterval = 100;
        net.getTrainParam().performanceGoal = 1e-5;

        net.train(trainDataIn, trainDataOut);
        XYSeries hist = net.getPerformanceHistory();
        hist.setKey("rprop");

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(hist);
        plotTrainHistory(seriesCollection, false, 0);

        Matrix testDataIn = getRegressionDataIn(minRange, maxRange, testExamples);
        Matrix testDataOut = getRegressionDataOut(testDataIn);

        Matrix predDataOut = net.predict(testDataIn);
        plotRegressionResults(testDataIn, testDataOut, predDataOut, trainDataIn, trainDataOut);

        System.out.format("trainDataIn nCols: %d\n", trainDataIn.getNCols());

    }


    private void compareParallelToSequential()
    {
        //-----Training-data-creation--------------------
        double minRange = -1.0;
        double maxRange = 1.0;
        int trainExamples = 64;
        int testExamples = 256;

        Matrix trainDataIn = getRegressionDataIn(minRange, maxRange, trainExamples);
        Matrix trainDataOut = getRegressionDataOut(trainDataIn);
        int wholeTrainDataSetSize = trainDataIn.getNCols();

        int nInputs = 1;
        int nHiddenUnits = 20;
        int maxEpochs = 20;
        int showInterval = 1;
        long seed = 13210;


        //---Distribute-data-between-nodes
        int nNodes = 8;
        singleTestRun(new DSConfig(nNodes, 1, 1),
            (Node node) -> {
                Matrix myTrainDataIn = getMyColumnsOfMat(trainDataIn, node);
                Matrix myTrainDataOut = getMyColumnsOfMat(trainDataOut, node);

                FFNet net = new FFNetParallel(node, wholeTrainDataSetSize,
                        nInputs, nHiddenUnits, seed);
                net.getTrainParam().maxEpochs = maxEpochs;
                net.getTrainParam().showInterval = showInterval;
                net.getTrainParam().performanceGoal = 1e-5;

                net.train(myTrainDataIn, myTrainDataOut);
                XYSeries hist = net.getPerformanceHistory();

                if(node.getMyId() == 0){
                    hist.setKey("netParallel");

                    FFNet netS = new FFNetSequential(nInputs, nHiddenUnits, seed);
                    netS.getTrainParam().maxEpochs = maxEpochs;
                    netS.getTrainParam().showInterval = showInterval;
                    netS.getTrainParam().performanceGoal = 1e-5;

                    netS.train(trainDataIn, trainDataOut);
                    XYSeries histS = netS.getPerformanceHistory();
                    histS.setKey("netSerial");

                    XYSeriesCollection seriesCollection = new XYSeriesCollection();
                    seriesCollection.addSeries(hist);
                    seriesCollection.addSeries(histS);
                    plotTrainHistory(seriesCollection, false, 0);


                    if (net.hasEqualWeights(netS)){
                        System.out.println("Nets have the same weights - RESULT CORRECT.");
                    }else if (net.hasApproximatelyEqualWeights(netS, 0.001)){
                        System.out.println("Nets have approximately equal weights");
                    }else{
                        System.out.println("Nets have different weights - RESULT WRONG.");
                    }
                }


            },
            (DistributedSystem ds) -> { },
            (DistributedSystem ds) -> { },
            (DistributedSystem ds) -> {},
            (DistributedSystem ds) -> {},
            false);

    }


    private void compareDoubleParallelToParallel()
    {
        //-----Training-data-creation--------------------
        double minRange = -1.0;
        double maxRange = 1.0;
        int trainExamples = 64;
        int testExamples = 256;

        Matrix trainDataIn = getRegressionDataIn(minRange, maxRange, trainExamples);
        Matrix trainDataOut = getRegressionDataOut(trainDataIn);
        int wholeTrainDataSetSize = trainDataIn.getNCols();

        int nInputs = 1;
        int nHiddenUnits = 20;
        int maxEpochs = 4;
        int showInterval = 1;
        double perfGoal = 1e-5;
        long seed = 13210;

        int nLocalThreads = 4;


        //---Distribute-data-between-nodes
        int nNodes = 8;
        singleTestRun(new DSConfig(nNodes, 1, 1),
            (Node node) -> {
                Matrix myTrainDataIn = getMyColumnsOfMat(trainDataIn, node);
                Matrix myTrainDataOut = getMyColumnsOfMat(trainDataOut, node);

                FFNet net = new FFNetParallel(node, wholeTrainDataSetSize,
                        nInputs, nHiddenUnits, seed);
                net.getTrainParam().maxEpochs = maxEpochs;
                net.getTrainParam().showInterval = showInterval;
                net.getTrainParam().performanceGoal = perfGoal;
                net.train(myTrainDataIn, myTrainDataOut);
                XYSeries hist = net.getPerformanceHistory();

                FFNet net2 = new FFNetParallelDouble(node, wholeTrainDataSetSize,
                        nLocalThreads, nInputs, nHiddenUnits, seed);
                net2.getTrainParam().maxEpochs = maxEpochs;
                net2.getTrainParam().showInterval = showInterval;
                net2.getTrainParam().performanceGoal = perfGoal;
                net2.train(myTrainDataIn, myTrainDataOut);
                XYSeries hist2 = net2.getPerformanceHistory();

                if(node.getMyId() == 0){
                    hist.setKey("netParallel");
                    hist2.setKey("netParallelDouble");
                    XYSeriesCollection seriesCollection = new XYSeriesCollection();
                    seriesCollection.addSeries(hist);
                    seriesCollection.addSeries(hist2);
                    plotTrainHistory(seriesCollection, false, 0);

                    if (net.hasEqualWeights(net2)){
                        System.out.println("Nets have the same weights - RESULT CORRECT.");
                    }else if (net.hasApproximatelyEqualWeights(net2, 0.001)){
                        System.out.println("Nets have approximately equal weights");
                    }else{
                        System.out.println("Nets have different weights - RESULT WRONG.");
                    }
                }


            },
            (DistributedSystem ds) -> { },
            (DistributedSystem ds) -> { },
            (DistributedSystem ds) -> {},
            (DistributedSystem ds) -> {},
            false);

    }


    private Matrix getRegressionDataIn(double min, double max, double step)
    {
        double[] data = NNUtils.generateSequenceFixedStep(min, max, step);
        return new Matrix(1, data.length, data);
    }
    
    
    private Matrix getRegressionDataIn(double min, double max, int length)
    {
        double[] data = NNUtils.generateSequenceFixedLength(min, max, length);
        return new Matrix(1, length, data);
    }
    
    
    private Matrix getRegressionDataOut(Matrix in)
    {
        return in.applyFunctionElementwise(
            (double v) ->  {
                    return Math.pow(Math.sin(3.0*Math.PI*v), 2)
                            * Math.sin(Math.PI*v);
            });
    }

    
    
    private void plotRegressionResults(Matrix testDataIn, Matrix testDataOut,
            Matrix predDataOut, Matrix trainDataIn, Matrix trainDataOut)
    {
        
        
        XYSeries trainSeries = new XYSeries("Train examples");
        for(int i = 0; i < trainDataIn.getNCols(); ++i){
            trainSeries.add(trainDataIn.getElem(0, i), trainDataOut.getElem(0, i));
        }
        
        XYSeries testSeries = new XYSeries("Original values");
        for(int i = 0; i< testDataIn.getNCols(); ++i){
            testSeries.add(testDataIn.getElem(0, i), testDataOut.getElem(0, i));
        }
        
        XYSeries predictedSeries = new XYSeries("Predicted values");
        for(int i = 0; i< testDataIn.getNCols(); ++i){
            predictedSeries.add(testDataIn.getElem(0, i), predDataOut.getElem(0, i));
        }
        
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(testSeries);
        seriesCollection.addSeries(predictedSeries);
        seriesCollection.addSeries(trainSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Neural network as a function approximator", "x", "f(x)",
            seriesCollection,
            PlotOrientation.VERTICAL,
            true, // Show Legend
            true, // Use tooltips
            false // Configure chart to generate URLs?
            );
        
        //adding diamond marks to series
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
        r.setSeriesShape(2, ShapeUtilities.createDiamond(4));
        r.setSeriesShapesFilled(2, false);
        r.setSeriesShapesVisible(2, true);
        r.setSeriesLinesVisible(2, false);
        
        displayChart(chart);
    }
    
    private void plotTrainHistory(XYSeriesCollection sc, boolean log_xaxis, int smallest_x_value)
    {

        ValueAxis xAxis;
        if (log_xaxis){
            LogAxis axis = new LogAxis("Epoch");
            axis.setBase(10);
            axis.setSmallestValue(smallest_x_value);
            xAxis = axis;
        }else{
            xAxis = new NumberAxis("Epoch");
        }
//        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        LogAxis yAxis = new LogAxis("MSE");
        yAxis.setBase(10);
//        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYPlot plot = new XYPlot(sc,
            xAxis, yAxis, new XYLineAndShapeRenderer(true, false));
        JFreeChart chart = new JFreeChart(
            "Neural Net training", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        
        displayChart(chart);
    }
    
    
    
    
    private static Matrix getMyColumnsOfMat(Matrix m, Node node){
        int nColumns = m.getNCols();
        int nNodes = node.getNumberOfAllNodes();
        int myId = node.getMyId();
        return m.getSubmatrix(
            0,                                                  //upperLeft corner's row
            (myId*nColumns)/nNodes,                               //upperLeft corner's column
            m.getNRows(),                                          //number of rows
            ((myId+1)*nColumns)/nNodes - (myId*nColumns)/nNodes);   //number of columns
    }

    @Override
    protected void initSharedMemory(double[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
