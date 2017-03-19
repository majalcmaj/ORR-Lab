/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package labs;

import algorithms.distributed.BasicCommunication;
import algorithms.shared.BasicCommunicationSharedMem;
import distributedmodel.DistributedSystem;
import distributedmodel.Node;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Lab02 extends BaseLabMixed{
    
    boolean verboseCommunication = false;
    boolean validateResults = true;
    boolean printProcessingTime = false;
    boolean printStatusBeforeAndAfter = false;
    
    
    @Override
    public void testAll() {
        
        singleTestRun(8,
                (Node node) -> {
                    node.setLogCommunication(verboseCommunication);
                    BasicCommunication.scan(node, (a,b)-> a+b);},
                validateResults, printProcessingTime, printStatusBeforeAndAfter);
        
        singleTestRun(8,
                (double[] data) -> {
                BasicCommunicationSharedMem.scan(data, (a,b)-> a+b);},
                validateResults, printProcessingTime, printStatusBeforeAndAfter);
        
        //Uncomment this later:
//        testDistributedAlgorithms();
//        testSharedAlgorithms();
    }
    
    
    
    
    
    
    
    private void testDistributedAlgorithms(){
        
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        
        XYSeries exp1 = seriesOfRuns("Naive version", 
            (Node node) -> {
                node.setLogCommunication(verboseCommunication);
                BasicCommunication.scanNaive(node, (a,b)-> a+b);},
            validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp1);
        
        XYSeries exp2 = seriesOfRuns("Correct version",
            (Node node) -> {
                node.setLogCommunication(verboseCommunication);
                BasicCommunication.scan(node, (a,b)-> a+b);},
            validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp2);
        
        JFreeChart chart = makeChart(seriesCollection,
                "Scan performance (distributed system)",
                "Number of nodes", "Execution time [s]");      
        
        displayChart(chart);
    }
    
    private void testSharedAlgorithms(){
        
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        
        XYSeries exp1 = seriesOfRuns("Serial version", 
            (double[] data) -> {
                BasicCommunicationSharedMem.scanSerial(data, (a,b)-> a+b);},
            validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp1);
        
        XYSeries exp2 = seriesOfRuns("Correct version", 
            (double[] data) -> {
                BasicCommunicationSharedMem.scan(data, (a,b)-> a+b);},
            validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp2);
        
        JFreeChart chart = makeChart(seriesCollection,
                "Scan performance (shared memory)",
                "Number of elements to reduce", "Execution time [s]"); 
        
        displayChart(chart);
    }
    

    

    @Override
    protected void initNodesWithData(DistributedSystem ds) {
        for (int j = 0; j < ds.getConfiguration().getNumberOfNodes(); ++j){
            ds.getNode(j).setMyData(new double[] { 1.0 });
        }
    }

    
    @Override
    protected void validateDSState(DistributedSystem ds) {
        for (int i = 0; i < ds.getConfiguration().getNumberOfNodes(); ++i){
            double[] nodeData = ds.getNode(i).getMyData();
            if (nodeData == null || nodeData.length != 1 || nodeData[0] != i+1){
                failedValidationInfo();
                return;
            }
        }
        successfulValidationInfo();
    }

    
    @Override
    protected void initSharedMemory(double[] data) {
        for(int j=0; j< data.length; ++j) {data[j] = 1.0;}
    }
    
    
    @Override
    protected void validateSharedMemory(double[] data) {
        for(int j=0; j< data.length; ++j) {
            if(data[j] != j+1){
                failedValidationInfo();
                return;
            }
        }
        successfulValidationInfo();
    }
    
}
