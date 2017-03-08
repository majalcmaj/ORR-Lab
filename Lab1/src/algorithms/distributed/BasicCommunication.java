/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.distributed;

import distributedmodel.Node;
import static algorithms.Utils.*;
/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class BasicCommunication {

    /** A subroutine that, if run on each of the nodes in the distributed
     * system, scatters the data stored in node[0] among all the other nodes in
     * \Theta(log(nNodes)) time.
     *      In the scatter operation, the data in node[0] is divided into nNodes
     * equal parts and each of the parts go to different node, so that in the
     * end all the nodes have part of the data initially stored in node[0].
     * 
     * @param node - the node that takes part in scatter operation
     */
    public static void scatter(Node node){
        int myId = node.getMyId();
        int nodesCount = node.getNumberOfAllNodes();
        int totalSteps = binlog(nodesCount);
        int sendsToMake = 0;
        while (sendsToMake < totalSteps) {
            if((myId & (1 << sendsToMake)) != 0)
                break;
            else
                sendsToMake++;
        }

        if(myId != 0) {
            node.receiveAndSet();
        }

        for(;sendsToMake > 0; sendsToMake--) {
            double[] myData = node.getMyData();
            int dataLength = myData.length;
            double[] myNewData = new double[dataLength / 2];
            double[] dataToSend = new double[dataLength / 2];
            System.arraycopy(myData, dataLength/2, dataToSend, 0, dataLength / 2);
            node.send(myId + (1 << (sendsToMake - 1)), dataToSend);
            System.arraycopy(myData, 0, myNewData, 0, dataLength / 2 );
            node.setMyData(myNewData);

        }
    }
    
    
    
    /** Theta(nNodes) version of @see BasicCommunication#scatter
     * 
     * @param node 
     */
    public static void scatterNaive(Node node){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        if (myIdx == 0){
            double[] myData = node.getMyData();
            int partSize = myData.length / nNodes;
            double[] myNewData = new double[partSize];
            System.arraycopy(myData, 0, myNewData, 0, partSize);
            node.setMyData(myNewData);
            for(int i = 1; i< nNodes; ++i){
                double[] dataToSend = new double[partSize];
                System.arraycopy(myData, i*partSize, dataToSend, 0, partSize);
                node.send(i, dataToSend);
            }
        }else{
            node.receiveAndSet();
        }
    }
}
