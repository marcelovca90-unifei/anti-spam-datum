package io.github.marcelovca90.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;

import org.apache.commons.math3.util.Pair;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.ShuffleSplitter;

public class InputOutputHelper
{
    public static void bin2csv(String hamInput, String spamInput, String outputFilename) throws IOException
    {
        File hamFile = new File(hamInput);
        File spamFile = new File(spamInput);
        File outputFile = new File(outputFilename);

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

        // read ham data
        FileInputStream hamStream = new FileInputStream(hamFile);
        FileChannel hamChannel = hamStream.getChannel();
        ByteBuffer hamBuffer = ByteBuffer.allocate((int) hamFile.length());
        hamChannel.read(hamBuffer);
        hamChannel.close();
        hamStream.close();
        hamBuffer.flip();

        int hamInstanceAmount = hamBuffer.getInt();
        int hamFeatureAmount = hamBuffer.getInt();
        double hamData;

        // write csv header
        for (int j = 0; j < hamFeatureAmount; j++)
            bufferedWriter.write("x" + (j + 1) + ",");
        bufferedWriter.write("class" + System.lineSeparator());

        // write ham data
        for (int i = 0; i < hamInstanceAmount; i++)
        {
            for (int j = 0; j < hamFeatureAmount; j++)
            {
                hamData = hamBuffer.getDouble();
                bufferedWriter.write(String.valueOf(hamData) + ",");
            }
            bufferedWriter.write("ham" + System.lineSeparator());
        }
        bufferedWriter.flush();

        // read spam data
        FileInputStream spamStream = new FileInputStream(spamFile);
        FileChannel spamChannel = spamStream.getChannel();
        ByteBuffer spamBuffer = ByteBuffer.allocate((int) spamFile.length());
        spamChannel.read(spamBuffer);
        spamChannel.close();
        spamStream.close();
        spamBuffer.flip();

        int spamInstanceAmount = spamBuffer.getInt();
        int spamFeatureAmount = spamBuffer.getInt();
        double spamData;

        // write spam data
        for (int i = 0; i < spamInstanceAmount; i++)
        {
            for (int j = 0; j < spamFeatureAmount; j++)
            {
                spamData = spamBuffer.getDouble();
                bufferedWriter.write(String.valueOf(spamData) + ",");
            }
            bufferedWriter.write("spam" + System.lineSeparator());
        }
        bufferedWriter.flush();

        bufferedWriter.close();
    }

    public static Dataframe csv2dataframe(String inputFilename, int numberOfFeatures) throws IOException
    {
        Dataframe data = null;

        Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename)));

        LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>();

        for (int i = 1; i <= numberOfFeatures; i++)
            headerDataTypes.put("x" + i, TypeInference.DataType.NUMERICAL);
        headerDataTypes.put("class", TypeInference.DataType.CATEGORICAL);

        data = Dataframe.Builder.parseCSVFile(fileReader, "class", headerDataTypes, ',', '"', System.lineSeparator(), null, null,
                Configuration.getConfiguration());

        return data;
    }

    public static Pair<Dataframe, Dataframe> dataframe2trainingAndTestingSets(Dataframe data)
    {
        // Spit into train and test datasets
        ShuffleSplitter.Split split = new ShuffleSplitter(0.6, 1).split(data).next();
        Dataframe trainingDataframe = split.getTrain();
        Dataframe testingDataframe = split.getTest();
        return Pair.create(trainingDataframe, testingDataframe);
    }

    public static void buildEmptyCsv(String folder, int featureAmount) throws IOException
    {
        Pair<Integer, Integer> emptyPatterns = EmptyPatternHelper.getEmptyPatternCountsByFolder(folder);
        int emptyHamCount = emptyPatterns.getFirst();
        int emptySpamCount = emptyPatterns.getSecond();

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < featureAmount; i++)
            buffer.append("0.0,");

        String emptyHam = buffer.toString() + "ham";
        String emptySpam = buffer.toString() + "spam";

        String output = folder + File.separator + "empty.csv";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(output)));

        for (int i = 0; i < featureAmount; i++)
            bufferedWriter.write("x" + (i + 1) + ",");
        bufferedWriter.write("class" + System.lineSeparator());

        for (int i = 0; i < emptyHamCount; i++)
            bufferedWriter.write(emptyHam + System.lineSeparator());

        for (int i = 0; i < emptySpamCount; i++)
            bufferedWriter.write(emptySpam + System.lineSeparator());

        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
