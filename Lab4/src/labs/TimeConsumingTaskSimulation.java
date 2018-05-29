/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package labs;

import distributedmodel.DistributedSystem;
import software.TimeConsumingTaskOnOneNode;
import software.TimeConsumingTaskParallelized;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class TimeConsumingTaskSimulation extends BaseLabDS{
    
    public static long UNIT_PROCESSING_TIME_MILLIS = 300;

    @Override
    public void testAll() {
        
        boolean beVerbose = true;
        
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        
        XYSeries exp1 = seriesOfRuns("One node version", 
                new TimeConsumingTaskOnOneNode(UNIT_PROCESSING_TIME_MILLIS),
                beVerbose);
        seriesCollection.addSeries(exp1);
        
        XYSeries exp2 = seriesOfRuns("Parallel version",
                new TimeConsumingTaskParallelized(UNIT_PROCESSING_TIME_MILLIS),
                beVerbose);
        seriesCollection.addSeries(exp2);
        
        
        JFreeChart chart = makeChart(seriesCollection,
                "Heavy operation performance",
                "Number of nodes", "Execution time [s]");      
        
        displayChart(chart);
    }

    @Override
    protected void initNodesWithData(DistributedSystem ds) {
        ds.getNode(0).setMyData(generateRandomData(16));
    }
    
}
