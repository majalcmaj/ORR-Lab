/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package software;

import distributedmodel.Node;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class TimeConsumingTaskOnOneNode implements SoftwareDS{

    private final long mProcessingTime;
    
    public TimeConsumingTaskOnOneNode(long processing_time_milis){
        mProcessingTime = processing_time_milis;
    }
    
    public TimeConsumingTaskOnOneNode(){
        this(DEFAULT_SIMULATION_UNIT_PROCESSING_TIME);
    }
    
    @Override
    public void instructions(Node node) {
        if (node.getMyId() == 0){
            double[] myData = node.getMyData();
            double result = 0;
            for(int i = 0; i < myData.length; ++i){
                try {
                    Thread.sleep(mProcessingTime);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TimeConsumingTaskOnOneNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                result += myData[i];
            }
            myData[0] = result;
            node.setMyData(myData);
        }
    }
    
}
