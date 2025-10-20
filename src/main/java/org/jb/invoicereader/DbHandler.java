package org.jb.invoicereader;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.sql.*;

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
            populatePessoasTable();
        } catch (ClassNotFoundException | SQLException | IOException | InterruptedException e) {
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

    private void populatePessoasTable() throws IOException, InterruptedException, SQLException {
        ConexosAPI conexos = ConexosAPI.getInstance();
        System.out.println("populating db");
        boolean finished = false;
        int pageNum = 1;
        while (finished == false) {
            finished = true;
            System.out.println("Page num: " + pageNum);
            HttpResponse<String> response = conexos.PostRequest("cmn025/list", "{\n" +
                    "\"fieldList\":[\n" +
                    "    \"pesCod\",\n" +
                    "    \"dpeCodSeq\",\n" +
                    "    \"dpeNomPessoa\",\n" +
                    "    \"dpeNomFantasia\",\n" +
                    "    \"dpeDtaValidade\",\n" +
                    "    \"vldValido\",\n" +
                    "    \"pesVldStatus\"\n" +
                    "],\n" +
                    "\"filterList\":{\n" +
                    "    \"vldValido#EQ\":\"1\"\n" +
                    "},\n" +
                    "\"pageNumber\":" + pageNum++ + ",\n" +
                    "\"pageSize\":1000,\n" +
                    "\"serviceName\":\"cmn025\",\n" +
                    "\"orderList\":{\n" +
                    "    \"orderList\":[\n" +
                    "        {\n" +
                    "        \"propertyName\":\"pesCod\",\n" +
                    "        \"order\":\"asc\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n" +
                    "}");
            if (response.statusCode() != 200) return;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());
            JsonNode rows = json.get("rows");
            for (JsonNode row : rows) {
                System.out.println(row);
                String pesCod = String.valueOf(row.get("pesCod"));
                if (checkIfPessoaExists(pesCod)) continue;
                finished = false;
                String endCod = getEndCod(pesCod);
                String docFederal = getDocFederal(pesCod);
                String dtaValidade = String.valueOf(row.get("dpeDtaValidade"));
                String descricao = String.valueOf(row.get("dpeNomFantasia"));
                if (pesCod.isEmpty() || endCod == null || docFederal == null
                    || dtaValidade.isEmpty() || descricao.isEmpty()) continue;
                setPessoaRow(pesCod, descricao, endCod, docFederal, dtaValidade);
            }
        }
    }

    private String getEndCod(String pesCod) throws IOException, InterruptedException {
        ConexosAPI conexos = ConexosAPI.getInstance();

        HttpResponse<String> response = conexos.PostRequest("cmn025/endPessoas/list", "{\n" +
                "\"filterList\":{\n" +
                "    \"pesCod#EQ\":\"" + pesCod + "\"\n" +
                "},\n" +
                "\"pageNumber\":1,\n" +
                "\"pageSize\":50,\n" +
                "\"orderList\":{\n" +
                "    \"orderList\":[\n" +
                "        {\n" +
                "        \"propertyName\":\"endCod\",\n" +
                "        \"order\":\"desc\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n" +
                "}");
        if (response.statusCode() != 200) return null;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());
        if (Integer.parseInt(String.valueOf(json.get("count"))) == 0) return "0";
        String endCod = String.valueOf(json.get("rows").get(0).get("endCod"));
        return endCod;
    }

    private String getDocFederal(String pesCod) throws IOException, InterruptedException {
        ConexosAPI conexos = ConexosAPI.getInstance();

        HttpResponse<String> response = conexos.PostRequest("cmn025/endPessoas/list", "{\n" +
                "    \"fieldList\":[\n" +
                "        \"pesCod\",\n" +
                "        \"pdcDocFederal\",\n" +
                "        \"pdcDocFederalFmt\"\n" +
                "    ],\n" +
                "    \"filterList\":{\n" +
                "        \"pesCod#EQ\":\"" + pesCod + "\"\n" +
                "    },\n" +
                "    \"pageNumber\":1,\n" +
                "    \"pageSize\":50,\n" +
                "    \"serviceName\":\"cmn025\",\n" +
                "    \"orderList\":{\n" +
                "        \"orderList\":[\n" +
                "            {\n" +
                "            \"propertyName\":\"pesCod\",\n" +
                "            \"order\":\"desc\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}");
        if (response.statusCode() != 200) return null;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());
        if (Integer.parseInt(String.valueOf(json.get("count"))) == 0) return "N/A";
        String docFederal = String.valueOf(json.get("rows").get(0).get("pdcDocFederal")).replaceAll("\"", "");
        return docFederal;
    }

    private void updatePessoasTable(String key, String column, String data) throws SQLException {
        String sql = "UPDATE pessoas SET ? = ? WHERE pesCod = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, column);
        pstmt.setString(2, data);
        pstmt.setString(3, key);
        pstmt.executeUpdate();
    }

    private void updateDespesasTable() {

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

    private boolean checkIfPessoaExists(String pesCod) throws SQLException {
        String sql = "SELECT pesCod FROM pessoas WHERE pesCod = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, pesCod);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }
}
