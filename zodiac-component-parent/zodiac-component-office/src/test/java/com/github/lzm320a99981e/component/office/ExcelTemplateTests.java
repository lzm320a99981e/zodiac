package com.github.lzm320a99981e.component.office;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.lzm320a99981e.component.office.excel.*;
import com.github.lzm320a99981e.component.office.excel.metadata.ExcelTable;
import com.github.lzm320a99981e.component.office.excel.metadata.Point;
import com.github.lzm320a99981e.component.office.excel.metadata.Table;
import org.apache.poi.ss.usermodel.*;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExcelTemplateTests {
    static final String basePath = System.getProperty("user.dir");
    static final File resources = Paths.get(basePath, "src/test/resources").toFile();

    @Test
    public void test() throws Exception {
        final Workbook workbook = WorkbookFactory.create(new File(resources, "test-writer-template.xlsx"));
        System.out.println(workbook);
        final Sheet sheet = workbook.getSheetAt(0);
        final int lastRowNum = sheet.getLastRowNum();
        for (int i = 0; i < lastRowNum; i++) {
            final Row row = sheet.getRow(i);
            // 判断是否是模板行

            final short lastCellNum = row.getLastCellNum();
            for (int j = 0; j < lastCellNum; j++) {
                row.getCell(j).removeCellComment();
            }
        }
    }

    @Test
    public void test2() throws Exception {
        final Workbook workbook = WorkbookFactory.create(new File(resources, "test-writer-template.xlsx"));
        final RichTextString richTextString = workbook.getSheetAt(0).getRow(11).getCell(0).getRichStringCellValue();
        final int runs = richTextString.numFormattingRuns();
        System.out.println(runs);
    }

    private boolean isTableRow(Row row) {
        final Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            final Cell cell = cellIterator.next();
            final String value = cell.getStringCellValue();
            final RichTextString richStringCellValue = cell.getRichStringCellValue();


        }
        return false;
    }

    @Test
    public void testMatchTemplateVar() {
        String var = "姓名：{name}，年龄：{{age}}岁，省份证号：{goods}";
        final Pattern pattern = Pattern.compile("\\{[^{}]+}");
        final Matcher matcher = pattern.matcher(var);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    @Test
    public void testFrequency() {
        List<Integer> numbers = Arrays.asList(new Integer[]{1, 2, 1, 3, 4, 4});
        numbers.stream().filter(i -> Collections.frequency(numbers, i) > 1)
                .collect(Collectors.toSet()).forEach(System.out::println);
    }

    @Test
    public void testWriter() throws Exception {
        Table table = Table.create(0, 1);
        table.setSize(5);
        Integer sheetIndex = table.getSheetIndex();
        Integer startRow = table.getStartRowNumber();
        // 姓名	年龄	性别	出生
        table.setColumns(
                Arrays.asList(
                        Point.create(sheetIndex, startRow, 0, "name"),
                        Point.create(sheetIndex, startRow, 2, "age"),
                        Point.create(sheetIndex, startRow, 3, "birth")
                )
        );

        Table table1 = Table.create(0, 7);
        table1.setSize(3);
        Integer sheetIndex1 = table.getSheetIndex();
        Integer startRow1 = table.getStartRowNumber();
        // 姓名	年龄	性别	出生
        table1.setColumns(
                Arrays.asList(
                        Point.create(sheetIndex1, startRow1, 0, "name"),
                        Point.create(sheetIndex1, startRow1, 2, "age"),
                        Point.create(sheetIndex1, startRow1, 3, "birth")
                )
        );

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            JSONObject rowData = new JSONObject();
            rowData.put("name", "zhangsan");
            rowData.put("age", "zhangsan" + i);
            rowData.put("birth", "zhangsan" + i);
            data.add(rowData);
        }
        File template = new File(resources, "test-writer-template.xlsx");
        ExcelWriter.create()
                .addTable(table, data, TableCellMergeRangesStrategy.REPEAT_COLUMN)
                .addTable(table1, data, TableCellMergeRangesStrategy.REPEAT_ROW)
                .addPoint(Point.create(sheetIndex, 10, 1), "熊逸")
                .addPoint(Point.create(sheetIndex, 11, 1), "熊逸1234")
                .addPoint(Point.create(sheetIndex, 12, 1), "234234234324")
                .write(template, new FileOutputStream(new File(resources, "test-reader.xlsx")));
    }

    @Test
    public void testReader() {
        Table table = Table.create(0, 1, "users");
        table.setSize(50);
        Integer sheetIndex = table.getSheetIndex();
        Integer startRow = table.getStartRowNumber();
        table.setColumns(
                Arrays.asList(
                        Point.create(sheetIndex, startRow, 0, "name"),
                        Point.create(sheetIndex, startRow, 2, "age"),
                        Point.create(sheetIndex, startRow, 3, "birth")
                )
        );

        File file = new File(resources, "test-reader.xlsx");
        Map<String, Object> data = ExcelReader.create()
                .addTable(table)
                .addPoint(Point.create(sheetIndex, 28, 1, "username"))
                .addPoint(Point.create(sheetIndex, 29, 1, "password"))
                .addPoint(Point.create(sheetIndex, 30, 1, "phone"))
                .read(file);
        System.out.println(JSON.toJSONString(data, true));
    }

    @Test
    public void testSimpleReader() {
        File file = new File(resources, "test-reader.xlsx");
        List<User> users = SimpleExcelReader.create().readTable(User.class, file);
        System.out.println(JSON.toJSONString(users, true));
    }

    @Test
    public void testSimpleWriter() {
        File template = new File(resources, "test-writer-template.xlsx");
        final List<User> data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final User user = new User();
            user.setName("张三" + i);
            user.setAge("22");
            user.setSex("男");
            user.setBirth("1990-01-01");
            data.add(user);
        }
        try {
            SimpleExcelWriter.create(template).writeTable(data, new FileOutputStream(new File(resources, "test-reader.xlsx")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testShiftRows() throws Exception {
        File template = new File(resources, "test-writer-template.xlsx");
        FileInputStream input = new FileInputStream(template);
        Workbook workbook = WorkbookFactory.create(input);
        Sheet sheet = workbook.getSheetAt(0);
        sheet.shiftRows(1, sheet.getLastRowNum(), 5, true, false);
        Path output = Paths.get(template.getParentFile().getAbsolutePath(), "test-reader.xlsx");
        FileOutputStream outputStream = new FileOutputStream(output.toFile());

        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        input.close();
    }

    @Test
    public void testMergedRegion() throws Exception {
        File template = new File(resources, "test-writer-template.xlsx");
        FileInputStream input = new FileInputStream(template);
        Workbook workbook = WorkbookFactory.create(input);
        Sheet sheet = workbook.getSheetAt(0);
//        sheet.addMergedRegion()

        sheet.shiftRows(1, sheet.getLastRowNum(), 5, true, false);
        Path output = Paths.get(template.getParentFile().getAbsolutePath(), "test-reader.xlsx");
        FileOutputStream outputStream = new FileOutputStream(output.toFile());

        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        input.close();
    }

    @ExcelTable(limit = 5)
    static class TableAnnoEntity {
        private String a1;
        private String b1;
        private String c1;
        private String a2;
        private String b2;
        private String c2;
    }

    @Test
    public void testFieldOrder() {
        final Field[] fields = TableAnnoEntity.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName());
        }
        // ExcelTable sheetName sheetIndex startRowNumber startColumnNum rowStep columnStep
        // ExcelColumn index
        // ExcelPoint sheetName sheetIndex rowNumber columnNumber
    }

    @Test
    public void testExcelAnnotation() {
        final ExcelTable table = TableAnnoEntity.class.getAnnotation(ExcelTable.class);
        System.out.println(table.limit()[0]);
    }

    @Test
    public void testPicture() throws Exception {
        File excel = new File(resources, "test-image-read.xlsx");
        final Workbook workbook = WorkbookFactory.create(excel);

        // images
        final byte[] images001 = Files.readAllBytes(new File(resources, "images001.jpeg").toPath());
        final int pictureIndex = workbook.addPicture(images001, Workbook.PICTURE_TYPE_JPEG);

        final CreationHelper creationHelper = workbook.getCreationHelper();

        final Sheet sheet = workbook.getSheetAt(0);
        final Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
        // picture position
        final Row row = sheet.getRow(0);
        final Cell cell = row.createCell(0);

        final ClientAnchor clientAnchor = creationHelper.createClientAnchor();
        clientAnchor.setCol1(cell.getColumnIndex());
        clientAnchor.setRow1(row.getRowNum());
        clientAnchor.setCol2(cell.getColumnIndex());
        clientAnchor.setRow2(row.getRowNum());

        final short rowHeight = row.getHeight();
        final int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
        clientAnchor.setDx1(0);
        clientAnchor.setDy1(0);
        clientAnchor.setDx2(rowHeight);
        clientAnchor.setDy2(columnWidth);


//        clientAnchor.setDx1();
//        clientAnchor.setDy1();

        // create picture
        final Picture picture = drawingPatriarch.createPicture(clientAnchor, pictureIndex);
        // resize
//        picture.resize();

        // output
        File output = new File(resources, "test-image-write.xlsx");
        workbook.write(new FileOutputStream(output));
        workbook.close();
    }
}
