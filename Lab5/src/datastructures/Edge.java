/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package datastructures;

/** Simplest directed weighted edge possible 
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public final class Edge{
    public final int from;
    public final int to;
    public final double weight;
    
    public Edge(int f, int t, double w){
        from = f; to = t; weight = w;
    }
    
    @Override
    public String toString(){
        return String.format("%2d --(%6.4f)--> %2d", from, weight, to);
    }
    
    public double[] serialize(){
        return new double[] { from, to, weight };
    }
    
    public static Edge deserialize(double[] fromData){
        return new Edge((int) fromData[0], (int) fromData[1], fromData[2]);
    }
}
