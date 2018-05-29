/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package distributedmodel;

import datastructures.Matrix;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import software.SoftwareDS;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */

public class Node implements Runnable{
    private final NetworkEndpoint mNetEndpoint;//socket associated with the node
    private final int mId;                     //unique ID of the node
    private final Thread mThread;              //'process' associated with the node
    private final CyclicBarrier mDSBarrier;     //systemwide barrier to synchronize nodes
    private double[] mData;                    //data of the node
    private SoftwareDS mSoftware;                //software in the node
    private boolean mLogCommunication = false;                 //if communication should be logged to console
    
    public Matrix A;        // for lab04 - part of a distributed matrix A
    public Matrix B;        // for lab04 - part of a distributed matrix B
    public Matrix C;        // for lab04 - part of A*B
    
    public static enum MatrixInNode{ A, B, C }     // for lab04
    
    Node(NetworkEndpoint netEndpoint, double[] initialData){
        this(netEndpoint);
        if (initialData != null){
            mData = initialData.clone();
        }
    }
    
    Node(NetworkEndpoint netEndpoint){
        mNetEndpoint = netEndpoint;
        mId = netEndpoint.getId();
        mThread = new Thread(this);
        mDSBarrier = null;
    }
    
    Node(NetworkEndpoint netEndpoint, CyclicBarrier dsBarrier){
        mNetEndpoint = netEndpoint;
        mId = netEndpoint.getId();
        mThread = new Thread(this);
        mDSBarrier = dsBarrier;
    }
    
    Node(NetworkEndpoint netEndpoint, CyclicBarrier dsBarrier, double[] initialData){
        this(netEndpoint, dsBarrier);
        if (initialData != null){
            mData = initialData.clone();
        }
    }
    
    
    public double[] getMyData(){
        return mData;
    }
    
    public void setMyData(double[] data){
        mData = data;
    }
    
    public boolean getLogCommunication(){
        return mLogCommunication;
    }
    
    public void setLogCommunication(boolean logCommunication){
        mLogCommunication = logCommunication;
    }
    
    public void send(int destinationId, double[] data){
        if (data!=null){
            if (mLogCommunication){
                System.out.printf("Node %d tries to send data to node %d%n", 
                        mId, destinationId);
            }
            try {
                mNetEndpoint.send(destinationId, data);
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            throw(new RuntimeException(String.format(
                    "Node %d tries to send nothing (data is null).%n", mId)));
        }
    }
    
    
    public void sendMyData(int destinationId){
        send(destinationId, mData);
    }
    
    public void sendForward(DataPacket dp){
        try {
            mNetEndpoint.send(dp);
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DataPacket receive(){
        DataPacket dp = null;
        if (mLogCommunication){
                System.out.printf("Node %d waits to receive data%n", mId);
            }
        try {
            dp = mNetEndpoint.receive();
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (mLogCommunication){
            if (dp != null){
                System.out.printf("Node %d received data packet: %s%n", mId, dp.toString());
            }else{
                System.out.printf("Node %d failed to receive data!%n", mId);
            }
        }
        return dp;
    }
    
    public void receiveAndSet(){
            setMyData(receive().getData());
    }
    
    public int getNumberOfAllNodes(){
        return mNetEndpoint.getNumberOfChannels();
    }
            
    public int getMyId(){
        return mId;
    }

    public void synchronizeDS(){
        try {
            mDSBarrier.await();
        } catch (InterruptedException | BrokenBarrierException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    void loadSoftware(SoftwareDS s){
        mSoftware = s;
    }
    
    void startSoftware(){
        mThread.start();
    }
    
    void exitSoftware() throws InterruptedException{
        mThread.join();
    }
    
    @Override
    public void run() {
        if (mSoftware != null){
            mSoftware.instructions(this);
        }else{
            throw(new RuntimeException(String.format(
                    "Node %d does not have any program to execute!", mId)));
        }
    }
    
    @Override
    public String toString(){
        if (mData!=null){
            return String.format("Node %d has data: %s%n", mId, Arrays.toString(mData));
        }
        else{
            return String.format("Node %d does not have any data.%n", mId);
        }
    }
    
//    public void save(double[] data, int offset){
//        System.arraycopy(data, 0, mMemory, offset, data.length);
//    }
//    
//    public void save(double[] data){
//        save(data, 0);
//    }
//    
//    public double[] read(int length, int offset){
//        double[] res = new double[length];
//        System.arraycopy(mMemory, offset, res, 0, length);
//        return res;
//    }
//    
//    public double[] read(int length){
//        return read(length, 0);
//    }
}
