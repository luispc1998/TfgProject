package utils;

import geneticAlgorithm.Individual;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import utils.random.RandomGenerator;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Utility class with some methods.
 */
public class Utils {

    /**
     * Normalizes an array to 0-1 interval.
     * @param numberArray The array to be normalized
     * @return The normalized array.
     */
    public static double[] normalizeDoubleArray(double[] numberArray){
        double divisor = 0;
        for (double number: numberArray) {
            divisor += number;
        }

        int arrayLength = numberArray.length;
        double[] normalizedArray = new double[arrayLength];

        if (divisor!=0) {

            for (int i = 0; i < arrayLength; i++) {
                normalizedArray[i] = numberArray[i] / divisor;
            }
        }
        return normalizedArray;


    }

    /**
     *
     * @param popSize Size of the population to be generated.
     * @param individualPrime First individual, from which the others will be generated by shuffling.
     * @return A population of individuals of size {@code popSize} and Chromosomes based
     * on the one of {@code individualPrime}
     */
    public static List<Individual> generatePopulationOfSizeFromIndividual(int popSize, Individual individualPrime) {
        List<Individual> population = new ArrayList<>();
        population.add(individualPrime);
        Random gn = RandomGenerator.getGenerator();
        List<Integer> chromosomePrime = individualPrime.getChromosome();
        for (int i = 0; i < popSize-1; i++) {
            List<Integer> chromosomeClone = new ArrayList<>(chromosomePrime);
            Collections.shuffle(chromosomeClone, gn);
            population.add(new Individual(chromosomeClone));
        }
        return population;
    }

    /**
     * Creates a directory based on the current date and hour.
     * @return A directory name based on the date and time.
     */
    public static String createDirectoryStringBasedOnHour() {
        LocalDate ld = LocalDate.now();
        LocalTime lt = LocalTime.now();

        return String.format("%04d", ld.getYear()) +
                String.format("%02d", ld.getMonthValue()) +
                String.format("%02d", ld.getDayOfMonth()) +
                "_" +
                String.format("%02d", lt.getHour()) +
                String.format("%02d", lt.getMinute());
    }

    /**
     * Creates a directory given its path.
     * @param directory The path of the directory to be created.
     */
    public static void createDirectory(String directory) {

        File theDir = new File(directory);

        if (!theDir.exists()){
            if (!theDir.mkdirs()) {
                throw new RuntimeException("No se ha podido crear el directorio de salida de estadísticas. Path: " + theDir);
            }
        }
    }

    /**
     * Gets the first different {@code Individual} instances from a list and puts then on a set.
     * @param finalPopulation The list from which the individuals will be obtained.
     * @param finalSet The set in which the individuals will be outputted.
     * @param maxToTake The maximum number of indiviudals to take.
     */
    public static void getBestSchedules(List<Individual> finalPopulation, HashSet<Individual> finalSet, int maxToTake) {
        for (Individual idv: finalPopulation) {
            if (maxToTake <= 1) {
                break;
            }
            if (! hashSetContains(finalSet, idv)) {
                finalSet.add(idv);
                maxToTake--;
            }
        }
    }

    /**
     * Contains method for the HashSet.
     * @param finalSet Set in which we are checking the existence of {@code idv}.
     * @param idv The individual that we are checking if in {@code finalSet}.
     * @return True un case the individuals was contained on the set, False otherwise.
     */
    public static boolean hashSetContains(Set<Individual> finalSet, Individual idv) {
        for (Individual fidv: finalSet) {
            if (fidv.equals(idv)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Creates the output directory for each execution of the program.
     * @param outputBaseDirectory The base directory path in which the outputs of the program will be saved.
     * @return The path of the generated directory in which the results of the program execution will be written.
     */
    public static String createOutputDirectory(String outputBaseDirectory) {


        StringBuilder directoryBuilder = new StringBuilder();
        directoryBuilder.append(outputBaseDirectory);
        directoryBuilder.append(Utils.createDirectoryStringBasedOnHour());

        File theDir = new File(directoryBuilder.toString());

        if (!theDir.exists()){
            if (!theDir.mkdirs()) {
                throw new RuntimeException("No se ha podido crear el directorio de salida. Path: " + theDir);
            }
        }

        return directoryBuilder + "/";
    }



    public static void checkCellValueIsPresent(Row row, int i, String errorMessage) {
        if (row.getCell(i) == null || row.getCell(i).getCellType().equals(CellType.BLANK)){
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void checkCellValuesArePresent(Row row, int[] cells, String initialErrorMessage) {
        List<Integer> notPresent = new ArrayList<>();
        for (Integer i: cells) {
            if (row.getCell(i) == null || row.getCell(i).getCellType().equals(CellType.BLANK)){
                notPresent.add(i);
            }
        }
        if (notPresent.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(initialErrorMessage);
            sb.append(" [Line: ");
            sb.append(row.getRowNum());
            sb.append("]");
            sb.append(" Cannot omit values for cells: ");
            sb.append("[");
            sb.append(notPresent.get(0));
            for (int i = 1; i < notPresent.size(); i++) {
                sb.append(", ");
                sb.append(notPresent.get(i));
            }
            sb.append("]");

            throw new IllegalArgumentException(sb.toString());
        }

    }


    public static String nullFilter(String stringProperty) {
        if (stringProperty == null){
            throw new NullPointerException();
        }
        return stringProperty;
    }

    public static boolean emptyCell(Cell cell) {
        return cell == null || cell.getCellType().equals(CellType.BLANK);
    }
}
