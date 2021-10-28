package main;

import configuration.Configurer;
import domain.DataHandler;
import domain.entities.Exam;
import domain.entities.ExamDatesComparator;
import domain.parsers.ExamParser;
import fitnessFunctions.FitnessFunction;
import fitnessFunctions.greedyAlgorithm.CromosomeDecoder;
import fitnessFunctions.greedyAlgorithm.LinearFitnessFunction;
import geneticAlgorithm.Enconder;
import geneticAlgorithm.GeneticCore;
import geneticAlgorithm.Individual;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.*;

public class App {

    public static void main(String[] args) throws IOException {




        Configurer conf = new Configurer("files");

        DataHandler dataHandler = new DataHandler(conf);


        Enconder basicEncoder = new Enconder();
        Individual individualPrime = basicEncoder.encodeListExams(dataHandler.getExams());


        FitnessFunction fn = new LinearFitnessFunction(dataHandler);

        GeneticCore genCore = new GeneticCore(individualPrime, 10000);

        Individual finalOne = genCore.geneticAlgorithm(0.15, fn, 300);
        System.out.println();

        CromosomeDecoder decoder = new CromosomeDecoder();

        decoder.decode(finalOne, dataHandler);


        List<Exam> finalResult = dataHandler.getClonedSchedule();
        Comparator<Exam> examComparator = new ExamDatesComparator();
        finalResult.sort(examComparator);
        ExamParser.parseToExcel(finalResult, dataHandler.getConfigurer().getFilePaths("outputFile"));




    }
}
