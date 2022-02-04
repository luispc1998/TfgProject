package domain.parsers.constrictionsParserTools;

import domain.DataHandler;
import domain.constraints.Constraint;
import domain.constraints.types.softConstrictions.userConstraints.OrderExamsConstraint;
import domain.constraints.types.softConstrictions.userConstraints.UserConstraint;
import domain.entities.Exam;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import utils.Utils;

/**
 * This is the parser for {@link OrderExamsConstraint}
 */
public class OrderExamsConstrictionParserTool extends AbstractCosntrictionParserTool {
    @Override
    public UserConstraint specificParseConstriction(Row row, int baseExcelColumn, DataHandler dataHandler) {
        Utils.checkCellValuesArePresent(row, new int[]{baseExcelColumn, baseExcelColumn+1, baseExcelColumn+2},
                "Error creating Order Exam Constraint.");
        Exam exam1 = dataHandler.getExamById((int) row.getCell(baseExcelColumn).getNumericCellValue());
        Exam exam2 = dataHandler.getExamById((int) (row.getCell(baseExcelColumn + 1).getNumericCellValue()));
        UserConstraint uc = new OrderExamsConstraint(exam1, exam2);
        checkIfMustBeHard(uc, row, baseExcelColumn + 2);
        return uc;
    }

    @Override
    public void writeConstriction(Constraint con, Row row, int baseExcelColumn) {
        OrderExamsConstraint oec = (OrderExamsConstraint) con;
        int cellCounter = baseExcelColumn -1;
        Cell cell = row.createCell(++cellCounter);
        cell.setCellValue(oec.getFirst().getId());

        cell = row.createCell(++cellCounter);
        cell.setCellValue(oec.getSecond().getId());

        cellCounter = writeCommonThings(row, cellCounter, oec.wasHardified(), oec.getLastEvaluation());

        cell = row.createCell(++cellCounter);
        cell.setCellValue(oec.getFirst().getTextualIdentifier());

        cell = row.createCell(++cellCounter);
        cell.setCellValue(oec.getSecond().getTextualIdentifier());
    }
}
