/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package datastructures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import software.SoftwareDS;

/**
 *
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public final class Matrix {
    final private double[][] mData;
    final private int mNRows;
    final private int mNCols;
    
    
    
    public Matrix(int nRows, int nCols, double[] dataRowwise){
        this(nRows, nCols, dataRowwise, 0);
    }
    
    public Matrix(int nRows, int nCols, double[] dataRowwise, int offset)
    {
        if(dataRowwise.length - offset < nRows*nCols){
            throw new RuntimeException(
                    "Matrix of the specified size requires more data");
        }
        mNRows = nRows;
        mNCols = nCols;
        mData = new double[nRows][nCols];
        int i = offset;
        for(int row = 0; row < nRows; ++row){
            for(int col = 0; col < nCols; ++col){
                mData[row][col] = dataRowwise[i++];
            }
        }
    }
    
    
    /** Creates nRows by nCols zero matrix 
     * 
     * @param nRows
     * @param nCols 
     */
    public Matrix(int nRows, int nCols)
    {
        mNRows = nRows;
        mNCols = nCols;
        mData = new double[nRows][nCols];
        for(int row = 0; row < nRows; ++row){
            for(int col = 0; col < nCols; ++col){
                mData[row][col] = 0;
            }
        }
    }
    
    /** Copies the 2d array of doubles and wraps it into a matrix object.
     * 
     * @param data 
     */
    public Matrix(double[][] data){
        this(data, true);
    }
    
    /** Wraps the 2d-array of doubles into a matrix object, optionally copying
     * them.
     * 
     * @param data 
     * @param copyFlag - if true the data is copied
     */
    public Matrix(double[][] data, boolean copyFlag){
        mNRows = data.length;
        mNCols = data[0].length;
        if(!copyFlag){
            mData = data;
        }else{
            mData = new double[mNRows][mNCols];
            for(int i = 0; i < mNRows; ++i){
                System.arraycopy(data[i], 0, mData[i], 0, mNCols);
            }
        }
    }
    
    
    /** Copy constructor
     * 
     * @param other 
     */
    public Matrix(Matrix other){
        this(other.mData);
    }
    
    
    public int getNRows(){
        return mNRows;
    }
    
    public int getNCols(){
        return mNCols;
    }
    
    /** 'Serializes' the matrix data in a rowwise order 
     * 
     * @return 
     */
    public double[] getMatrixRowwise()
    {
        double[] res = new double[mNRows*mNCols];
        int i = 0;
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                res[i++] = mData[row][col];
            }
        }
        return res;
    }
    
    /** Returns (copy of) submatrix of the matrix (this object), indicated by the position
     * of the upper left element and size of the submatrix.
     * 
     * @param upLeftRow - can be between 0 and mNRows-1
     * @param upLeftCol - can be between 0 and mNCols-1
     * @param nRows
     * @param nCols
     * @return 
     */
    public Matrix getSubmatrix(int upLeftRow, int upLeftCol, 
            int nRows, int nCols)
    {
        if (upLeftRow < 0 || upLeftRow >= mNRows || 
                upLeftCol < 0 || upLeftCol >= mNCols){
            throw new RuntimeException("Out-of-bounds upper-left corner of a submatrix.");
        }
        int bottomRightRow = upLeftRow + nRows - 1;
        int bottomRightCol = upLeftCol + nCols - 1;
        if (bottomRightRow < upLeftRow || bottomRightRow >= mNRows ||
                bottomRightCol < upLeftCol || bottomRightCol >= mNCols) {
            throw new RuntimeException("Out-of-bounds bottom-right corner of a submatrix.");
        }
        double[][] data = new double[nRows][nCols];
        for (int i = 0; i<nRows; ++i){
            System.arraycopy(mData[upLeftRow+i], upLeftCol, data[i], 0, nCols);
        }
        return new Matrix(data);
    }
    
    
    /** Sets a submatrix data into this matrix in the place defined by upper
     * left corner placement 
     * 
     * @param submatrix
     * @param upLeftRow - can be between 0 and this.mNRows-1
     * @param upLeftCol  - can be between 0 and this.mNCols-1
     */
    public void setSubmatrix(Matrix submatrix, int upLeftRow, int upLeftCol)
    {
        int nRows = submatrix.getNRows();
        int nCols = submatrix.getNCols();
        
        
        if (upLeftRow < 0 || upLeftRow >= mNRows || 
                upLeftCol < 0 || upLeftCol >= mNCols){
            throw new RuntimeException("Out-of-bounds upper-left corner of a submatrix.");
        }
        int bottomRightRow = upLeftRow + nRows - 1;
        int bottomRightCol = upLeftCol + nCols - 1;
        if (bottomRightRow < upLeftRow || bottomRightRow >= mNRows ||
                bottomRightCol < upLeftCol || bottomRightCol >= mNCols) {
            throw new RuntimeException("Out-of-bounds bottom-right corner of a submatrix.");
        }
        
        for (int i = 0; i<submatrix.getNRows(); ++i){
            System.arraycopy(submatrix.mData[i], 0, mData[upLeftRow+i], upLeftCol, nCols);
        }
    }
    
    
    
    /** Returns a copy of the specified row
     * 
     * @param iRow - the row number
     * @return 
     */
    public double[] getRow(int iRow){
        if (iRow < 0 || iRow>=mNRows){
            return null;
        }
        double[] res = new double[mNCols];
        System.arraycopy(mData[iRow], 0, res, 0, mNCols);
        return res;
    }
    
    /** Sets the row iRow of the matrix to a copy of values
     * 
     * @param iRow
     * @param values 
     */
    public void setRow(int iRow, double[] values){
        if (iRow < 0 || iRow>=mNRows || values.length != mNCols){
            return;
        }
        System.arraycopy(values, 0, mData[iRow], 0, mNCols);
    }
    
    /** Returns a copy of iCol column
     * 
     * @param iCol
     * @return 
     */
    public double[] getCol(int iCol){
        if (iCol < 0 || iCol>=mNCols){
            return null;
        }
        double[] res = new double[mNRows];
        for(int row = 0; row < mNRows; ++row){
            res[row] = mData[row][iCol];
        }
        return res;
    }
    
    /** Sets the iCol column values to copies of values.
     * 
     * @param iCol
     * @param values 
     */
    public void setCol(int iCol, double[] values){
        if (iCol < 0 || iCol>=mNCols || values.length != mNRows){
            return;
        }
        for(int row = 0; row < mNRows; ++row){
            mData[row][iCol] = values[row];
        }
    }
    
    public double getElem(int row, int col){
        return mData[row][col];
    }
    
    public void setElem(int iRow, int iCol, double value){
        if (iRow < 0 || iRow >= mNRows || iCol < 0 || iCol >= mNCols){
            return;
        }
        mData[iRow][iCol] = value;
    }
    
    /** Writes the whole matrix rowwise to a given memory array starting from
     * the given offset.
     * 
     * @param memory
     * @param offset 
     */
    public void saveMatrixRowwise(double[] memory, int offset){
        if(memory.length - offset < mNRows*mNCols){
            throw new RuntimeException(
                    "The matrix does not fit into the memory"
                            + " at the specified location.");
        }
        int i = offset;
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                memory[i++] = mData[row][col];
            }
        }
    }
    
    public Matrix times(Matrix other){
        if(this.mNCols != other.mNRows){
            throw new RuntimeException("Matrices inner sizes mismatch");
        }
        Matrix res = new Matrix(mNRows, other.mNCols);
        for(int row = 0; row < res.mNRows; ++row){
            for(int col = 0; col < res.mNCols; ++col){
                double tmp = 0;
                for(int k = 0; k < this.mNCols; ++k){
                    tmp += mData[row][k] * other.mData[k][col];
                }
                res.mData[row][col] = tmp;
            }
        }
        return res;
    }
    
    
    public Matrix timesLagged(Matrix other){
        if(this.mNCols != other.mNRows){
            throw new RuntimeException("Matrices inner sizes mismatch");
        }
        Matrix res = new Matrix(mNRows, other.mNCols);
        for(int row = 0; row < res.mNRows; ++row){
            for(int col = 0; col < res.mNCols; ++col){
                double tmp = 0;
                for(int k = 0; k < this.mNCols; ++k){
                    tmp += mData[row][k] * other.mData[k][col];
                    try {
                        Thread.sleep(SoftwareDS.DEFAULT_SIMULATION_UNIT_PROCESSING_TIME);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Matrix.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                res.mData[row][col] = tmp;
            }
        }
        return res;
    }
    
    
    
    public Matrix times(double scalar)
    {
        return this.applyFunctionElementwise((double e) -> {return e*scalar;});
    }
    
    public Matrix timesInPlace(double scalar)
    {
        return this.applyFunctionElementwiseInPlace((double e) -> {return e*scalar;});
    }
    
    public Matrix divide(double scalar)
    {
        return this.applyFunctionElementwise((double e) -> {return e/scalar;});
    }
    
    public Matrix divideInPlace(double scalar)
    {
        return this.applyFunctionElementwiseInPlace((double e) -> {return e/scalar;});
    }
    
    public Matrix add(double scalar)
    {
        return this.applyFunctionElementwise((double e) -> {return e+scalar;});
    }
    
    public Matrix addInPlace(double scalar)
    {
        return this.applyFunctionElementwiseInPlace((double e) -> {return e+scalar;});
    }
    
    public Matrix subtract(double scalar)
    {
        return this.applyFunctionElementwise((double e) -> {return e-scalar;});
    }
    
    public Matrix subtractInPlace(double scalar)
    {
        return this.applyFunctionElementwiseInPlace((double e) -> {return e-scalar;});
    }
    
    
    /** This is equivalent to this.times(other.transpose()) but avoids
     * unnecessary alocations.
     * 
     * @param other
     * @return 
     */
    public Matrix timesByTranspose(Matrix other){
        if(this.mNCols != other.mNCols){
            throw new RuntimeException(
                "Both matrices must have the same number of columns");
        }
        Matrix res = new Matrix(mNRows, other.mNRows);
        for(int row = 0; row < res.mNRows; ++row){
            for(int col = 0; col < res.mNCols; ++col){
                double tmp = 0;
                for(int k = 0; k < this.mNCols; ++k){
                    tmp += mData[row][k] * other.mData[col][k];
                }
                res.mData[row][col] = tmp;
            }
        }
        return res;
    }
    
    
    public Matrix timesElementByElement(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            throw new RuntimeException("Matrices sizes mismatch");
        }
        Matrix res = new Matrix(mNRows, mNCols);
        for(int row = 0; row < res.mNRows; ++row){
            for(int col = 0; col < res.mNCols; ++col){
                res.mData[row][col] = mData[row][col] * other.mData[row][col];
            }
        }
        return res;
    }
    
    public Matrix timesElementByElementInPlace(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            throw new RuntimeException("Matrices sizes mismatch");
        }
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                mData[row][col] *= other.mData[row][col];
            }
        }
        return this;
    }
    
    
    public Matrix add(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            throw new RuntimeException("Matrices sizes mismatch");
        }
        Matrix res = new Matrix(mNRows, mNCols);
        for(int row = 0; row < res.mNRows; ++row){
            for(int col = 0; col < res.mNCols; ++col){
                res.mData[row][col] = mData[row][col] + other.mData[row][col];
            }
        }
        return res;
    }
    
    
    public Matrix subtract(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            throw new RuntimeException("Matrices sizes mismatch");
        }
        Matrix res = new Matrix(mNRows, mNCols);
        for(int row = 0; row < res.mNRows; ++row){
            for(int col = 0; col < res.mNCols; ++col){
                res.mData[row][col] = mData[row][col] - other.mData[row][col];
            }
        }
        return res;
    }
    
    public Matrix subtractInPlace(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            throw new RuntimeException("Matrices sizes mismatch");
        }
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                mData[row][col] -= other.mData[row][col];
            }
        }
        return this;
    }
    
    public Matrix addInPlace(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            throw new RuntimeException(
                String.format("Matrices sizes mismatch: [%d, %d] vs [%d, %d]",
                        this.mNRows, this.mNCols, other.mNRows, other.mNCols));
        }
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                mData[row][col] += other.mData[row][col];
            }
        }
        return this;
    }
    
    /** Returned matrix is a copy of original matrix - transposed.
     * 
     * @return 
     */
    public Matrix transpose(){
        double[][] data = new double[this.mNCols][this.mNRows];
        for(int r = 0; r < mNRows; ++r){
            for(int c=0; c < mNCols; ++c){
                data[c][r] = this.mData[r][c];
            }
        }
        return new Matrix(data, false);
    }
    
    
    
    public Matrix applyFunctionElementwiseInPlace(DoubleUnaryOperator func)
    {
        for(int r = 0; r < mNRows; ++r){
            for(int c=0; c < mNCols; ++c){
                this.mData[r][c] = func.applyAsDouble(this.mData[r][c]);
            }
        }
        return this;
    }
    
    public Matrix applyFunctionElementwise(DoubleUnaryOperator func)
    {
        double[][] data = new double[this.mNRows][this.mNCols];
        for(int r = 0; r < mNRows; ++r){
            for(int c=0; c < mNCols; ++c){
                data[r][c] = func.applyAsDouble(this.mData[r][c]);
            }
        }
        return new Matrix(data, false);
    }
    
    
    /** Reduces columns of the matrix using reduction function (for ex. sum, min 
     * etc.) to produce a [1, nColumns] output matrix
     * 
     * @param reductionFunction     - DoubleBinaryOperator
     * @return                      - [1, nColumns] output matrix
     */
    public Matrix reduceColumns(DoubleBinaryOperator reductionFunction){
        double[][] resData = new double[1][this.getNCols()];
        
        for(int col = 0; col < this.getNCols(); ++col){
            double[] column = getCol(col);
            double accu = column[0];
            for(int i = 1; i < column.length; ++i){
                accu = reductionFunction.applyAsDouble(accu, column[i]);
            }
            resData[0][col] = accu;
        }
        return new Matrix(resData, false);
    }
    
    
    
    public Matrix reduceRows(DoubleBinaryOperator reductionFunction){
        double[][] resData = new double[this.getNRows()][1];
        
        for(int r = 0; r < resData.length; ++r){
            double[] row = getRow(r);
            double accu = row[0];
            for(int i = 1; i < row.length; ++i){
                accu = reductionFunction.applyAsDouble(accu, row[i]);
            }
            resData[r][0] = accu;
        }
        return new Matrix(resData, false);
    }
    
    
    public Matrix addInPlaceRepeatedRow(Matrix row){
        if(row.mNRows != 1 || this.mNCols != row.mNCols){
            throw new RuntimeException("Input is not a row matrix of an appropiate size");
        }
        for(int r = 0; r < mNRows; ++r){
            for(int col = 0; col < mNCols; ++col){
                mData[r][col] += row.mData[0][col];
            }
        }
        return this;
    }
    
    
    public Matrix addInPlaceRepeatedColumn(Matrix column){
        if(column.mNCols != 1 || this.mNRows != column.mNRows){
            throw new RuntimeException("Input is not a column matrix of an appropiate size");
        }
        for(int r = 0; r < mNRows; ++r){
            for(int col = 0; col < mNCols; ++col){
                mData[r][col] += column.mData[r][0];
            }
        }
        return this;
    }
    
    
    /** Serializes the object into an array of doubles.
     * The returned array can be sent between nodes in a distributed system and
     * reconstructed at the receiving node using static deserialize method.
     * The first element in returned array is the number of rows of the matrix,
     * and all the rest is the data packed row-wise.
     * 
     * @return 
     */
    public double[] serialize(){
        double[] res = new double[1 + mNRows*mNCols];
        res[0] = mNRows;
        int i = 1;
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                res[i++] = mData[row][col];
            }
        }
        return res;
    }
    
    /** The reverse of serialize().
     * 
     * @param array
     * @return 
     */
    public static Matrix deserialize(double[] array){
        int nRows = (int) array[0];
        int nCols = (array.length - 1)/nRows;
        return new Matrix(nRows, nCols, array, 1);
    }
    
    
    
    /** Reads a dense matrix from csv file.
     * 
     * @param csvFileName
     * @return 
     */
    public static Matrix readFromCSVFile(String csvFileName)
    {
        List<List<String>> strData;
        try {
            strData = readTXTFile(csvFileName);
        } catch (IOException ex) {
            Logger.getLogger(Matrix.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        Matrix ret = new Matrix(strData.size(), strData.get(0).size());
        for(int i = 0; i< ret.mNRows; ++i){
            List<String> strRow = strData.get(i);
            for(int j = 0; j< ret.mNCols; ++j){
                ret.setElem(i, j, Double.parseDouble(strRow.get(j)));
            }
        }
        return ret;
    }
    
    
    
    /** Taken from: http://codereview.stackexchange.com/questions/10681/java-function-to-read-a-csv-file
     * 
     * @param csvFileName
     * @return
     * @throws IOException 
     */
    private static List<List<String>> readTXTFile(String csvFileName) throws IOException {

        String line;
        BufferedReader stream = null;
        List<List<String>> csvData = new ArrayList<>();

        try {
            stream = new BufferedReader(new FileReader(csvFileName));
            while ((line = stream.readLine()) != null)
                csvData.add(Arrays.asList(line.split(",")));
        } finally {
            if (stream != null)
                stream.close();
        }

        return csvData;

    }
    
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
            for(int r = 0; r < getNRows(); ++r){
                if(r==0){
                    if (getNRows() == 1){
                        sb.append("[");
                    }else{
                        sb.append("\u250c ");
                    }
                }else if(r==getNRows()-1){
                    sb.append("\u2514 ");
                }
                else{
                    sb.append("\u2502 ");
                }
                for(int c = 0; c < getNCols(); ++c){
                    double val = mData[r][c];
                    if(val == Double.POSITIVE_INFINITY){
                        sb.append(String.format("  +Inf "));
                    }else if(val == Double.NEGATIVE_INFINITY){
                        sb.append(String.format("  -Inf "));
                    }
                    else{
                        sb.append(String.format("%6.2f ", val));
                    }
                    
                }
                if(r==0){
                    if (getNRows() == 1){
                       sb.append("]");
                    }else{
                        sb.append("\u2510\n");
                    }
                }else if(r==getNRows()-1){
                    sb.append("\u2518\n");
                }
                else{
                    sb.append("\u2502\n");
                }
                
            }
        return sb.toString();
    }
    
    
    public boolean isEqual(Matrix other){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            return false;
        }
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                if(mData[row][col] != other.mData[row][col]){
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean isEqualApproximately(Matrix other, double eps){
        if(this.mNRows != other.mNRows || this.mNCols != other.mNCols){
            return false;
        }
        for(int row = 0; row < mNRows; ++row){
            for(int col = 0; col < mNCols; ++col){
                if( Math.abs(mData[row][col] - other.mData[row][col]) > eps){
                    return false;
                }
            }
        }
        return true;
    }
    
}
