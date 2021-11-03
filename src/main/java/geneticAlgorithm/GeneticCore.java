package geneticAlgorithm;

import fitnessFunctions.FitnessFunction;
import geneticAlgorithm.operators.crossing.CrossingOperator;
import geneticAlgorithm.operators.crossing.OXCrosssingOperator;
import geneticAlgorithm.operators.mutation.MutationOperator;
import geneticAlgorithm.operators.mutation.MutationSwap;
import geneticAlgorithm.operators.replacement.ReplacementOperator;
import geneticAlgorithm.operators.replacement.ReplacementOperatorImpl;
import geneticAlgorithm.operators.selection.RouletteSelection;
import geneticAlgorithm.operators.selection.SelectionOperator;
import geneticAlgorithm.utils.Utils;
import me.tongfei.progressbar.ProgressBar;
import random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GeneticCore {

    /**
     * First population of individuals
     */
    private List<Individual> initialPopulation;

    /**
     * Current population of individuals
     */
    private List<Individual> population;

    // Operators that will be used in the execution

    /**
     * Selection operator for the genetic algorithm.
     * <p>
     * The design follows the "Strategy" design pattern.
     */
    private SelectionOperator selectionOperator;

    /**
     * Mutation operator for the genetic algorithm.
     * <p>
     * The design follows the "Strategy" design pattern.
     */
    private MutationOperator mutationOperator;

    /**
     * Crossing operator for the genetic algorithm.
     * <p>
     * The design follows the "Strategy" design pattern.
     */
    private CrossingOperator crossingOperator;

    /**
     * Replacement operator for the genetic algorithm.
     * <p>
     * The design follows the "Strategy" design pattern.
     */
    private ReplacementOperator replacementOperator;


    /**
     * Constructor for the class
     * @param individualPrime First individual from which the initial population will be created.
     * @param popSize Size of the population to be handled by the algorithm.
     */
    public GeneticCore(Individual individualPrime, int popSize) {
        initialPopulation = Utils.generatePopulationOfSizeFromIndividual(popSize, individualPrime);
        population = new ArrayList<>(initialPopulation);

        this.selectionOperator = new RouletteSelection(initialPopulation.size()/2);
        this.mutationOperator = new MutationSwap();
        this.crossingOperator = new OXCrosssingOperator();
        this.replacementOperator = new ReplacementOperatorImpl();
    }

    /**
     * Genetic algorithm skeleton
     * @param mutationProbability Probability for new individuals to mutate
     * @param fitnessFunction Fitness function to be used by the algorithm
     * @param maxIterations Maximum number of iterations that the algorithm will do.
     * @return The best individual
     */ //TODO, return a list of different individuals whose index is the best as possible.
    public Individual geneticAlgorithm(double mutationProbability, FitnessFunction fitnessFunction, int maxIterations) {

        // Coger al mejor individuo y hacer la media del fitness para estudiar convergencia.
        int genCounter = 0;

        // Needed to be repeated for initial generation
        Individual bestIndividual = getBestIndividual(fitnessFunction);
        double averageFitness = averageFitness(fitnessFunction);

        System.out.println("\n" + "Gen: " + genCounter
                + ", Best Fitness: " + bestIndividual.getFitnessScore(fitnessFunction)
                + ", Avg Fitness: " + averageFitness);

        System.out.println(bestIndividual);

        try (ProgressBar pb = new ProgressBar("GA", maxIterations)) { // name, initial max
            while (genCounter < maxIterations) { //limit by iterations, limit by finnding a solution.

                population = computeNewGeneration(fitnessFunction, mutationProbability);
                genCounter++;

                bestIndividual = getBestIndividual(fitnessFunction);
                averageFitness = averageFitness(fitnessFunction);

                /*
                System.out.println("\n" + "Gen: " + genCounter
                        + ", Best Fitness: " + bestIndividual.getFitnessScore(fitnessFunction)
                        + ", Avg Fitness: " + averageFitness);

                System.out.println(bestIndividual);
                */

                pb.step();
                pb.setExtraMessage("Gen: " + genCounter + ", BF: " + bestIndividual.getFitnessScore(fitnessFunction) +
                        ", AF: " + averageFitness);
            }
            System.out.println("\n" + "[Gen: " + genCounter
                    + ", Best Fitness: " + bestIndividual.getFitnessScore(fitnessFunction)
                    + ", Avg Fitness: " + averageFitness + "]");

            System.out.println(bestIndividual);

        }
            return bestIndividual;
    }

    /**
     * Computes the average fitness of the current population.
     * @param fitnessFunction Fitness function to be used by the algorithm.
     * @return The average fitness of the current population.
     */
    private double averageFitness(FitnessFunction fitnessFunction) {

        double accumulator = 0;

        for (Individual idv: population) {
            accumulator += idv.getFitnessScore(fitnessFunction);
        }

        return accumulator / population.size();
    }

    /**
     * Returns the best individual of the population.
     *
     * <p>
     * The best individual is the one with less fitness value. We are maximizing f(x) = 1 / fitnessValue
     * @param fitnessFunction Fitness function to be used by the algorithm,
     * @return The individual with less fitness value of the population
     */
    private Individual getBestIndividual(FitnessFunction fitnessFunction) {
        // We are minimizing, the best individual is the closest fitness to 0
        Individual bestIndividual = null;
        double bestFitnessFound = Double.POSITIVE_INFINITY;

        for (Individual idv: population) {
            double idvFitness = idv.getFitnessScore(fitnessFunction);
            if (idvFitness < bestFitnessFound){
                bestFitnessFound = idvFitness;
                bestIndividual = idv;
            }
        }

        return bestIndividual;
    }

    /**
     * Computes a new generation of the population.
     * @param fitnessFunction Fitness function to be used by the algorithm
     * @param mutationProbability The mutation probability of the new individuals.
     * @return The new population.
     */
    private List<Individual> computeNewGeneration(FitnessFunction fitnessFunction, double mutationProbability) {

        List<Individual> newGenChilds = new ArrayList<>(population.size());

        selectionOperator.reset();
        for (int i = 0; i < selectionOperator.maxPairs(); i++) {
            Individual father = selectionOperator.selection(population, fitnessFunction);
            Individual mother = selectionOperator.selection(population, fitnessFunction);

            List<Individual> childs = crossingOperator.doCrossing(father, mother);
            checkForMutation(childs, mutationProbability);
            newGenChilds.addAll(childs);
        }

        return replacementOperator.doReplacement(population, newGenChilds, fitnessFunction);
    }

    /**
     * Checks if a child must have a mutation
     * @param childs List of the new individuals
     * @param mutationProb The mutation probability of the new individuals.
     */
    private void checkForMutation(List<Individual> childs, double mutationProb) {
        for (Individual child: childs) {
            if (RandomGenerator.getGenerator().nextDouble() <= mutationProb){
              mutationOperator.mutation(child);
            }
        }
    }

    /**
     * Returns the current population.
     * @return The current population.
     */
    public List<Individual> getPopulation(){
        return population;
    }
}
