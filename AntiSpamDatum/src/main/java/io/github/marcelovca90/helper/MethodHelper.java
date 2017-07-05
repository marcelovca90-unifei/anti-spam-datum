package xyz.marcelo.helper;

import java.util.ArrayList;
import java.util.List;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.classification.BernoulliNaiveBayes;
import com.datumbox.framework.core.machinelearning.classification.BinarizedNaiveBayes;
import com.datumbox.framework.core.machinelearning.classification.MaximumEntropy;
import com.datumbox.framework.core.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.core.machinelearning.classification.OrdinalRegression;
import com.datumbox.framework.core.machinelearning.classification.SoftMaxRegression;
import com.datumbox.framework.core.machinelearning.classification.SupportVectorMachine;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;

import libsvm.svm_parameter;

public class MethodHelper
{
    public static List<AbstractClassifier<?, ?>> getAll(Configuration configuration)
    {
        List<AbstractClassifier<?, ?>> list = new ArrayList<>();

        BernoulliNaiveBayes.TrainingParameters bernoulliNaiveBayesParams = new BernoulliNaiveBayes.TrainingParameters();
        bernoulliNaiveBayesParams.setMultiProbabilityWeighted(true);
        BernoulliNaiveBayes bernoulliNaiveBayes = MLBuilder.create(bernoulliNaiveBayesParams, configuration);
        list.add(bernoulliNaiveBayes);

        BinarizedNaiveBayes.TrainingParameters binarizedNaiveBayesParams = new BinarizedNaiveBayes.TrainingParameters();
        binarizedNaiveBayesParams.setMultiProbabilityWeighted(true);
        BinarizedNaiveBayes binarizedNaiveBayes = MLBuilder.create(binarizedNaiveBayesParams, configuration);
        list.add(binarizedNaiveBayes);

        MaximumEntropy.TrainingParameters maximumEntropyParams = new MaximumEntropy.TrainingParameters();
        maximumEntropyParams.setTotalIterations(100);
        MaximumEntropy maximumEntropy = MLBuilder.create(maximumEntropyParams, configuration);
        list.add(maximumEntropy);

        MultinomialNaiveBayes.TrainingParameters multinomialNaiveBayesParams = new MultinomialNaiveBayes.TrainingParameters();
        multinomialNaiveBayesParams.setMultiProbabilityWeighted(true);
        MultinomialNaiveBayes multinomialNaiveBayes = MLBuilder.create(multinomialNaiveBayesParams, configuration);
        list.add(multinomialNaiveBayes);

        OrdinalRegression.TrainingParameters ordinalRegressionParams = new OrdinalRegression.TrainingParameters();
        ordinalRegressionParams.setL2(0.0);
        ordinalRegressionParams.setLearningRate(0.1);
        ordinalRegressionParams.setTotalIterations(100);
        OrdinalRegression ordinalRegression = MLBuilder.create(ordinalRegressionParams, configuration);
        list.add(ordinalRegression);

        SoftMaxRegression.TrainingParameters softMaxRegressionParams = new SoftMaxRegression.TrainingParameters();
        softMaxRegressionParams.setL1(0.0);
        softMaxRegressionParams.setL2(0.0);
        softMaxRegressionParams.setTotalIterations(100);
        softMaxRegressionParams.setLearningRate(0.1);
        SoftMaxRegression softMaxRegression = MLBuilder.create(softMaxRegressionParams, configuration);
        list.add(softMaxRegression);

        SupportVectorMachine.TrainingParameters supportVectorMachineParams = new SupportVectorMachine.TrainingParameters();
        svm_parameter svmParameter = new svm_parameter();
        svmParameter.C = 1e+3;
        svmParameter.cache_size = 2048;
        svmParameter.gamma = 1e-3;
        svmParameter.kernel_type = svm_parameter.RBF;
        svmParameter.svm_type = svm_parameter.C_SVC;
        supportVectorMachineParams.setSvmParameter(svmParameter);
        SupportVectorMachine supportVectorMachine = MLBuilder.create(supportVectorMachineParams, configuration);
        list.add(supportVectorMachine);

        return list;
    }
}
