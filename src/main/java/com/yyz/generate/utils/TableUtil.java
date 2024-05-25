package com.yyz.generate.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import com.yyz.generate.pojo.TableFiled;
import com.yyz.generate.pojo.TableInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * main方法启动，不需要启动整个项目
 */
public class TableUtil {
    public static String GEN_FILE_PATH = "D:\\Downloads\\";
    private static String dbHost = "127.0.0.1";
    private static int dbPort = 3306;
    private static String userName = "root";
    private static String password = "mysql";
    private static String dbName = "test_db";

    public static void main(String[] args) throws SQLException {
        DataSource ds = getDataSource();
        table2Word(ds, dbName, dbName + "三线表.doc");
    }


    /**
     * 连接数据库
     * @return
     */
    private static DataSource getDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false", dbHost, dbPort, dbName));
        datasource.setUsername(userName);
        datasource.setPassword(password);
        datasource.setDriverClassName("com.mysql.jdbc.Driver");
        datasource.setInitialSize(1);
        datasource.setMinIdle(1);
        datasource.setMaxActive(3);
        datasource.setMaxWait(60000);
        return datasource;
    }


    /**
     * 生成word文档
     *
     * @param ds：数据源
     * @param fileName：生成文件地址
     * @return: void
     */
    public static void table2Word(DataSource ds, String databaseName, String fileName) throws SQLException {
        List<TableInfo> tables = getTableInfos(ds, databaseName);
        Document document = new Document(PageSize.A4);
        try {
            File dir = new File(GEN_FILE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fileName = GEN_FILE_PATH + File.separator + fileName;
            File file = new File(fileName);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            file.createNewFile();

            // 写入文件信息
            RtfWriter2.getInstance(document, new FileOutputStream(fileName));
            document.open();

//            gebTableInfoDesc(document, tables);
            genTableStructDesc(document, tables, ds);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        printMsg("所有表【共%d个】已经处理完成", tables.size());
    }

    private static void gebTableInfoDesc(Document document, List<TableInfo> tables) throws DocumentException {
        Paragraph ph = new Paragraph();
        Paragraph p = new Paragraph("表清单描述", new Font(Font.TIMES_ROMAN, 24, Font.NORMAL, new Color(0, 0, 0)));
        p.setAlignment(Element.ALIGN_LEFT);
        document.add(p);

        printMsg("产生表清单开始");
        Table table = new Table(2);
        int[] widths = new int[]{500, 900};
        table.setWidths(widths);
        table.setBorderWidth(1);
        table.setPadding(0);
        table.setSpacing(0);

        //添加表头行
        Cell headerCell = new Cell("表名");
        headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell.setBackgroundColor(new Color(192, 192, 192));
        table.addCell(headerCell);

        headerCell = new Cell("表描述");
        headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell.setBackgroundColor(new Color(192, 192, 192));
        table.addCell(headerCell);
        table.endHeaders();

        for (TableInfo tableInfo : tables) {
            addCell(table, tableInfo.getTblName());
            addCell(table, tableInfo.getTblComment());
        }
        document.add(table);
        printMsg("产生表清单结束");
    }

    private static void genTableStructDesc(Document document, List<TableInfo> tables, DataSource ds) throws DocumentException, SQLException, IOException {

        Paragraph p = new Paragraph("表结构描述", new Font(Font.TIMES_ROMAN, 18, Font.NORMAL, new Color(0, 0, 0)));
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);


        printMsg("共需要处理%d个表", tables.size());
        int colNum = 9;
        //循环处理每一张表
        for (int i = 0; i < tables.size(); i++) {
            TableInfo tableInfo = tables.get(i);
            String tblName = tableInfo.getTblName();
            String tblComment = tableInfo.getTblComment();


            printMsg("处理%s表开始", tableInfo);
            //写入表说明
//                String tblTile = "" + (i + 1) + " 表名称:" + tblName + "（" + tblComment + "）";
//                Paragraph paragraph = new Paragraph(tblTile);
//                document.add(paragraph);

            List<TableFiled> fileds = getTableFields(ds, tables.get(i).getTblName());
            Table table = new Table(colNum);
            int[] widths = new int[]{160, 250, 350, 160, 80, 80, 160, 80, 80};
            table.setWidths(widths);
//            table.setBorderWidth(1);
            table.setPadding(0);
            table.setSpacing(0);


//            添加表名行
            String tblInfo = StringUtils.isBlank(tblComment) ? tblName : String.format("%s(%s)", tblName, tblComment);
//            Cell headerCell = new Cell(tblInfo);
//
//            headerCell.disableBorderSide(15);
//
//
//            headerCell.setColspan(colNum);
//            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            table.addCell(headerCell);
            Paragraph ph = new Paragraph(tblInfo, new Font(Font.TIMES_ROMAN, 14, Font.NORMAL, new Color(0, 0, 0)));
            ph.setAlignment(Element.ALIGN_CENTER);
            document.add(ph);


            BaseFont bfComic0 = BaseFont.createFont("C:\\Windows\\Fonts\\simsunb.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            Font font = new Font(bfComic0, 10.5f);

            //添加表头行
            addCell(table, "字段名", 0, font);
            addCell(table, "字段描述", 0, font);
            addCell(table, "数据类型", 0, font);
            addCell(table, "长度", 0, font);
            addCell(table, "为空", 0, font);
            addCell(table, "是否主键", 0, font);
            addCell(table, "约束", 0, font);
            addCell(table, "缺省值", 0, font);
            addCell(table, "备注", 0, font);
            table.endHeaders();

            int k;
            // 表格的主体
            for (k = 0; k < fileds.size() - 1; k++) {
                TableFiled field = fileds.get(k);
                addCell(table, field.getField());
                addCell(table, field.getComment(), font);
                addCell(table, field.getType());
                addCell(table, field.getLength());
                addCell(table, field.isNull() ? "是" : "否", font);
                addCell(table, field.getKey().equals("PRI") ? "是" : "否", font);
                addCell(table, "", font); // 约束
                addCell(table, field.getDefaultVal()); //缺省值
                addCell(table, field.getExtra());
            }


            /**
             * 生成表格，最后一行
             */
            if (k == fileds.size() - 1) {
                TableFiled field = fileds.get(k);

                addCell(table, field.getField(), 1);
                addCell(table, field.getComment(), 1, font);
                addCell(table, field.getType(), 1);
                addCell(table, field.getLength(), 1);
                addCell(table, field.isNull() ? "是" : "否", 1, font);
                addCell(table, field.getKey().equals("PRI") ? "是" : "否", 1, font);
                addCell(table, "", 1, font); //约束
                addCell(table, field.getDefaultVal(), 1); //缺省值
                addCell(table, field.getExtra(), 1);
            }

            table.setBorder(2);
            table.setBorderWidth(15f);

            document.add(table);
            printMsg("处理%s表结束", tableInfo);
        }
    }

    private static void addCell(Table table, String content, int flag) {
        addCell(table, content, -1, Element.ALIGN_CENTER, flag);
    }

    private static void addCell(Table table, String content, int flag, Font font) {
        addCell(table, content, -1, Element.ALIGN_CENTER, flag, font);
    }

    private static void addCell(Table table, String content, Font font) {
        addCell(table, content, -1, Element.ALIGN_CENTER, font);
    }

    private static void addCell(Table table, String content) {
        addCell(table, content, -1, Element.ALIGN_CENTER);
    }

    /**
     * 添加表头到表格
     *
     * @param table
     * @param content
     * @param width
     * @param align
     */
    private static void addCell(Table table, String content, int width, int align, Font font) {
// Font font = new Font(Font.TIMES_ROMAN, 5, Font.BOLD);
        try {
            Cell cell = new Cell(new Paragraph(content, font));
            if (width > 0)
                cell.setWidth(width);
            cell.setHorizontalAlignment(align);
            cell.disableBorderSide(15);
            table.addCell(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCell(Table table, String content, int width, int align) {
        Font font = new Font(Font.TIMES_ROMAN, 10.5f, Font.NORMAL);
        try {
            Cell cell = new Cell(new Paragraph(content, font));
            if (width > 0)
                cell.setWidth(width);
            cell.setHorizontalAlignment(align);
            cell.disableBorderSide(15);
            table.addCell(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param table
     * @param content
     * @param width
     * @param align
     */
    private static void addCell(Table table, String content, int width, int align, int flag) {
        try {
            Font font = new Font(Font.TIMES_ROMAN, 10.5f, Font.NORMAL);
            Cell cell = new Cell(new Paragraph(content, font));
            if (width > 0)
                cell.setWidth(width);
            cell.setHorizontalAlignment(align);
            //0---header,有上下边界,1----有下边界
            if (flag == 0) {
                cell.disableBorderSide(12);
                cell.setBorderColorTop(new Color(0, 0, 0));
                cell.setBorderWidthTop(3f);
                cell.setBorderColorBottom(new Color(0, 0, 0));
                cell.setBorderWidthBottom(3f);
//            cell.Border = Rectangle.RIGHT_BORDER | Rectangle.TOP_BORDER | Rectangle.BOTTOM_BORDER;
//            cell.setBorderWidth(3f);
//            cell.setBackgroundColor(new Color(192, 192, 192));
            } else {
                cell.disableBorderSide(13);
                cell.setBorderColorBottom(new Color(0, 0, 0));
                cell.setBorderWidthBottom(3f);
            }
            table.addCell(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCell(Table table, String content, int width, int align, int flag, Font font) {
        try {
            Cell cell = new Cell(new Paragraph(content, font));
            if (width > 0)
                cell.setWidth(width);
            cell.setHorizontalAlignment(align);
            //0---header,有上下边界,1----有下边界
            if (flag == 0) {
                cell.disableBorderSide(12);
                cell.setBorderColorTop(new Color(0, 0, 0));
                cell.setBorderWidthTop(3f);
                cell.setBorderColorBottom(new Color(0, 0, 0));
                cell.setBorderWidthBottom(3f);
//            cell.Border = Rectangle.RIGHT_BORDER | Rectangle.TOP_BORDER | Rectangle.BOTTOM_BORDER;
//            cell.setBorderWidth(3f);
//            cell.setBackgroundColor(new Color(192, 192, 192));
            } else {
                cell.disableBorderSide(13);
                cell.setBorderColorBottom(new Color(0, 0, 0));
                cell.setBorderWidthBottom(3f);
            }
            table.addCell(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printMsg(String format, Object... args) {
        System.out.println(String.format(format, args));
    }

    private static List<TableInfo> getTableInfos(DataSource ds, String databaseName) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<TableInfo> list = new ArrayList();
        try {
            conn = ds.getConnection();
            String sql = "select TABLE_NAME,TABLE_TYPE,TABLE_COMMENT from information_schema.tables where table_schema =? order by table_name";

            stmt = conn.prepareStatement(sql);
            setParameters(stmt, Arrays.<Object>asList(databaseName));

            rs = stmt.executeQuery();
            ResultSetMetaData rsMeta = rs.getMetaData();

            while (rs.next()) {
                TableInfo row = new TableInfo();
                row.setTblName(rs.getString(1));
                row.setTblType(rs.getString(2));
                row.setTblComment(rs.getString(3));
                list.add(row);
            }
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
            JdbcUtils.close(conn);
        }
        return list;
    }

    private static List<TableFiled> getTableFields(DataSource ds, String tblName) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
//        List<TableFiled> list = Lists.newArrayList();
        List<TableFiled> list = new ArrayList();
        try {
            conn = ds.getConnection();
            //返回的列顺序是: Field,Type,Collation,Null,Key,Default,Extra,Privileges,Comment
            String sql = "SHOW FULL FIELDS FROM " + tblName;
            //返回的列顺序是: Field,Type,Null,Key,Default,Extra
//            sql = "show columns FROM " + tblName;

            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();
            ResultSetMetaData rsMeta = rs.getMetaData();

            while (rs.next()) {
                TableFiled field = new TableFiled();
                field.setField(rs.getString(1));
                String type = rs.getString(2);
                String length = "";
                if (type.contains("(")) {
                    int idx = type.indexOf("(");
                    length = type.substring(idx + 1, type.length() - 1);
                    type = type.substring(0, idx);
                }
                field.setType(type);
                field.setLength(length);
                field.setNull(rs.getString(4).equalsIgnoreCase("YES") ? true : false);
                field.setKey(rs.getString(5));
                field.setDefaultVal(rs.getString(6));
                field.setExtra(rs.getString(7));
                field.setComment(rs.getString(9));
                list.add(field);
            }
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
            JdbcUtils.close(conn);
        }
        return list;
    }

    private static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0, size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            stmt.setObject(i + 1, param);
        }
    }

}
