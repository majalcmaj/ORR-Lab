/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.distributed;

import algorithms.Utils;
import distributedmodel.DataPacket;
import distributedmodel.Node;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class BasicCommunication {


     /** A subroutine that, if run on each of the nodes in the distributed 
     * system, broadcasts the data stored in node[0] to all the other nodes in
     * \Theta(log(nNodes)) time.
     * 
     * @param node - the node that takes part in broadcast operation
     */
    public static void broadcast(Node node){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        //this is actually taken from GramaGupta ch. 4 (one2all broadcast)
        int mask = nNodes - 1;   //set all d bits of mask to 1
        int powerOfTwo = nNodes;
        for(int i=Utils.binlog(powerOfTwo)-1; i>=0; --i){

            powerOfTwo >>= 1;
            mask ^= powerOfTwo;   //set bit i of mask to 0

            if ( (myIdx & mask) == 0 ){       //if lower i bits of idx are 0
                if ( (myIdx & powerOfTwo) == 0 ){
                    node.sendMyData(myIdx ^ powerOfTwo);
                }else{
                    node.receiveAndSet();
                }
            }

        }
    }
    
    
    /** A subroutine that, if run on each of the nodes in the distributed 
     * system, broadcasts data array of node 0 to each of the nodes (as a
     * returned value) in \Theta(log(N)) time.
     * 
     * @param node      - node of a distributed system
     * @param data      - data to broadcast - only meaningful for node 0
     * @return          - broadcasted data
     */
    public static double[] broadcast(Node node, double[] data){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        double[] dataToSend = data;
        
        //this is actually taken from GramaGupta ch. 4 (one2all broadcast)
        int mask = nNodes - 1;   //set all d bits of mask to 1
        int powerOfTwo = nNodes;
        for(int i=Utils.binlog(powerOfTwo)-1; i>=0; --i){

            powerOfTwo >>= 1;
            mask ^= powerOfTwo;   //set bit i of mask to 0

            if ( (myIdx & mask) == 0 ){       //if lower i bits of idx are 0
                if ( (myIdx & powerOfTwo) == 0 ){
                    node.send(myIdx ^ powerOfTwo, dataToSend);
                }else{
                    dataToSend = node.receive().getData();
                }
            }
        }
        return dataToSend;
    }

    
    public static double[] broadcastWithBarrier(Node node, double[] data){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        double[] dataToSend = data;
        
        //this is actually taken from GramaGupta ch. 4 (one2all broadcast)
        int mask = nNodes - 1;   //set all d bits of mask to 1
        int powerOfTwo = nNodes;
        for(int i=Utils.binlog(powerOfTwo)-1; i>=0; --i){

            powerOfTwo >>= 1;
            mask ^= powerOfTwo;   //set bit i of mask to 0

            if ( (myIdx & mask) == 0 ){       //if lower i bits of idx are 0
                if ( (myIdx & powerOfTwo) == 0 ){
                    node.send(myIdx ^ powerOfTwo, dataToSend);
                }else{
                    dataToSend = node.receive().getData();
                }
            }
        }
        node.synchronizeDS();
        
        return dataToSend;
    }
    
    
    public static double broadcastWithBarrier(Node node, double data)
    {
        double[] res = broadcastWithBarrier(node, new double[] {data});
        return res[0];
    }

    
    /** All nodes broadcast their data to all the other. The result however is
     * not stored inside the node but returned by the function. Each node can
     * then save this result as its data if desirable.
     * The ring passing algorithm.
     * 
     * @param node
     * @return 
     */
    public static double[][] broadcastAll2All(Node node){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        double[][] result = new double[nNodes][];
        
        int right = (myIdx+1)%nNodes;
        
        double[] dataToSend = node.getMyData();
        result[myIdx] = dataToSend.clone();
        
        for(int i = 1; i<nNodes; ++i){
            DataPacket dpReceived = null;
            
            //Even-odd sending-receiving pattern because of synchronous
            //double-blocking communication
            if(myIdx%2 == 0){ node.send(right, dataToSend); }
            else{ dpReceived = node.receive(); }
            if(myIdx%2 != 0){ node.send(right, dataToSend); }
            else{ dpReceived = node.receive(); }
            
            if(dpReceived != null){
                dataToSend = dpReceived.getData();
            }
            
            int source = (myIdx-i)%nNodes;
            if(source<0){ source += nNodes;}    //lack of modulo operator in Java :(
            result[source] = dataToSend.clone();
        }
        
        return result;
    }

    
    /** Simple routine that returns the data of otherId node to the current node
     * (and do same to the otherId node, i.e. returns the data of current node).
     * Uses synchronous communication so the first node that called the function
     * is blocked until the other also called it.
     * 
     * @param node  - 'current' node (the caller)
     * @param otherId - the other node with which the first one wants to
     * exchange data with
     * @param myData - data to send by 'current' node
     * @return - the data obtained from the other node
     */
    public static double[] exchangeWith(Node node, int otherId, 
            double[] myData)
    {
        int myId = node.getMyId();
        double[] result;
        
        if(myId<otherId){
            node.send(otherId, myData);
            result = node.receive().getData();
        }else if(myId>otherId){
            result  = node.receive().getData();
            node.send(otherId, myData);
        }else{
            throw new RuntimeException(String.format(
                    "Node %d tries to exchange data with itself.%n", myId));
        }
        return result;
    }
    
    
    
    /** Simple routine that returns the data of otherId node to the current node
     * (and do same to the otherId node, i.e. returns the data of current node).
     * Uses synchronous communication so the first node that called the function
     * is blocked until the other also called it.
     * 
     * @param node  - 'current' node (the caller)
     * @param otherId - the other node with which the first one wants to
     * exchange data with
     * @param myData - data to send by 'current' node
     * @return - the data obtained from the other node
     */
    public static double exchangeWith(Node node, int otherId, 
            double myData)
    {
        int myId = node.getMyId();
        double[] result;
        
        if(myId<otherId){
            node.send(otherId, new double[] {myData});
            result = node.receive().getData();
        }else if(myId>otherId){
            result  = node.receive().getData();
            node.send(otherId, new double[] {myData});
        }else{
            throw new RuntimeException(String.format(
                    "Node %d tries to exchange data with itself.%n", myId));
        }
        return result[0];
    }
    
    
    /** A subroutine that, if run on each of the nodes in the distributed 
     * system, reduces data[0] elements of each of the nodes to data[0] in node 
     * 0 using the given DoubleBinaryOperator (which should be an associative 
     * operation)
     * 
     * @param node - a node of the distributed system
     * @param op - associative double binary operator (like + or * or max)
     */
    public static void reduce(Node node, DoubleBinaryOperator op){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        //this is actually taken from GramaGupta ch. 4 (all2one reduce)
        int mask = 0;   //set all d bits of mask to 0
        int powerOfTwo = 1;
        for(int i=0; i<Utils.binlog(nNodes); ++i){
            if ( (myIdx & mask) == 0 ){       //if lower i bits of idx are 0
                if ( (myIdx & powerOfTwo) != 0 ){
                    node.sendMyData(myIdx ^ powerOfTwo);
                }else{
                    DataPacket dp = node.receive();
                    double[] extData = dp.getData();
                    double[] myData = node.getMyData();
                    myData[0] = op.applyAsDouble(myData[0], extData[0]);
                    node.setMyData(myData);
                }
            }
            mask ^= powerOfTwo;   //set bit i of mask to 1
            powerOfTwo <<= 1;
        }
    }
    
    
    
    /** A subroutine that, if run on each of the nodes in the distributed 
     * system, reduces data elements of each of the nodes to returned value
     * in node 0 using the given BinaryOperator (which should be an
     * associative operation) in \Theta(log(N)) time.
     * 
     * @param node   - a node in the distributed system
     * @param data   - data to reduce
     * @param op     - associative reduce operation of type (double[], double[]) -> double[]
     * @return       - reduced data - this has a meaningful value only in node 0
     */
    public static double[] reduce(Node node, double[] data,
            BinaryOperator<double[]> op){
        
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        double[] dataToSend = data;
        
        //this is actually taken from GramaGupta ch. 4 (all2one reduce)
        int mask = 0;   //set all d bits of mask to 0
        int powerOfTwo = 1;
        for(int i=0; i<Utils.binlog(nNodes); ++i){
            if ( (myIdx & mask) == 0 ){       //if lower i bits of idx are 0
                if ( (myIdx & powerOfTwo) != 0 ){
                    node.send(myIdx ^ powerOfTwo, dataToSend);
                }else{
                    DataPacket dp = node.receive();
                    double[] externalData = dp.getData();
                    dataToSend = op.apply(dataToSend, externalData);
                }
            }
            mask ^= powerOfTwo;   //set bit i of mask to 1
            powerOfTwo <<= 1;
        }
        return dataToSend;
    }
    
    
    
    public static double[] reduceWithBarrier(Node node, double[] data,
            BinaryOperator<double[]> op){
        
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        double[] dataToSend = data;
        
        //this is actually taken from GramaGupta ch. 4 (all2one reduce)
        int mask = 0;   //set all d bits of mask to 0
        int powerOfTwo = 1;
        for(int i=0; i<Utils.binlog(nNodes); ++i){
            if ( (myIdx & mask) == 0 ){       //if lower i bits of idx are 0
                if ( (myIdx & powerOfTwo) != 0 ){
                    node.send(myIdx ^ powerOfTwo, dataToSend);
                }else{
                    DataPacket dp = node.receive();
                    double[] externalData = dp.getData();
                    dataToSend = op.apply(dataToSend, externalData);
                }
            }
            mask ^= powerOfTwo;   //set bit i of mask to 1
            powerOfTwo <<= 1;
        }
        
        node.synchronizeDS();
        
        return dataToSend;
    }
    
    
    public static double reduceWithBarrier(Node node, double localValueToReduce,
            DoubleBinaryOperator op){
        
        double[] tmp = reduceWithBarrier(node, new double[] {localValueToReduce},
                (double[] v1, double[] v2) -> {
                    double res = op.applyAsDouble(v1[0], v2[0]);
                    return new double[] {res};
                });
        return tmp[0];
    }
}
