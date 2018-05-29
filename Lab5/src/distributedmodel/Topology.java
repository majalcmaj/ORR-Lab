/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package distributedmodel;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Topology {
    
    /** Returns id of a Node which is placed at (row, col) coordinate in a mash
     * with nColumns columns.
     * 
     * @param row       - row coordinate of a node
     * @param col       - column coordinate of a node
     * @param nColumns  - number of columns in a given mesh
     * @return          - id of the node at (row, col)
     */
    public static int meshCoordsToId(int row, int col, int nColumns){
        return row*nColumns+col;
    }
    
    /** Returns a row number (coordinate) in which Node with nodeId is placed in
     * a mesh with nColumns columns.
     * 
     * @param nodeId    - id of a Node
     * @param nColumns  - number of columns in a given mesh
     * @return          - row number (row coordinate)
     */
    public static int meshRowOfId(int nodeId, int nColumns){
        return nodeId/nColumns;
    }
    
    /** Returns a column number (coordinate) in which Node with nodeId is placed
     * in a mesh with nColumns columns.
     * 
     * @param nodeId    - id of a Node
     * @param nColumns  - number of columns in a given mesh
     * @return          - column number (column coordinate)
     */
    public static int meshColOfId(int nodeId, int nColumns){
        return nodeId - meshRowOfId(nodeId, nColumns)*nColumns;
    }
    
}
