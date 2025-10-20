package org.jb.invoicereader.Database;

import org.jb.invoicereader.ConexosAPI;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.sql.SQLException;

public class UpdateDB {
    private final DbHandler db;

    public UpdateDB() throws SQLException, IOException, InterruptedException {
        db = DbHandler.getInstance();

        if (checkIfDbExists() == false || db.isTableEmpty("pessoas") || db.isTableEmpty("despesas")) {
            InitDB initialiazer = new InitDB();
        }
        updatePessoasTable();
    }

    private boolean checkIfDbExists() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(Paths.get("src/main/resources/config.json").toFile());
        File file = new File("./" + String.valueOf(data.get("database").get("filename")).replaceAll("\"", ""));
        return file.exists();
    }

    private void updatePessoasTable() throws IOException, InterruptedException, SQLException {
        ConexosAPI conexos = ConexosAPI.getInstance();
        boolean finished = false;
        while (finished == false) {
            finished = true;
            String last = db.getLastPessoa();
            System.out.println(last);
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
                    "    \"vldValido#EQ\":\"1\",\n" +
                    "    \"pesCod#GT\":\"" + last + "\"\n" +
                    "},\n" +
                    "\"pageNumber\":1,\n" +
                    "\"pageSize\":100,\n" +
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
            System.out.println(response.body());
            JsonNode rows = json.get("rows");
            for (JsonNode row : rows) {
                System.out.println(row);
                String pesCod = String.valueOf(row.get("pesCod"));
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
}
