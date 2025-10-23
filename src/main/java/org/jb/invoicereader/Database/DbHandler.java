package org.jb.invoicereader.Database;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

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

    public void createTable(String tableName, String keyType, String primaryKey, String... columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + primaryKey + " " + keyType + " PRIMARY KEY DEFAULT '',");

        for (String column : columns) {
            sql.append(column).append(" TEXT DEFAULT '',");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        Statement stmt = conn.createStatement();
        stmt.execute(String.valueOf(sql));
    }

    private void updatePessoasEntry(String key, String column, String data) throws SQLException {
        String sql = "UPDATE pessoas SET ? = ? WHERE pesCod = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, column);
        pstmt.setString(2, data);
        pstmt.setString(3, key);
        pstmt.executeUpdate();
    }

    private void updateDespesasEntry() {

    }

    public String[] getDespesaData(String code) {
        return null;
    }

    public void setPessoaRow(String key, String... data) throws SQLException {
        String sql = "INSERT OR IGNORE INTO pessoas (pesCod, descricao, endCod, docFederal, dtaValidade) VALUES (?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, key);
        pstmt.setString(2, data[0]);
        pstmt.setString(3, data[1]);
        pstmt.setString(4, data[2]);
        pstmt.setString(5, data[3]);
        pstmt.executeUpdate();
    }

    boolean checkIfPessoaExists(String pesCod) throws SQLException {
        String sql = "SELECT pesCod FROM pessoas WHERE pesCod = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, pesCod);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    public String getPesCod(String cnpj) throws SQLException {
        String sql = "SELECT pesCod from pessoas WHERE docFederal LIKE ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        System.out.println(pstmt);
        pstmt.setString(1, "%" + cnpj + "%");
        System.out.println(pstmt);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getString("pesCod");
        }
        return null;
    }

    public boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData dbm = conn.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        return tables.next();
    }

    public boolean isTableEmpty(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("COUNT(*)") == 0;
        }
        return false;
    }

    public String getLastPessoa() throws SQLException {
        String sql = "SELECT CAST(pesCod AS INTEGER) as last\n" +
                "FROM pessoas\n" +
                "ORDER BY last DESC\n" +
                "LIMIT 1;";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            return rs.getString("last");
        }
        return null;
    }

    public void setDespesaRow(Integer key, String... data) throws SQLException {
        String sql = "INSERT OR IGNORE INTO despesas (id, ctpEspConta, ctpCod, descricao, prjCod) VALUES (?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, key);
        pstmt.setString(2, data[0]);
        pstmt.setString(3, data[1]);
        pstmt.setString(4, data[2]);
        pstmt.setString(5, data[3]);
        pstmt.executeUpdate();
    }

    public ArrayList<String[]> getDespesas() throws SQLException {
        String sql = "SELECT prjCod, ctpCod, descricao FROM despesas";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ArrayList<String[]> despesas = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[3];
            row[0] = rs.getString("descricao");
            row[1] = rs.getString("prjCod");
            row[2] = rs.getString("ctpCod");
            despesas.add(row);
        }
        return despesas;
    }

    public String getEndCod(String pesCod) throws SQLException {
        String sql = "SELECT endCod FROM pessoas WHERE pesCod = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, pesCod);
        ResultSet rs = pstmt.executeQuery();
        return rs.getString("endCod");
    }
}
