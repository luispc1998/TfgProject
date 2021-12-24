package domain.parsers;

import domain.DataHandler;
import domain.entities.Exam;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.ConsoleLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * This parses the exams from the Excel input file. It also writes the schedule to the ouput file.
 */
public class ExamParser {

    /**
     * Constant to store the name of the Excel headers.
     */
    private final static String[] excelHeaders = {
            "Curso",
            "Sem",
            "Cod",
            "Acron.",
            "Asignatura",
            "Orden",
            "Contenido",
            "Modalidad",
            "Alumnos",
            "Tiempo",
            "Fecha",
            "Día",
            "Ini",
            "Fin",
            "Extra time",
            "CN",
            "ID",
            "Tanda"
    };

    private final static HashMap<String, List<Integer>> rounds = new HashMap<>();

    /**
     * Parsing method of the exams
     * @param filepath The input Excel file
     * @return A {@code List} of parsed {@code Exam}
     */
    public static List<Exam> parseExams(String filepath, DataHandler dataHandler) {
        List<Exam> exams = new ArrayList<>();
        int i = 0;
        try (FileInputStream fis = new FileInputStream(filepath);
             Workbook workbook = new XSSFWorkbook(fis)
        ) {

            Sheet sheet = workbook.getSheet("Planificación");

            Map<Integer, List<String>> data = new HashMap<>();

            int jumpLines = 1;

            ConsoleLogger.getConsoleLoggerInstance().logInfo("Parseando exámenes...");

            for (Row row : sheet) {

                if (jumpLines > 0) {
                    jumpLines--;
                    continue;
                }

                Exam exam = generateExam(row, i, dataHandler);
                if (exam == null) {
                    continue;
                }
                i++;
                exams.add(exam);

            }

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not find input excel file");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not parse input excel file");
        }

        ConsoleLogger.getConsoleLoggerInstance().logInfo("Examenes creados: " + i);
        RoundsParser.createRoundIfNecessary(rounds, exams);
        return exams;
    }

    /**
     * Generates an exam based of a row.
     * @param row The row with the exm data.
     * @param i The index of the row.
     * @return The {@code Exam object parsed}
     */
    private static Exam generateExam(Row row, int i, DataHandler dataHandler) {
        Exam exam = null;
        String round;
        try {
            //checkVitalRowData(row, i);
            exam = new Exam(parseMandatoryNumberCell(row, 0),
                    parseMandatoryNumberCell(row, 1),
                    row.getCell(2).getStringCellValue(),
                    row.getCell(3).getStringCellValue(),
                    row.getCell(4).getStringCellValue(),
                    parseNumberCell(row, 5),
                    row.getCell(6).getStringCellValue(),
                    row.getCell(7).getStringCellValue(),
                    parseNumberCell(row, 8),
                    row.getCell(9).getNumericCellValue(),
                    (int) row.getCell(15).getNumericCellValue(),
                    parseMandatoryNumberCell(row, 16), null);

            if (row.getCell(17) != null && ! row.getCell(17).getStringCellValue().isEmpty()) {
                round = row.getCell(17).getStringCellValue();
                exam.setRoundId(round);
                if (! rounds.containsKey(row.getCell(17).getStringCellValue())){
                    rounds.put(round, new ArrayList<>());
                }
                rounds.get(row.getCell(17).getStringCellValue()).add(exam.getId());
            }

            if (checkForAlreadyClassifiedExam(row)) {
                exam.setDateFromExcel(row.getCell(10).getDateCellValue());
                exam.setHourFromExcel(row.getCell(12).getNumericCellValue());
            }

            if (row.getCell(14) != null && row.getCell(14).getNumericCellValue() >= 0) {
                exam.setExtraTimeFromExcel(row.getCell(14).getNumericCellValue());
            }
            else {
                exam.setExtraTime(dataHandler.getConfigurer().getDateTimeConfigurer().getDefaultExamExtraMinutes());
            }



        } catch (IllegalArgumentException e) {
            ConsoleLogger.getConsoleLoggerInstance().logWarning(e.getMessage() + " [Line: " + i + "] Skipping...");
        } catch (Exception e){
            ConsoleLogger.getConsoleLoggerInstance().logWarning("Unknown error raised when creating exam "
                    + "[Line: " + i + "] Skipping...");

            //System.out.println("Unknown error raised when creating exam from line: " + i);
        }

        return exam;
    }




    /**
     * Parses a cell with Number content.
     * @param row The row in which the cell is.
     * @param cell The cell to be checked.
     * @return The value of the cell. Null if no value or 0.
     */
    private static Integer parseNumberCell(Row row, int cell) {
        if (row.getCell(cell) == null || row.getCell(cell).getNumericCellValue() == 0) {
            return null;
        }

        return Double.valueOf(row.getCell(cell).getNumericCellValue()).intValue();
    }

    /**
     * Parses a cell with Number content.
     * @param row The row in which the cell is.
     * @param cell The cell to be checked.
     * @return The value of the cell. Null if no value or 0.
     */
    private static Integer parseMandatoryNumberCell(Row row, int cell) {
        if (row.getCell(cell) == null) {
            throw new IllegalArgumentException("Cannot omit cell: " + cell + " for an exam");
        }

        return Double.valueOf(row.getCell(cell).getNumericCellValue()).intValue();
    }

    /**
     * Checks if an exam is already classified.
     * @param row The row of the exam
     * @return true if the exam was classified, false otherwise.
     */
    private static boolean checkForAlreadyClassifiedExam(Row row) {

        if (row.getCell(10) != null && row.getCell(12) != null) {
            try {
                return !row.getCell(10).getDateCellValue().toString().equals("") && row.getCell(12).getNumericCellValue() != 0;
            } catch (Exception e){
                return false;
            }
        }

        return false;
    }



    /**
     * Parses the exam schedule to an Excel file.
     * @param exams The exam schedule.
     * @param workbook The workbook where the exam scheduling must be written.
     */
    public static void parseToExcel(List<Exam> exams, XSSFWorkbook workbook) {

        XSSFSheet sheet = workbook.createSheet("Planificación");

        int rowCount = 0;
        Row row = sheet.createRow(rowCount);
        writeHeaders(row);

        for (Exam exam : exams) {
            row = sheet.createRow(++rowCount);
            int cellCount = 0;

            for (Object att : exam.getAttributes()) {
                Cell cell = row.createCell(cellCount++);
                if (att == null){
                    continue;
                }
                switch (cellCount-1) {

                    case 0:
                    case 1:
                    case 5:
                    case 8:
                    //case 14:
                    case 15:
                    case 16:
                        cell.setCellValue((int) att);
                        break;
                    case 14:
                    case 9: //duration
                        cell.setCellValue((double) att);
                        break;
                    case 10: //date
                        cell.setCellValue(DateUtil.getExcelDate(Date.from(((LocalDate) att).atStartOfDay(ZoneId.systemDefault()).toInstant())));
                        break;
                    case 12: //time
                    case 13:
                        cell.setCellValue(DateUtil.convertTime(((LocalTime) att).toString()));
                        break;

                    default:
                        cell.setCellValue((String) att);
                }

            }



        }
    }

    /**
     * Writes the headers row for the excel file
     * @param row The row at which the Headers will be written.
     */
    private static void writeHeaders(Row row) {
        int cellCount = 0;
        for (String header : excelHeaders) {
            Cell cell = row.createCell(cellCount++);
            cell.setCellValue(header);
        }
    }


}


