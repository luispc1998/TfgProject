package geneticAlgorithm.operators.selection;

import geneticAlgorithm.Individual;
import geneticAlgorithm.fitnessFunctions.FitnessFunction;

import java.util.List;

public interface SelectionOperator {

    Individual selection(List<Individual> population, FitnessFunction fitnessFunction);

    void reset();

    int maxPairs();
}
