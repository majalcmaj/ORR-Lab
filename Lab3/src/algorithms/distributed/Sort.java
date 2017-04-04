/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package algorithms.distributed;

import algorithms.Utils;
import distributedmodel.Node;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Sort {
    
    public enum Half{
        LOWER,
        UPPER
    }
    
    /** Bitonic sort of the first elements of each node data.
     * 
     * @param node
     */
    public static void bitonicSortAsc(Node node){
        int myId = node.getMyId();
        double myData = node.getMyData()[0];
        int nNodes = node.getNumberOfAllNodes();
        int d = Utils.binlog(nNodes);
        
        for(int i = 0; i<d; ++i){
            for(int j = i; j >= 0; --j){
                int otherId = myId^(1<<j);
                double otherData = BasicCommunication.exchangeWith(node,
                        otherId, myData);
                
                //if in myId (i+1)-th bit is not equal to j-th bit
                if( ((myId&(1<<(i+1))) != 0) != ((myId&(1<<j)) != 0) ){
                    myData = Math.max(myData, otherData);
                }else{
                    myData = Math.min(myData, otherData);
                }
            }
        }
        node.setMyData(new double[]{myData});
    }
    
  
    
    
    /** Bitonic sort, when each node has n/nNodes part of the data. It is
     * important that each node has the same amount of data. Sorts in decreasing
     * order.
     * 
     * @param node
     */
    public static void bitonicSortGeneralizedDesc(Node node){

        int myId = node.getMyId();
        double[] myData = node.getMyData();

        Utils.reverseSortArray(myData);

        int nNodes = node.getNumberOfAllNodes();
        int d = Utils.binlog(nNodes);
        for(int i = 0; i<d; ++i){
            for(int j = i; j >= 0; --j){
                int otherId = myId^(1<<j);
                //if in myId (i+1)-th bit is not equal to j-th bit
                if( ((myId&(1<<(i+1))) != 0) != ((myId&(1<<j)) != 0) ){
                    compareSplitDescending(node, otherId, Half.UPPER);
                }else{
                    compareSplitDescending(node, otherId, Half.LOWER);
                }
            }
        }
        node.setMyData(myData);
        
    }


    /** Exchanges data (has to be sorted in descending order!) between two nodes
     * that called the function with IDs of each other, then each of them merges
     * the data and takes a half of it, lower or upper depending of Half h
     * argument.
     * 
     * Example: 
     *          Before: node.myData = [10 6 4 1], otherId myData = [9 5 4 3], h = LOWER
     *          After:  node.myData = [4 4 3 1]
     * 
     *          Before: node.myData = [10 6 4 1], otherId myData = [9 5 4 3], h = UPPER
     *          After:  node.myData = [10 9 6 5]
     * 
     * 
     * @param node
     * @param otherId
     * @param h
     */
    public static void compareSplitDescending(Node node, int otherId, Half h){
        // you can use Utils.mergeDescending helper function
        double[] data;
        double[] myData = node.getMyData();
        if(h == Half.LOWER) {
            data = node.receive().getData();
            node.send(otherId, myData);
        }else {
            node.send(otherId, myData);
            data = node.receive().getData();
        }
        double[] newData = Utils.mergeDescending(data, myData);
        if(h == Half.LOWER)
            System.arraycopy(newData, 0, myData, 0, data.length);
        else
            System.arraycopy(newData, myData.length, myData, 0, myData.length);
    }
    
}
