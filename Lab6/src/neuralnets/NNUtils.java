/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import java.util.Random;

/** A class with variuos general pieces of code that may be helpful for 
 * a neural netork code.
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class NNUtils
{
    
    /** Generates size random numbers from a normal distribution with std
     * standard variation, using specified seed
     * 
     * @param size      - number of elements to generate
     * @param std       - standard deviation
     * @param seed      - seed to initialize rng
     * @return
     */
    public static double[] generateNormalRandomData(int size, double std, long seed)
    {
        Random r = new Random(seed);
        return generateNormalRandomData(size, std, r);
    }
    
    /** Generates size random numbers from a normal distribution with std
     * standard variation, using specified seed
     * 
     * @param size      - number of elements to generate
     * @param std       - standard deviation
     * @return 
     */
    public static double[] generateNormalRandomData(int size, double std)
    {
        Random r = new Random();
        return generateNormalRandomData(size, std, r);
    }
    
    /** Generates size random numbers from a normal distribution with std
     * standard variation, using given random number generator
     * 
     * @param size      - number of elements to generate
     * @param std       - standard deviation
     * @param r         - java.util.Random object
     * @return 
     */
    public static double[] generateNormalRandomData(int size, double std, Random r){
        double[] data = new double[size];
        for(int i = 0; i<size; ++i){
            data[i] = std * r.nextGaussian();
        }
        return data;
    }
    
    
    
    public static double[] generateSequenceFixedLength(double min, double max, int length){
        double step = (max-min)/(length-1);
        
        double[] res = new double[length];
        for (int i = 0; i<length; ++i){
            res[i] = min + i*step;
        }
        
        return res;
    }
    
    public static double[] generateSequenceFixedStep(double min, double max, double step){
        int length = (int) Math.floor((max-min)/step) + 1;
        
        double[] res = new double[length];
        for (int i = 0; i<length; ++i){
            res[i] = min + i*step;
        }
        
        return res;
    }
    
}
