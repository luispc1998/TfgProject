package domain;

import configuration.Configurer;
import domain.constrictions.Constriction;
import domain.constrictions.types.singles.UnclassifiedExamsConstriction;
import domain.entities.Exam;
import domain.parsers.ConstrictionParser;
import domain.parsers.ExamParser;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataHandler {

    private final Configurer configurer;
    private List<Exam> exams;
    private Set<String> preScheduledExams;
    private List<Constriction> constrictions;



    public DataHandler(Configurer configurer) throws IOException {

        this.configurer = configurer;
        this.preScheduledExams = new HashSet<>();

        String inputDataFile = configurer.getFilePaths("inputFile");
        this.exams = ExamParser.parseExams(inputDataFile);
        identifyScheduledExams();


        this.constrictions = ConstrictionParser.parseConstrictions(inputDataFile, this);
        addConstriction(new UnclassifiedExamsConstriction(exams));

    }

    private void identifyScheduledExams() {
        for (Exam exam: exams) {
            if (exam.isScheduled()){
                preScheduledExams.add(exam.getCode());
            }
        }
    }

    public void resetScheduling(){
        for (Exam exam: exams) {
            if (! wasScheduled(exam)){
               exam.resetScheduling();
            }
        }
    }

    private boolean wasScheduled(Exam exam) {
        return preScheduledExams.contains(exam.getCode());
    }


    public List<Exam> getExams(){
        return new ArrayList<>(exams);
    }

    public Exam getExam(String code){
        for (Exam exam: exams) {
            if (exam.getCode().equals(code)){
                return exam;
            }
        }
        return null;
    }


    public List<Exam> getClonedSchedule() {
        List<Exam> clonedExams = new ArrayList<>();
        for (Exam exam: exams) {
            clonedExams.add(exam.clone());
        }
        return clonedExams;
    }


    public List<Constriction> getConstrictions() {
        return new ArrayList<>(constrictions);
    }



    public Exam checkColisionOf(LocalDate currentDate, LocalTime currentHour, Duration duration) {
        for (Exam exam: exams) {
            if (exam.isScheduled()){
                if (exam.willCollideWith(currentDate,currentHour,duration)){
                    return exam;
                }
            }
        }
        return null;
    }

    public void schedule(Exam exam, LocalDate currentDate, LocalTime currentHour) {
        exam.scheduleFor(currentDate, currentHour);
    }

    public Configurer getConfigurer() {
        return configurer;
    }

    public Exam getExam(Integer index) {
        return exams.get(index);
    }


    public void addConstriction(Constriction constriction) {
        constrictions.add(constriction);
    }
}
