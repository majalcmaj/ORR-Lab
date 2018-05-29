/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */

package neuralnets;

import java.util.function.DoubleUnaryOperator;

/** A class representing neuron's activation function with it's gradient.
 * Obtaining objects of this class is possible through static get.. methods,
 * constructor is private.
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class ActivationUnit {
    
    private final DoubleUnaryOperator _activation;
    private final DoubleUnaryOperator _gradient;
    
    public DoubleUnaryOperator getActivationFun(){
        return _activation;
    }
    public DoubleUnaryOperator getGradientFun(){
        return _gradient;
    }
    
    private ActivationUnit( DoubleUnaryOperator activ,
                            DoubleUnaryOperator grad)
    {
        _activation = activ;
        _gradient = grad;
    }
    
    public static ActivationUnit getLogisticUnit(){
        DoubleUnaryOperator logistic = (double in) -> {
            return 1.0/(1.0+(Math.expm1(-in) + 1.0));
        };
        DoubleUnaryOperator gradLogistic = (double out) -> {
            return out*(1.0-out);
        };
        ActivationUnit unit = new ActivationUnit(logistic, gradLogistic);
        return unit;
    }
    
    public static ActivationUnit getTanhUnit()
    {
        DoubleUnaryOperator tanh = (double in) -> {
            double aux = Math.expm1(-in)+1.0;
            return (1.0-aux)/(1.0+aux);
        };
        DoubleUnaryOperator gradTanh = (double out) -> {
            return 0.5*(1.0-out*out);
        };
        ActivationUnit unit = new ActivationUnit(tanh, gradTanh);
        return unit;
    }
    
    public static ActivationUnit getPurelinUnit(){
        DoubleUnaryOperator purelin = DoubleUnaryOperator.identity();
        DoubleUnaryOperator gradPurelin = (double in) -> {
            return 1.0;
        };
        ActivationUnit unit = new ActivationUnit(purelin, gradPurelin);
        return unit;
    }
    
    public static ActivationUnit getHardlimUnit(){
        DoubleUnaryOperator hardlim = (double in) -> {
            return (in > 0)? 1.0 : 0.0;
        };
        DoubleUnaryOperator gradHardlim = (double in) -> {
            return 0.0;
        };
        ActivationUnit unit = new ActivationUnit(hardlim, gradHardlim);
        return unit;
    }
    
    public static ActivationUnit getHardlimsUnit(){
        DoubleUnaryOperator hardlims = (double in) -> {
            return (in > 0)? 1.0 : -1.0;
        };
        DoubleUnaryOperator gradHardlims = (double in) -> {
            return 0.0;
        };
        ActivationUnit unit = new ActivationUnit(hardlims, gradHardlims);
        return unit;
    }
    
}
