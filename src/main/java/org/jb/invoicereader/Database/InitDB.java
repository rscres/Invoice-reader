package org.jb.invoicereader.Database;

import org.jb.invoicereader.ConexosAPI;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;

public class InitDB {
    private final DbHandler db;

    public InitDB() throws SQLException, IOException, InterruptedException {
        db = DbHandler.getInstance();
        db.createTable("pessoas",
                "pesCod",
                "descricao",
                "endCod",
                "docFederal",
                "dtaValidade");
        db.createTable("despesas",
                "ctpCod",
                "descricao",
                "prjCod");
        if (db.isTableEmpty("pessoas")) populatePessoasTable();
        if (db.isTableEmpty("despesas")) populateDespesasTable();
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
                if (db.checkIfPessoaExists(pesCod)) continue;
                finished = false;
                String endCod = getEndCod(pesCod);
                String docFederal = getDocFederal(pesCod);
                String dtaValidade = String.valueOf(row.get("dpeDtaValidade"));
                String descricao = String.valueOf(row.get("dpeNomFantasia"));
                if (pesCod.isEmpty() || endCod == null || docFederal == null
                        || dtaValidade.isEmpty() || descricao.isEmpty()) continue;
                db.setPessoaRow(pesCod, descricao, endCod, docFederal, dtaValidade);
            }
        }
    }

    String getEndCod(String pesCod) throws IOException, InterruptedException {
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

    String getDocFederal(String pesCod) throws IOException, InterruptedException {
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
        System.out.println(response.body());
        if (Integer.parseInt(String.valueOf(json.get("count"))) == 0) return "N/A";
        String docFederal = String.valueOf(json.get("rows").get(0).get("pdcDocFederal")).replaceAll("\"", "");
        return docFederal;
    }

    private void populateDespesasTable() {
        System.out.println("Populate despesas");
    }
}
