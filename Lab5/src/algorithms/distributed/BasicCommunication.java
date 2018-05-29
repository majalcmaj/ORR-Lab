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
    
    
    /** All nodes broadcast their data to all the other. The result however is
     * not stored inside the node but returned by the function. Each node can
     * then save this result as its data if desirable.
     * Series of one2all broadcasts version.
     * 
     * @param node
     * @return 
     */
    public static double[][] broadcastAll2AllNaive(Node node){
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        double[][] result = new double[nNodes][];
        
        double[] myInitialData = node.getMyData().clone();
        
        for(int i = 0; i < nNodes; ++i){
            //send the data to node 0 because in our implementation of broadcast
            //this is the node that broadcasts data to others
            if(i != 0){
                if(myIdx == 0){
                    node.receiveAndSet();
                }else if(myIdx == i){
                    node.send(0, myInitialData);
                }
            }
            
            broadcast(node);   //make a broadcast
            result[i] = node.getMyData().clone(); //copy to results what you got
        }
        return result;
    }
    
    
    
    /** \Theta(nNodes) version of @see BasicCommunication#broadcast
     * 
     * @param node 
     */
    public static void broadcastNaive(Node node){

        int nNodes = node.getNumberOfAllNodes();
        int myIdx = node.getMyId();

        if (node.getMyData() != null){
            node.sendMyData(myIdx+1);
        }
        else{
            node.receiveAndSet();
            if(myIdx != nNodes -1){
                node.sendMyData(myIdx+1);
            }
        }
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
    
    
    
    /** \Theta(nNodes) version of @see BasicCommunication#reduce
     * 
     * @param node
     * @param op 
     */
    public static void reduceNaive(Node node, DoubleBinaryOperator op){
        int nNodes = node.getNumberOfAllNodes();
        int myIdx = node.getMyId();
        
        //if I am not the last node I wait for data from the next node
        //and having it, I reduce with my data
        if (myIdx != nNodes-1){
            DataPacket dp = node.receive(); //blocking receive
            double[] extData = dp.getData();
            double[] myData = node.getMyData();
            myData[0] = op.applyAsDouble(myData[0], extData[0]);
            node.setMyData(myData);
        }
        
        //if I am not the first node I send the data I have to the previous node
        if (myIdx != 0){
            node.sendMyData(myIdx - 1); //blocking send
        }
    }
    
    
    
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
                    double[] myData = node.getMyData();
                    double[] myNewData = new double[myData.length/2];
                    double[] dataToSend = 
                            new double[myData.length - myNewData.length];
                    
                    System.arraycopy(myData, 0, myNewData, 0, myNewData.length);
                    System.arraycopy(myData, myNewData.length, dataToSend, 0, 
                            dataToSend.length);
                    
                    node.setMyData(myNewData);
                    node.send(myIdx ^ powerOfTwo, dataToSend);
                }else{
                    node.receiveAndSet();
                }
            }
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
    
    
    
    public static void scanNaive(Node node, DoubleBinaryOperator op)
    {
        int myIdx = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        
        if (myIdx != 0){
            double[] receivedData = node.receive().getData();
            double[] myData = node.getMyData();
            myData[0] = op.applyAsDouble(myData[0], receivedData[0]);
            node.setMyData(myData);
            
        }
        if (myIdx != nNodes-1){
            int destIdx = myIdx+1;
            node.sendMyData(destIdx);
        }
    }
    
    
    /** This is a Wikipedia version of scan
     * (see Wikipedia's article 'prefix sum')
     * 
     * @param node
     * @param op
     */
    public static void scan(Node node, DoubleBinaryOperator op)
    {
        int nNodes = node.getNumberOfAllNodes();
        int myIdx = node.getMyId();
        
        //1) step (it's a reduce but with the result stored in the last node)
        int mask = 0;   //set all d bits of mask to 0
        int powerOfTwo = 1;
        for(int i=0; i<Utils.binlog(nNodes); ++i){
            if ( (myIdx & mask) == mask){  //if lower i bits of idx are 1
                if ( (myIdx & powerOfTwo) == 0 ){
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
        
        //2) step (see Wikipedia prefix-sum)
        for(int i=Utils.binlog(nNodes)-2; i>=0; --i){
            powerOfTwo = (1 << i);
            mask = powerOfTwo - 1;
            if (((myIdx & mask) == mask)){
                if ( (myIdx & powerOfTwo) == powerOfTwo ){
                    int destIdx = myIdx + powerOfTwo;
                    if (destIdx < nNodes-1){
                        node.sendMyData(myIdx+powerOfTwo);
                    }
                }else{
                    int srcIdx = myIdx - powerOfTwo;
                    if (srcIdx >= 0){
                        DataPacket dp = node.receive();
                        double[] extData = dp.getData();
                        double[] myData = node.getMyData();
                        myData[0] = op.applyAsDouble(myData[0], extData[0]);
                        node.setMyData(myData);
                    }
                }
            }
        }
    }
    
    
    /** This is a book version of scan - the one based on all-to-all
     * broadcast communication pattern
     * 
     * @param node
     * @param op
     */
    public static void scanV2(Node node, DoubleBinaryOperator op)
    {
        int myId = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();
        double result = node.getMyData()[0];
        double msg = result;
        
        for (int i = 0; i < Utils.binlog(nNodes); ++i){
            int partnerId = myId ^ (1 << i);
            double receivedData = exchangeWith(node, partnerId, msg);
            msg = op.applyAsDouble(msg, receivedData);
            if (partnerId < myId){
                result = op.applyAsDouble(result, receivedData);
            }
        }
        node.setMyData(new double[] {result});
    }
    
}
