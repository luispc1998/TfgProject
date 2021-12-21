package domain.parsers.constrictionsParserTools;

import domain.constrictions.types.weakConstriction.hardifiableConstrictions.UserConstriction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * This is just to group some common functionality to all of the ConstrictionParserTools.
 */
public abstract class AbstractCosntrictionParserTool implements ConstrictionParserTool {

    /**
     * Description for the type of Constriction in the excel.
     */
    private String description;

    /**
     * Headers for the type of Constriction in the excel.
     */
    private String[] headers;

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getHeaders() {
        return headers;
    }

    protected void checkIfHard(UserConstriction uc, Row row, int i) {
        if (row.getCell(i).getBooleanCellValue()) {
            uc.hardify();
        }
    }

    /**
     * Generalizes the writing of common fields to all constrictions
     * @param row The row in which the cells will be
     * @param cellCounter The last cell index that was written.
     * @param hard Whether the constriction was considered hard or not
     * @param lastEvaluation Whether the constriction was met or not.
     * @return The last cell index that was written.
     */
    public int writeCommonThings(Row row, int cellCounter, boolean hard, boolean lastEvaluation) {
        Cell cell = row.createCell(++cellCounter);
        cell.setCellValue(hard);

        cell = row.createCell(++cellCounter);
        cell.setCellValue(lastEvaluation);

        return cellCounter;
    }
}
