/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package labs;

import algorithms.distributed.MatrixOperations;
import algorithms.shared.MatrixOperationsSM;
import datastructures.Matrix;
import distributedmodel.DSConfig;
import distributedmodel.DistributedSystem;
import distributedmodel.Node;

import static labs.BaseLab.generateRandomData;
import static labs.BaseLab.makeChart;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Lab04 extends BaseLabMixed {

    boolean verboseCommunication = false;
    boolean validateResults = true;
    boolean printProcessingTime = false;
    boolean printStatusBeforeAndAfter = true;


    @Override
    public void testAll() {

        // Shared Memory

        // Test 1
        testValiditySMMatrixMultiplication();

        // Test 2
        testScalabilitySMMatrixMultiplication();


        // Distributed Memory

        // Test 3 - simple example
//        singleTestRun(new DSConfig(4),
//            (Node node) -> {
//                node.setLogCommunication(verboseCommunication);
//                MatrixOperations.multiplySquareMtxSimple(node);} ,
//            (DistributedSystem ds) -> initNodesWithDataToy(ds),
//            (DistributedSystem ds) -> { if (printStatusBeforeAndAfter){printDSStateBefore(ds);}},
//            (DistributedSystem ds) -> { if (printStatusBeforeAndAfter){printDSStateAfter(ds);}},
//            (DistributedSystem ds) -> {if(validateResults){validateDSState(ds);}},
//            printProcessingTime);

        //Test 4 - bigger matrices, random elements, more nodes
//        singleTestRun(16,
//                (Node node) -> { node.setLogCommunication(verboseCommunication);
//                            MatrixOperations.multiplySquareMtxSimple(node);} ,
//                validateResults, printProcessingTime, printStatusBeforeAndAfter);


        // Test 5 - scalability tests
//        testDSAlgorithms();

    }


    private void testDSAlgorithms() {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();

        XYSeries exp1 = seriesOfRuns("Simple Parallel A*B",
                (Node node) -> {
                    node.setLogCommunication(verboseCommunication);
                    MatrixOperations.multiplySquareMtxSimple(node);
                },
                validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp1);

        JFreeChart chart = makeChart(seriesCollection,
                "Matrix multiplication scalability (constant matrix size)",
                "Number of nodes", "Execution time [s]");

        displayChart(chart);
    }


    private void testValiditySMMatrixMultiplication() {
        int nRows = 12;
        int nCols = 12;

        final Matrix a = new Matrix(nRows, nCols, generateRandomData(nRows * nCols));
        final Matrix b = new Matrix(nRows, nCols, generateRandomData(nRows * nCols));

        Matrix res1 = a.times(b);
        Matrix res2 = MatrixOperationsSM.multiplyMtx(a, b, 4, 4);

        if (printStatusBeforeAndAfter) {
            System.out.println("Should be:\n" + res1.toString());
            System.out.println("Is:\n" + res2.toString());
        }
        if (res1.isEqual(res2) || res1.isEqualApproximately(res2, 0.0001)) {
            successfulValidationInfo();
        } else {
            failedValidationInfo();
        }
    }


    private void testScalabilitySMMatrixMultiplication() {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();


        int nRows = 400;
        int nCols = 400;


        final Matrix a = new Matrix(nRows, nCols, generateRandomData(nRows * nCols));
        final Matrix b = new Matrix(nRows, nCols, generateRandomData(nRows * nCols));

        double time1 = singleMeasurement(null,
                (double[] d) -> {
                    Matrix res = a.times(b);
                });


        double time2 = singleMeasurement(null,
                (double[] d) -> {
                    Matrix res = MatrixOperationsSM.multiplyMtx(a, b, 4, 4);
                });

        System.out.format("Serial time = %f, Parallel time = %f\n", time1, time2);
    }


    @Override
    protected void initNodesWithData(DistributedSystem ds) {
        //int nNodes = ds.getConfiguration().getNumberOfNodes();
        int baseSize = 16;
        distrA = new Matrix(generateRandomDataForNodes(
                baseSize * 2, baseSize * 4), false);
        distrB = new Matrix(generateRandomDataForNodes(
                baseSize * 4, baseSize * 2), false);

        ds.scatterDistributedMatrix(distrA, Node.MatrixInNode.A);
        ds.scatterDistributedMatrix(distrB, Node.MatrixInNode.B);
    }


    private void initNodesWithDataToy(DistributedSystem ds) {
        int nNodes = ds.getConfiguration().getNumberOfNodes();
        int sideSize = (int) Math.sqrt(nNodes);
        double[] dataA = new double[nNodes * 6];
        double[] dataB = new double[nNodes * 6];

        for (int i = 0; i < dataA.length; ++i) {
            dataA[i] = Math.floor(i / (2 * sideSize));
            dataB[i] = i % (2 * sideSize);
        }
        distrA = new Matrix(2 * sideSize, 3 * sideSize, dataA);
        distrB = new Matrix(3 * sideSize, 2 * sideSize, dataB);
        ds.scatterDistributedMatrix(distrA, Node.MatrixInNode.A);
        ds.scatterDistributedMatrix(distrB, Node.MatrixInNode.B);
    }


    @Override
    protected void validateDSState(DistributedSystem ds) {

        Matrix expectedC = distrA.times(distrB);
        Matrix actualC = ds.collectDistributedMatrix(Node.MatrixInNode.C);
        if (actualC.isEqualApproximately(expectedC, 0.001)) {
            successfulValidationInfo();
        } else {
            failedValidationInfo();
        }

    }


    @Override
    protected void printDSStateBefore(DistributedSystem ds) {
        System.out.println("BEFORE :");
        ds.printDistributedMatrix(Node.MatrixInNode.A);
        ds.printDistributedMatrix(Node.MatrixInNode.B);
    }

    @Override
    protected void printDSStateAfter(DistributedSystem ds) {
        System.out.println("AFTER :");
        ds.printDistributedMatrix(Node.MatrixInNode.C);
        System.out.println("Expected matrix C :");
        System.out.println(distrA.times(distrB).toString());
    }


    @Override
    protected void initSharedMemory(double[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    Matrix distrA;
    Matrix distrB;

}
