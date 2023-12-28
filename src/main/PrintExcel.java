package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import problem.Problem;

public class PrintExcel {
    public static void writeDataToExcel(int numberDeadSensors , int index, Set<Integer> deadDetail, String datatype) {
        try {
            String filePath = "D:\\Demo\\test.xlsx";  // Đường dẫn đến file Excel
            FileInputStream fileInputStream = new FileInputStream(filePath);

            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet worksheet = workbook.getSheetAt(0);

            Row row_0 = worksheet.getRow(0);
            if (row_0 == null) {
                Row rowInit = worksheet.createRow(0);
                rowInit.createCell(0).setCellValue("Datatype");
                rowInit.createCell(1).setCellValue("Index");
                rowInit.createCell(2).setCellValue("Number Sensor Deployed");
                rowInit.createCell(3).setCellValue("Number dead sensors ");
                rowInit.createCell(4).setCellValue("Rate dead (%)");
                rowInit.createCell(5).setCellValue("Detail about dead sensors");
            }
            else {
                row_0.createCell(0).setCellValue("Datatype");
                row_0.createCell(1).setCellValue("Index");
                row_0.createCell(2).setCellValue("Number Sensor Deployed");
                row_0.createCell(3).setCellValue("Number dead sensors ");
                row_0.createCell(4).setCellValue("Rate dead (%)");
                row_0.createCell(5).setCellValue("Detail about dead sensors");
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (Integer element: deadDetail) {
                stringBuilder.append(element).append(",");
            }
            String result = stringBuilder.toString().trim();
            Row row_Index = worksheet.getRow(index);
            if (row_Index == null) {
                Row row = worksheet.createRow(index);
                row.createCell(0).setCellValue(datatype);
                row.createCell(1).setCellValue(index);
                row.createCell(2).setCellValue(Problem.deployedSensors);
                row.createCell(3).setCellValue(numberDeadSensors);
                row.createCell(4).setCellValue((double) 100*deadDetail.size() / Problem.deployedSensors);
                row.createCell(5).setCellValue(result);
            } else {
                row_Index.createCell(0).setCellValue(datatype);
                row_Index.createCell(1).setCellValue(index);
                row_Index.createCell(2).setCellValue(Problem.deployedSensors);
                row_Index.createCell(3).setCellValue(numberDeadSensors);
                row_Index.createCell(4).setCellValue((double) 100*deadDetail.size() / Problem.deployedSensors);
                row_Index.createCell(5).setCellValue(result);
            }

            fileInputStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
            workbook.write(fileOutputStream);

            fileOutputStream.close();
            System.out.println("Data is successfully written to Excel.");
        } catch (IOException e) {
            System.err.println("Error occurred while writing data to Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static  void printNumberDeadSensor(int cycle, int cycleDeadSensors, int test) {
        try {
            String filePath = "D:\\Demo\\test.xlsx"; // Đường dẫn đến file Excel
            FileInputStream fileInputStream = new FileInputStream(filePath);

            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet worksheet = workbook.getSheetAt(0);


            String stringBuilder = "Cycle " + cycle;
            System.out.println("Test "+stringBuilder);
            int cycleColumn = 6+ cycle;
            Row row_0 = worksheet.getRow(0);
            if (row_0 == null ) {
                Row rowInit = worksheet.createRow(0);
                rowInit.createCell(6).setCellValue("Number of dead sensors per cycle");
                rowInit.createCell(cycleColumn).setCellValue(stringBuilder);
            } else {
                row_0.createCell(6).setCellValue("Number of dead sensors per cycle");
                row_0.createCell(cycleColumn).setCellValue(stringBuilder);
            }

            Row row_1 = worksheet.getRow(test);
            if (row_1 == null ) {
                Row row = worksheet.createRow(test);
                row.createCell(cycleColumn).setCellValue(cycleDeadSensors);
            }
            else {
                row_1.createCell(cycleColumn).setCellValue(cycleDeadSensors);
            }


            System.out.println("Cycle value "+cycleColumn);
            System.out.println("cycleDeadSensors "+cycleDeadSensors);
            fileInputStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
            workbook.write(fileOutputStream);

            fileOutputStream.close();
            System.out.println("Cycle detail  is successfully written to Excel.");
        } catch (IOException e) {
            System.err.println("Error occurred while writing data to Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
