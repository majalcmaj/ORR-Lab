/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package labs;

import algorithms.Utils;
import algorithms.distributed.BasicCommunication;
import algorithms.distributed.GraphAlgorithms;
import datastructures.Edge;
import datastructures.Matrix;
import distributedmodel.DSConfig;
import distributedmodel.DistributedSystem;
import distributedmodel.Node;
import java.io.File;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/** Main lab05 class, the topic of which is 'graph algorithms'
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Lab05 extends BaseLabDS{
    
    @Override
    public void testAll() {
        
        boolean printStatus = true;
        boolean verboseCommunication = false;
        boolean printProcessingTime = false;
        
//        testEdgesCommunication();
        
        testSerialPrim(verboseCommunication, printStatus, printProcessingTime);
        testParallelPrim(verboseCommunication, printStatus, printProcessingTime);
        
           testScalability();      //takes ca. 7 minutes (!)
        
    }
    
    
    private void testSerialPrim(boolean verboseCommunication, boolean printStatus,
                                boolean printProcessingTime)
    {
        
        int nNodes = 4;
        Matrix adj = loadAdjMtx("mat6.txt");
        
        singleTestRun(new DSConfig(nNodes), (Node node) -> {
            node.setLogCommunication(verboseCommunication);
            GraphAlgorithms.findMSTPrimSerial(node, printStatus);},
                (DistributedSystem ds) -> {ds.getNode(0).A = adj;},
                (DistributedSystem ds) -> { System.out.print(
                    "Adjacency mtx in node 0:\n"+ds.getNode(0).A.toString()); },
                (DistributedSystem ds) -> {},
                (DistributedSystem ds) -> {inDSValidateResult(ds, 8); },
                printProcessingTime);
        
    }
    
    
    private void testParallelPrim(boolean verboseCommunication, boolean printStatus,
                                boolean printProcessingTime)
    {
        
        int nNodes = 4;
        Matrix adj = loadAdjMtx("mat6.txt");
        
        singleTestRun(new DSConfig(nNodes), (Node node) -> {
            node.setLogCommunication(verboseCommunication);
            GraphAlgorithms.findMSTPrim(node, printStatus);},
                (DistributedSystem ds) -> { inDSDistributeAdjMtx(ds, adj); },
                (DistributedSystem ds) -> { ds.printDistributedMatrix(Node.MatrixInNode.A);},
                (DistributedSystem ds) -> {},
                (DistributedSystem ds) -> {inDSValidateResult(ds, 8); },
                printProcessingTime);
    }
    
    
    private void testScalability(){
        
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        
        seriesCollection.addSeries(serialSeries());
        
        int[] nNodesArray = new int[] {2, 4, 8};
        for(int nNodes : nNodesArray){
            seriesCollection.addSeries(parallelSeries(nNodes));
        }
        
        JFreeChart chart = makeChart(seriesCollection,
                "Prim algorithm implementations scalability comparison",
                "Number of graph vertices", "Execution time [s]"); 
        
        displayChart(chart);
    }
    
    
    
    private XYSeries parallelSeries(int nNodes)
    {
        int[] nVertices = new int[] { 16, 32, 64, 128};
        double[] expMSTs = new double[] {4.8093, 4.1119, 5.8113, 4.7748 };
        
        XYSeries series = new XYSeries(String.format("nNodes = %d", nNodes));
        
        for(int i = 0; i < nVertices.length; ++i){
            Matrix adj= loadAdjMtx("mat"+Integer.toString(nVertices[i])+".txt");
            double expMST = expMSTs[i];
            double time = singleTestRun(new DSConfig(nNodes),
                (Node node) -> {GraphAlgorithms.findMSTPrim(node, false);},
                (DistributedSystem ds) -> { inDSDistributeAdjMtx(ds, adj); },
                (DistributedSystem ds) -> {},
                (DistributedSystem ds) -> {},
                (DistributedSystem ds) -> {inDSValidateResult(ds, expMST);},
                false);
            series.add(nVertices[i], time);
        }
        
        return series;
    }
    
    private XYSeries serialSeries()
    {
        int[] nVertices = new int[] { 16, 32, 64, 128};
        double[] expMSTs = new double[] {4.8093, 4.1119, 5.8113, 4.7748 };
        
        XYSeries series = new XYSeries("nNodes = 1 (serial code)");
        
        for(int i = 0; i < nVertices.length; ++i){
            Matrix adj= loadAdjMtx("mat"+Integer.toString(nVertices[i])+".txt");
            double expMST = expMSTs[i];
            double time = singleTestRun(new DSConfig(1),
               (Node node) -> {GraphAlgorithms.findMSTPrimSerial(node, false);},
               (DistributedSystem ds) -> {ds.getNode(0).A = adj; },
               (DistributedSystem ds) -> {},
               (DistributedSystem ds) -> {},
               (DistributedSystem ds) -> {inDSValidateResult(ds, expMST);},
               false);
            series.add(nVertices[i], time);
        }
        
        return series;
    }
    
    
    
    
    
    
    private void testEdgesCommunication(){
        int nNodes = 4;
        
        //general broadcast and reduce tests
        Edge[] edges = new Edge[] {
            new Edge(0, 5, 3.00),
            new Edge(0, 1, 1.00),
            new Edge(1, 2, 0.50),
            new Edge(2, 4, 1.01)};
        
        singleTestRun(nNodes,
            (Node node) -> {
                double[] output = BasicCommunication.reduce(
                    node,
                    edges[node.getMyId()].serialize(),
                        (double[] a, double[] b) -> {
                            Edge e1 = Edge.deserialize(a);
                            Edge e2 = Edge.deserialize(b);
                            return (e1.weight < e2.weight)? a : b;
                        });
                if (node.getMyId() == 0){
                    System.out.println(Edge.deserialize(output).toString());
                }

                output = BasicCommunication.broadcast(node, output);
                System.out.printf("Node[%d] got: %s\n",
                        node.getMyId(), Edge.deserialize(output).toString());
            },
            false, false, false);
    }
    
    
    private static void printValidationInfo(double actual, double expected, double eps){
        if(Utils.almostEqual(actual, expected, eps)){
            System.out.println("Result CORRECT.");
        }else{
            System.out.format("Result NOT correct (expected: "
                    + "%6.4f, actual: %6.4f).%n", expected, actual);
        }
    }
        
    
    private static Matrix loadAdjMtx(String filename){
        String wdPath = System.getProperty("user.dir");
        return rawMtxToAdjacencyMtx(Matrix.readFromCSVFile(
            wdPath+File.separator+"data"+File.separator+filename));
    }
    
    
    private static void inDSDistributeAdjMtx(DistributedSystem ds, Matrix adj){
        int nVertices = adj.getNCols();
        int nNodes = ds.getNodes().length;
        for(int n = 0; n < nNodes; ++n){
            ds.getNode(n).A = adj.getSubmatrix(
                0,                                                  //upperLeft corner's row
                (n*nVertices)/nNodes,                               //upperLeft corner's column
                nVertices,                                          //number of rows
                ((n+1)*nVertices)/nNodes - (n*nVertices)/nNodes);   //number of columns
        }
    }
    
    private static void inDSValidateResult(DistributedSystem ds,
            double expMST)
    {
        double[] node0Data = ds.getNode(0).getMyData();
        double actualMST = node0Data[0];
        printValidationInfo(actualMST, expMST, 0.001);
    }
    
    private static Matrix rawMtxToAdjacencyMtx(Matrix m){
        Matrix ret = new Matrix(m);
        for(int r = 0; r<ret.getNRows(); ++r){
            for(int c=0; c<ret.getNCols(); ++c){
                if(r!=c && ret.getElem(r, c)==0){
                    ret.setElem(r, c, Double.POSITIVE_INFINITY);
                }
            }
        }
        return ret;
    }
}
