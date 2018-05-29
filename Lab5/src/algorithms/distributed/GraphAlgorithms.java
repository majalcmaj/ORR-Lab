/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package algorithms.distributed;

import algorithms.Utils;
import datastructures.Edge;
import distributedmodel.DataPacket;
import distributedmodel.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import software.SoftwareDS;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class GraphAlgorithms {
    
    /** Assumes that node 0 contains the whole graph adjacency matrix in its 
     * field A. All the work is done by this single node.
     * 
     * @param node
     * @param printResult 
     */
    public static void findMSTPrimSerial(Node node, boolean printResult)
    {
        if (node.getMyId() == 0){
            
            int nVertices = node.A.getNCols();
            
            List<Edge> mst = new ArrayList<>(nVertices-1);
            double mstTotalWeight = 0;
            Map<Integer, Edge> minDistToMST = new HashMap<>(nVertices-1);
            
            int r = 0;      // root of the MST; begin from vertex 0
            
            for(int i = 0; i<node.A.getNCols(); ++i){
                if( i != r){
                    minDistToMST.put(i, new Edge(r, i, node.A.getElem(r, i)));
                }
            }
            
            while (!minDistToMST.isEmpty()){
                
                //determine the next vertex to add
                Edge bestEdge = new Edge(-1, -1, Double.POSITIVE_INFINITY);
                for(Integer k : minDistToMST.keySet()){
                    if (minDistToMST.get(k).weight < bestEdge.weight){
                        bestEdge = minDistToMST.get(k);
                    }
                    simulationWait();
                }
                
                //add to mst and remove from minDistToMST
                int idxVertexToAdd = bestEdge.to;
                minDistToMST.remove(idxVertexToAdd);
                mstTotalWeight += bestEdge.weight;
                mst.add(bestEdge);
                
                //update minDistToMST
                for(Integer k : minDistToMST.keySet()){
                    if (node.A.getElem(idxVertexToAdd, k) < minDistToMST.get(k).weight){
                        minDistToMST.replace(k, new Edge(idxVertexToAdd, k, 
                                node.A.getElem(idxVertexToAdd, k)));
                    }
                    simulationWait();
                }
            }
            
            //for validation purposes:
            node.setMyData(new double[] {mstTotalWeight});
            
            
            if(printResult){
                System.out.println("Resulting MST:");
                mst.stream().forEach((e) -> {
                    System.out.println("\t"+e.toString());
                });
                System.out.format("Total MST weight = %4.2f\n", mstTotalWeight);
            }
            
        }
    }
    
    
    /** Parallel version of Prim algorithm of finding MST in a graph. Assumes
     * the adjacency matrix describing a graph is distributed columnwise among
     * nodes in the distributed system.
     * 
     * @param node
     * @param printResult 
     */
    public static void findMSTPrim(Node node, boolean printResult){
            
        int myId = node.getMyId();
        int nNodes = node.getNumberOfAllNodes();       
        int nVertices = node.A.getNRows();
        int nMyVertices = node.A.getNCols();
        int myColStart = getMyColStart(node);
        double mstTotalWeight = 0;
        List<Integer> visited = new ArrayList<>();
        visited.add(0);

        List<Integer> locals = new ArrayList<>();
        for(int i = 0 ; i < nMyVertices ;i++)
            if(i + myColStart != 0)
                locals.add(i);

        while(visited.size() != nVertices)
        {
            int minIdx = -1;
            int otherIdx = -1;
            double minValue = Double.POSITIVE_INFINITY;
            for(int local : locals)
            {
                for(int visitedIndex : visited) {
                    if (myColStart + local != visitedIndex) {
                        double value = node.A.getElem(visitedIndex, local);
                        if (minValue > value) {
                            minValue = value;
                            minIdx = local;
                            otherIdx = visitedIndex;
                        }
                    }
                }
            }
            Edge toSend;
            if(minIdx != -1 && otherIdx != -1)
                toSend = new Edge(myColStart + minIdx, otherIdx, node.A.getElem(otherIdx, minIdx));
            else
                toSend = new Edge(-1, -1, Double.POSITIVE_INFINITY);
            double[] serializedEdge = BasicCommunication.reduce(node, toSend.serialize(), (double[] a, double[] b) -> {
                if(a[2] < b[2])
                    return a;
                else
                    return b;
            });
            serializedEdge = BasicCommunication.broadcast(node, serializedEdge);
            Edge e = Edge.deserialize(serializedEdge);
            int rmIdx = e.from - myColStart;
            if(rmIdx < nMyVertices && rmIdx >= 0)
                locals.remove((Object)rmIdx);
            visited.add(e.from);
            mstTotalWeight += e.weight;
        }

        //for validation purposes:
        node.setMyData(new double[] {mstTotalWeight});
        
        if(printResult && myId == 0){
            System.out.println("Resulting MST:");
            visited.stream().forEach((e) -> {
                System.out.println("\t"+e.toString());
            });
            System.out.format("Total MST weight = %4.2f\n", mstTotalWeight);
        }  
    }
    
    private static int getMyColStart(Node n)
    {
        for(int i = 0 ; i < n.A.getNRows() ; i++)
        {
            if(n.A.getElem(i, 0) == 0)
            {
                return i;
            }
        }
        throw new RuntimeException("Node idx not found");
    }
    private static void simulationWait(){
        try {
            Thread.sleep(SoftwareDS.DEFAULT_SIMULATION_UNIT_PROCESSING_TIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(GraphAlgorithms.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
