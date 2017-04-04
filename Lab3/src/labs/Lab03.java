/*
 *  This code is for Parallel and Distributed Algorithms
 *  laboratory at Gdansk University of Technology
 */
package labs;

import algorithms.shared.SortSM;
import algorithms.distributed.Sort;
import distributedmodel.DSConfig;
import distributedmodel.DistributedSystem;
import distributedmodel.Node;

import java.util.Arrays;
import java.util.Comparator;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Karol Draszawka <kadr@eti.pg.gda.pl>
 */
public class Lab03 extends BaseLabMixed {

    boolean verboseCommunication = false;
    boolean validateResults = true;
    boolean printProcessingTime = false;
    boolean printStatusBeforeAndAfter = false;

    @Override
    public void testAll() {

        //Distributed system part:

        singleTestRun(new DSConfig(8, DELAY_CONNECT_MILLIS, DELAY_TRANSMIT_MILLIS),
                (Node node) -> {
                    node.setLogCommunication(verboseCommunication);
                    Sort.bitonicSortGeneralizedDesc(node);
                },
                (DistributedSystem ds) -> {
                    arrayLengthPerNode = 4;
                    initNodesWithData(ds);
                },
                validateResults, printProcessingTime, printStatusBeforeAndAfter);

        //Shared memory part:

        singleTestRun(128, (double[] data) -> {
                    SortSM.QuicksortDescendingForkJoin(data);
                },
                validateResults, printProcessingTime, printStatusBeforeAndAfter);

        testSortAlgorithmsSharedMemory();

    }


    private void testSortAlgorithmsSharedMemory() {

        XYSeriesCollection seriesCollection = new XYSeriesCollection();

        XYSeries exp1 = seriesOfRuns("Serial version",
                (double[] data) -> {
                    SortSM.serialQuicksort(data);
                },
                false, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp1);

        XYSeries exp2 = seriesOfRuns("ForkJoin version",
                (double[] data) -> {
                    SortSM.QuicksortDescendingForkJoin(data);
                },
                false, printProcessingTime, printStatusBeforeAndAfter);
        seriesCollection.addSeries(exp2);

        JFreeChart chart = makeChart(seriesCollection,
                "Quicksort performance (shared memory)",
                "Number of elements to sort", "Execution time [s]");

        displayChart(chart);
    }


    @Override
    protected void initNodesWithData(DistributedSystem ds) {
        initialNodesData = generateRandomDataForNodes(ds.getConfiguration().getNumberOfNodes(), arrayLengthPerNode);
        for (Node n : ds.getNodes()) {
            n.setMyData(initialNodesData[n.getMyId()]);
        }
    }

    @Override
    protected void validateDSState(DistributedSystem ds) {
        double[] referenceData = algorithms.Utils.flatten2dArray(initialNodesData);
        referenceData = Arrays.stream(referenceData).mapToObj(v -> v)
                .sorted(Comparator.reverseOrder()).mapToDouble(v -> (v)).toArray();

        for (int i = 0; i < ds.getConfiguration().getNumberOfNodes(); ++i) {
            double[] nodeData = ds.getNode(i).getMyData();
            if (nodeData == null || nodeData.length != arrayLengthPerNode) {
                failedValidationInfo();
                return;
            }
            for (int j = 0; j < nodeData.length; ++j) {
                if (nodeData[j] != referenceData[arrayLengthPerNode * i + j]) {
                    failedValidationInfo();
                    return;
                }
            }
        }
        successfulValidationInfo();
    }

    @Override
    protected void initSharedMemory(double[] data) {
        for (int i = 0; i < data.length; ++i) {
            data[i] = Math.random();
        }
    }

    @Override
    protected void validateSharedMemory(double[] data) {
        if (algorithms.Utils.isSortedDescending(data)) {
            successfulValidationInfo();
        } else {
            failedValidationInfo();
        }
    }

    double[][] initialNodesData;
    int arrayLengthPerNode;
}
