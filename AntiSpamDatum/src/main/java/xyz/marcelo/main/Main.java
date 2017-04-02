package xyz.marcelo.main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.BasicConfigurator;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;

import xyz.marcelo.helper.DataSetHelper;
import xyz.marcelo.helper.InputOutputHelper;
import xyz.marcelo.helper.MethodHelper;
import xyz.marcelo.helper.PrimeHelper;

public class Main
{
    private static String baseFolderPath;
    private static int numberOfRepetitions;
    private static String[] metrics;
    private static String key;
    private static long start, end;
    private static boolean printedHeader = false;

    public static void main(String[] args) throws Exception
    {
        // exits if the wrong number of arguments was provided
        if (args.length != 2)
        {
            System.out.println("Usage: java -jar AntiSpamDatum.jar \"DATA_SET_FOLDER\" NUMBER_OF_REPETITIONS");
            System.exit(1);
        }
        else
        {
            // tries to build the folder and method configuration lists
            try
            {
                baseFolderPath = args[0];
                numberOfRepetitions = Integer.parseInt(args[1]);
                metrics = new String[] { "accuracy", "ham_precision", "spam_precision", "ham_recall", "spam_recall", "training_time", "testing_time" };
            }
            catch (Exception e)
            {
                // if an invalid data set folder was provided
                if (e instanceof IOException)
                    System.out.println("The specified data set folder is invalid.");
                // or if an invalid number of repetitions was provided
                else if ((e instanceof IllegalArgumentException) && (e instanceof NumberFormatException))
                    System.out.println("The specified number of repetitions is invalid.");

                // exit the program
                System.exit(1);
            }
        }

        // initialize basic configuration for log4j
        BasicConfigurator.configure();

        // initialize default configuration based on properties file
        Configuration configuration = Configuration.getConfiguration();

        for (AbstractClassifier<?, ?> classifier : MethodHelper.getAll(configuration))
        {
            for (String folder : DataSetHelper.getFolders(baseFolderPath))
            {
                // Initialize statistics HashMap
                Map<String, DescriptiveStatistics> stats = new LinkedHashMap<>();
                Arrays.stream(metrics).forEach(m -> stats.put(m, new DescriptiveStatistics()));

                for (int rep = 1; rep <= numberOfRepetitions; rep++)
                {
                    // Set the global seed to the next prime for all Random objects
                    RandomGenerator.setGlobalSeed((long) PrimeHelper.getNextPrime());

                    // Separate the training and testing data sets
                    Dataframe trainingDataframe = InputOutputHelper.csv2dataframe(folder + File.separator + "train.csv");
                    Dataframe testingDataframe = InputOutputHelper.csv2dataframe(folder + File.separator + "test.csv");

                    // Fit the classifier
                    start = System.currentTimeMillis();
                    classifier.fit(trainingDataframe);
                    end = System.currentTimeMillis();
                    key = "training_time";
                    stats.putIfAbsent(key, new DescriptiveStatistics());
                    stats.get(key).addValue(end - start);

                    // Serialize the classifier
                    classifier.save("AntiSpamClassifier@" + classifier.getClass().getSimpleName());

                    // Use the classifier to make predictions on the testingDataframe and get validation metrics
                    start = System.currentTimeMillis();
                    classifier.predict(testingDataframe);
                    ClassificationMetrics vm = new ClassificationMetrics(testingDataframe);
                    end = System.currentTimeMillis();
                    key = "testing_time";
                    stats.putIfAbsent(key, new DescriptiveStatistics());
                    stats.get(key).addValue(end - start);

                    // Add single run accuracy statistics
                    key = "accuracy";
                    stats.putIfAbsent(key, new DescriptiveStatistics());
                    stats.get(key).addValue(100f * vm.getAccuracy());

                    // Add single run precision statistics
                    for (Entry<Object, Double> entry : vm.getMicroPrecision().entrySet())
                    {
                        key = entry.getKey() + "_precision";
                        stats.putIfAbsent(key, new DescriptiveStatistics());
                        stats.get(key).addValue(100f * entry.getValue());
                    }

                    // Add single run recall statistics
                    for (Entry<Object, Double> entry : vm.getMicroRecall().entrySet())
                    {
                        key = entry.getKey() + "_recall";
                        stats.putIfAbsent(key, new DescriptiveStatistics());
                        stats.get(key).addValue(100f * entry.getValue());
                    }

                    // Clean up: close Dataframes.
                    trainingDataframe.close();
                    testingDataframe.close();
                }

                // Consolidate and print statistics
                if (!printedHeader)
                {
                    System.out.print(String.format("%s\t%s", "folder", "method"));
                    stats.keySet().forEach(k -> System.out.print(String.format("\t%s", k)));
                    System.out.println();
                    printedHeader = true;
                }
                System.out.print(String.format("%s\t%s", folder.replace(baseFolderPath, ""), classifier.getClass().getSimpleName()));
                stats.values().forEach(v -> System.out.print(String.format("\t%.2f ± %.2f\t", v.getMean(), v.getStandardDeviation())));
                System.out.println();
            }

            // Clean up: delete the classifier.
            classifier.delete();
        }
    }
}
