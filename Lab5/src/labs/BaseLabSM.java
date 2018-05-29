/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package labs;

import java.util.Arrays;
import java.util.function.Consumer;
import org.jfree.data.xy.XYSeries;
import software.SoftwareSM;

/** This is a base class for all lab classes that deal only with testing
 * algorithms for systems with shared memory.
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public abstract class BaseLabSM extends BaseLab{
    
    /** Measures time (in seconds) needed to run a given software on a given
     * shared data
     * 
     * @param data
     * @param soft
     * @return - The time (in seconds) of executing soft
     */
    protected double singleMeasurement(double[] data, 
            SoftwareSM soft){
        
        long startTime = System.currentTimeMillis();
        soft.instructions(data);
        long stopTime = System.currentTimeMillis();
        
        double elapsedTime = (double)(stopTime - startTime) / 1000;       
        return elapsedTime;
    }
    
    
    protected double singleTestRun(int nElements, SoftwareSM soft, 
        boolean validate, boolean printProcessingTime, boolean printStatus)
    {
        double[] data = new double[nElements];
        
        initSharedMemory(data);
        
        if(printStatus){
            printSharedMemoryBefore(data);
        }
        
        double exTime = singleMeasurement(data, soft);
        
        if(printProcessingTime){
            System.out.printf("Processing took: %.2f sec.%n", exTime);
        }
        
        if(printStatus){
            printSharedMemoryAfter(data);
        }
        
        if(validate){
            validateSharedMemory(data);
        }
        
        return exTime;
    }
    
    
    protected double singleTestRun(int nElements, SoftwareSM soft)
    {
        return singleTestRun(nElements, soft, false, false, false);
    }
    
    
    protected double singleTestRun(int nElements,
        SoftwareSM soft,
        Consumer<double[]> sharedMemoryInitCode,
        Consumer<double[]> printSharedMemoryBefore,
        Consumer<double[]> printSharedMemoryAfter,
        Consumer<double[]> validationCode,
        boolean printProcessingTime)
    {
        double[] data = new double[nElements];
        sharedMemoryInitCode.accept(data);
        
        printSharedMemoryBefore.accept(data);
        
        double exTime = singleMeasurement(data, soft);
        
        if(printProcessingTime){
            System.out.printf("Processing took: %.2f sec.%n", exTime);
        }
        
        printSharedMemoryAfter.accept(data);
        
        validationCode.accept(data);
        
        return exTime;
    }
    
    
    
    
    protected XYSeries seriesOfRuns(String expName, 
            SoftwareSM soft, boolean validate, boolean printProcessingTime,
            boolean printStatus)
    {
        
        int[] nElements = new int[] {2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
        double[] exTimes = new double[nElements.length];
             
        for(int i = 0; i < nElements.length; ++i){
            exTimes[i] = singleTestRun(nElements[i], soft,
                    validate, printProcessingTime, printStatus);
        }
        
        XYSeries series = new XYSeries(expName);
        for(int i = 0; i< nElements.length; ++i){
            series.add(nElements[i], exTimes[i]);
        }
        
        return series;
    }
    
    
    protected XYSeries seriesOfRuns(String expName, 
            SoftwareSM soft, boolean printStatus)
    {
        return seriesOfRuns(expName, soft, true, false, printStatus);
    }
    

    
    /** This method must be implemented in subclasses to initialize the shared
     * memory with appropriate data.
     * 
     * @param data - already initialized array but not filled with any values.
     */
    protected abstract void initSharedMemory(double[] data);
    
    
    
    protected void printSharedMemoryBefore(double[] data){
        System.out.printf("BEFORE: %s%n", Arrays.toString(data));
    }
    
    protected void printSharedMemoryAfter(double[] data){
        System.out.printf("AFTER: %s%n", Arrays.toString(data));
    }
    
    protected void validateSharedMemory(double[] data) {
        System.out.println("No validation code supplemented.");
    }
    
}
