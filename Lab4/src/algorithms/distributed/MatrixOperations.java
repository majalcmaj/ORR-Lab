/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package algorithms.distributed;

import datastructures.Matrix;
import distributedmodel.DataPacket;
import distributedmodel.Node;
import distributedmodel.Topology;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class MatrixOperations {
    
    
    public static void multiplySquareMtxSimple(Node node)
    {
        int myId = node.getMyId();
        int meshSideSize = (int) Math.sqrt(node.getNumberOfAllNodes());
        int myRow = Topology.meshRowOfId(myId, meshSideSize);
        int myCol = Topology.meshColOfId(myId, meshSideSize);
        
        // TODO
        // ...
        
    }
}
