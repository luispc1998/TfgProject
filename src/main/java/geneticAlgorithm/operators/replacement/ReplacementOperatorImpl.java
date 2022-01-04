package geneticAlgorithm.operators.replacement;

import geneticAlgorithm.fitnessFunctions.FitnessFunction;
import geneticAlgorithm.Individual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReplacementOperatorImpl implements ReplacementOperator {

    /**
     * This implementation takes the individuals with higher fitness for theh next generations.
     * @param prevGeneration Individuals of the previous generation.
     * @param childs Individuals created by crossing the individuals in {@code prevGeneration}.
     * @param fitnessFunction The fitness function of the algorithm.
     * @return The new generation of Individuals.
     */
    @Override
    public List<Individual> doReplacement(List<Individual> prevGeneration, List<Individual> childs,
                                          FitnessFunction fitnessFunction) {

        List<Individual> replacements;

        List<Individual> tmp = new ArrayList<>();

        tmp.addAll(childs);
        tmp.addAll(prevGeneration);


        tmp.sort(Comparator.comparingDouble(c -> c.getFitnessScore(fitnessFunction)));


        replacements = tmp.subList(0, prevGeneration.size());
        prevGeneration.sort(Comparator.comparingDouble(c -> c.getFitnessScore(fitnessFunction)));
        childs.sort(Comparator.comparingDouble(c -> c.getFitnessScore(fitnessFunction)));

        for (int i = 0; i < 5; i++) {
           childs.set(childs.size()-1-i, prevGeneration.get(i));
        }
        //childs.set(childs.size()-1, prevGeneration.get(0));
        return childs;
    }



}
