/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package distributedmodel;

import datastructures.Matrix;
import java.lang.reflect.Field;
import java.util.concurrent.CyclicBarrier;
import software.SoftwareDS;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A class representing a distributed system of n Node final objects connected
 * with a network Network (also final). 
 *
 * @author Karol
 */
public class DistributedSystem {
    
    private final Network mNet;
    private final Node[] mNodes;
    private final DSConfig mConfig;
    private final CyclicBarrier mBarrier;
    
    /** Sets up a distributed system without setting data of its nodes. This can
     * be done later by inserting data to chosen nodes individually using
     * getNode(i).setMydata(data)
     * 
     * @param config - configuration of the system
     */
    public DistributedSystem(DSConfig config)
    {
        mConfig = config;
        mNet = new Network(config.getNumberOfNodes(),
                            config.getConnectionDelay(), 
                            config.getUnitTransmissionDelay());
        
        mBarrier = new CyclicBarrier(config.getNumberOfNodes());
        
        //the nodes do not have initial data
        mNodes = new Node[mNet.getNetworkSize()];
        for(int i = 0; i<mNet.getNetworkSize(); ++i){
            mNodes[i] = new Node(mNet.getEndpoint(i), mBarrier);
        }
        
        
    }
    
    /** Sets up a distributed system with setting initial data of the nodes. 
     * 
     * @param config - configuration of the system
     * @param initial_data_states - double[nNodes][dataSize]
     */
    public DistributedSystem(DSConfig config,
            double[][] initial_data_states){
        
        mConfig = config;
        mNet = new Network(config.getNumberOfNodes(),
                            config.getConnectionDelay(), 
                            config.getUnitTransmissionDelay());
        
        mBarrier = new CyclicBarrier(config.getNumberOfNodes());
        mNodes = new Node[mNet.getNetworkSize()];
        for(int i = 0; i<mNet.getNetworkSize(); ++i){
            mNodes[i] = new Node(mNet.getEndpoint(i), mBarrier, initial_data_states[i]);
        }
        
        
    }
    
    public void loadProgramToNodes(SoftwareDS s){
        for(int i = 0; i<mNet.getNetworkSize(); ++i){
            mNodes[i].loadSoftware(s);
        }
    }
    
    public Node getNode(int i){
        return mNodes[i];
    }
    
    public Node[] getNodes(){
        return mNodes;
    }
    
    public DSConfig getConfiguration(){
        return mConfig;
    }
    
    public void runSystem() {
        
        //start all the nodes
        for(int i = 0; i<mNet.getNetworkSize(); ++i){
            mNodes[i].startSoftware();
        }
        
        //return from all the nodes
        for(int i = 0; i<mNet.getNetworkSize(); ++i){
            try {
                mNodes[i].exitSoftware();
            } catch (InterruptedException ex) {
                Logger.getLogger(DistributedSystem.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "\nData in the system of nodes:\n");
        for(Node node : mNodes){
            sb.append(node.toString());
        }
        return sb.toString();
    }
    
    
    public void printDistributedMatrix(Node.MatrixInNode whichMtx)
    {
        int meshSideSize = (int) Math.sqrt(getConfiguration().getNumberOfNodes());
        printDistributedMatrix(whichMtx, meshSideSize, meshSideSize);
    }
    
    public void printDistributedMatrix(Node.MatrixInNode whichMtx, int meshRowCount, int meshColCount)
    {
        
        StringBuilder sb = new StringBuilder(
                String.format("Distributed matrix %s:\n", whichMtx.toString()));
        for(int mR = 0; mR < meshRowCount; ++mR){
            String[][] lines = new String[meshColCount][];
            for(int mC = 0; mC < meshColCount; ++mC){
                int id = Topology.meshCoordsToId(mR, mC, meshColCount);
                try {
                    Field f = getNode(id).getClass().getField(whichMtx.toString());
                    Matrix mtx = (Matrix) f.get(getNode(id));
                    String mtxString = mtx.toString();
                    lines[mC] = mtxString.split("[\\r\\n]+");
                } catch (NoSuchFieldException | SecurityException | 
                        IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(DistributedSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            int submtxRows = lines[0].length;
            for(int sR = 0; sR < submtxRows; ++sR){
                for(int mC = 0; mC < meshColCount; ++mC){
                    sb.append(lines[mC][sR]);
                }
                sb.append("\n");
            }
        }
        System.out.print(sb.toString());
    }
    
    
    public Matrix collectDistributedMatrix(Node.MatrixInNode whichMtx){
        int meshSideSize = (int) Math.sqrt(getConfiguration().getNumberOfNodes());
        return collectDistributedMatrix(whichMtx, meshSideSize, meshSideSize);
    }
    
    public Matrix collectDistributedMatrix(Node.MatrixInNode whichMtx, 
            int meshRowCount, int meshColCount){
        
        try {
            Field f = getNode(0).getClass().getField(whichMtx.toString());
            Matrix mtx = (Matrix) f.get(getNode(0));
            int submatrixNRows = mtx.getNRows();
            int submatrixNCols = mtx.getNCols();
            
            Matrix res = new Matrix(meshRowCount*submatrixNRows,
                                meshColCount*submatrixNCols);
        
            for(int mR = 0; mR < meshRowCount; ++mR){
                for(int r = 0; r < submatrixNRows; ++r){
                    double[] row = new double[res.getNCols()];
                    for(int mC = 0; mC < meshColCount; ++mC){
                        int nodeId = Topology.meshCoordsToId(mR, mC, meshColCount);
                        mtx = (Matrix) f.get(getNode(nodeId));
                        System.arraycopy(mtx.getRow(r), 0,
                            row, mC*submatrixNCols, submatrixNCols);
                    }
                    res.setRow(mR*submatrixNRows+r, row);
                }
            }
            return res;
        } catch (NoSuchFieldException | SecurityException | 
                IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(DistributedSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public void scatterDistributedMatrix(Matrix m, Node.MatrixInNode whichMtx)
    {
        int meshSideSize = (int) Math.sqrt(getConfiguration().getNumberOfNodes());
        scatterDistributedMatrix(m, whichMtx, meshSideSize, meshSideSize);
    }
    
    public void scatterDistributedMatrix(Matrix m, Node.MatrixInNode whichMtx, 
            int meshRowCount, int meshColCount)
    {
        try {
            Field f = getNode(0).getClass().getField(whichMtx.toString());
            
            int submatrixNRows = m.getNRows() / meshRowCount;
            int submatrixNCols = m.getNCols() / meshColCount;
        
            for (int n = 0; n < getConfiguration().getNumberOfNodes(); ++n)
            {
                int nodeR = Topology.meshRowOfId(n, meshColCount);
                int nodeC = Topology.meshColOfId(n, meshColCount);
                
                Matrix subMtx = m.getSubmatrix(nodeR*submatrixNRows,
                        nodeC*submatrixNCols, submatrixNRows, submatrixNCols);
                
                f.set(getNode(n), subMtx);
            }
            
        } catch (NoSuchFieldException | SecurityException | 
                IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(DistributedSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
