/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.distributed;

import algorithms.Utils;
import distributedmodel.DataPacket;
import distributedmodel.Node;
import java.util.function.DoubleBinaryOperator;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class BasicCommunication {
    
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
    
    
    public static void scan(Node node, DoubleBinaryOperator op)
    {
        
        //TODO
        
    }
    
}
