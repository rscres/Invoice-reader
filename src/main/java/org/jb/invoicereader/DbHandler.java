package org.jb.invoicereader;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public enum DbHandler {
    INSTANCE;

    final String url;
    final Connection conn;

    DbHandler() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(Paths.get("src/main/resources/config.json").toFile());
        url = String.valueOf(data.get("database").get("url")).replaceAll("\"", "");
        try {
            Class.forName("org.sqlite.JDBC");
            conn = connectDB();
            createTable("pessoas",
                    "pesCod",
                    "descricao",
                    "endCod",
                    "docFederal",
                    "dtaValidade");
            createTable("despesas",
                    "ctpCod",
                    "descricao",
                    "prjCod");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static DbHandler getInstance() {
        return INSTANCE;
    }

    private Connection connectDB() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        if (conn != null) {
            var meta = conn.getMetaData();
            System.out.println("The driver name is " + meta.getDriverName());
            System.out.println("A new database has been created.");
        }
        return conn;
    }

    private void createTable(String tableName, String primaryKey, String... columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + primaryKey + " TEXT PRIMARY KEY DEFAULT '',");

        for (String column : columns) {
            sql.append(column).append(" TEXT DEFAULT '',");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        Statement stmt = conn.createStatement();
        stmt.execute(String.valueOf(sql));
    }

    public String getInfoPessoa() {
        return null;
    }
}
