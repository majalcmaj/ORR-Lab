/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package software;

import algorithms.distributed.BasicCommunication;
import distributedmodel.Node;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class TimeConsumingTaskParallelized implements SoftwareDS{

    private final long mProcessingTime;
    
    
    public TimeConsumingTaskParallelized(long processing_time_milis){
        mProcessingTime = processing_time_milis;
    }
    
    public TimeConsumingTaskParallelized(){
        this(DEFAULT_SIMULATION_UNIT_PROCESSING_TIME);
    }
    
    @Override
    public void instructions(Node node) {
        
        BasicCommunication.scatter(node);
        
        double[] myData = node.getMyData();
        double result = 0;
        for(int i = 0; i < myData.length; ++i){
            try {
                Thread.sleep(mProcessingTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(TimeConsumingTaskParallelized.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            result += myData[i];
        }
        myData[0] = result;
        node.setMyData(myData);
        
        BasicCommunication.reduce(node, (a, b) -> a+b);
    }
    
}
