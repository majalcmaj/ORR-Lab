/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package labs;

import distributedmodel.DistributedSystem;
import distributedmodel.Node;
import algorithms.distributed.BasicCommunication;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/** Lab01 class with scatter exercise.
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Lab01S extends BaseLabDS{

    private final static int DATASIZE = 16;
    private double[] initialNode0Data;

    @Override
    public void testAll() {
        boolean verboseCommunication = false;
        boolean validateResults = true;
        boolean printProcessingTime = false;
        boolean printStatusBeforeAndAfter = false;

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        
        XYSeries exp1 = seriesOfRuns("Naive version",
                (Node node) -> {
                    node.setLogCommunication(verboseCommunication);
                    BasicCommunication.scatterNaive(node);},
                validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp1);
        
        XYSeries exp2 = seriesOfRuns("Correct version",
                (Node node) -> {
                    node.setLogCommunication(verboseCommunication);
                    BasicCommunication.scatter(node);},
                validateResults, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp2);
        
        JFreeChart chart = makeChart(seriesCollection, "Scatter performance",
                "Number of nodes", "Execution time [s]");      
        
        displayChart(chart);
    }

    @Override
    protected void initNodesWithData(DistributedSystem ds)
    {
        initialNode0Data = generateRandomData(DATASIZE);
        ds.getNode(0).setMyData(initialNode0Data);
    }


    @Override
    protected void validateDSState(DistributedSystem ds)
    {
        int nNodes = ds.getConfiguration().getNumberOfNodes();
        int expectedNodeDataSize = DATASIZE/nNodes;

        for(int i = 0; i < nNodes; ++i){

            double[] nodeData = ds.getNode(i).getMyData();

            if (nodeData == null || nodeData.length != expectedNodeDataSize){
                failedValidationInfo();
                return;
            }

            for(int j = 0; j < expectedNodeDataSize; ++j){
                if (nodeData[j] != initialNode0Data[i*expectedNodeDataSize+j]){
                    failedValidationInfo();
                    return;
                }

            }
        }

        successfulValidationInfo();
    }

}

