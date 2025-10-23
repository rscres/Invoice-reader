package org.jb.invoicereader;

import org.jb.invoicereader.Database.DbHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CreateExpense {
    private final ConexosAPI conexosApi = ConexosAPI.getInstance();

    public CreateExpense(ObjectNode data) throws Exception {
        DbHandler db = DbHandler.getInstance();
        String endCod = db.getEndCod(data.get("COD_PESSOA_CEDENTE").asString());
        System.out.println(data);
        if (endCod == null) throw new RuntimeException("Não foi possivel achar o endereço do cedente");
        data.put("endCod", endCod);

        checkMandatoryFields(data);
        setEmptyDates(data);

        System.out.println("here2");
        System.out.println("{\n" +
                "    \"priCod\": " + data.get("priCod").asString() + ",\n" +
                "    \"prjCod\": " + data.get("prjCod").asString() + ",\n" +
                "    \"ctpCod\": " + data.get("ctpCod").asString() + ",\n" +
                "    \"vldStatus\": 2,\n" +
                "    \"pgsMnyMneg\": " + data.get("VALOR_TOTAL").asString() + ",\n" +
                "    \"pgsMnyMnac\": " + data.get("VALOR_TOTAL").asString() + ",\n" +
                "    \"moeCod\": 790,\n" +
                "    \"pgsFltTaxa\": 1,\n" +
                "    \"pgsDtaVcto\": " + convertDate(data.get("VENCIMENTO").asString()) + "\n" +
                "}");

//        String pgsCodSeq = addSP(data);

        String pgsCodSeq = "3";
        System.out.println("{\n" +
            "    \"priCod\": " + data.get("priCod").asString() + ",\n" +
            "    \"docVldTipo\": 9,\n" +
            "    \"gcdCod\": 80,\n" +
            "    \"espNumero\": \"" + data.get("NUM_DOCUMENTO").asString() + "\",\n" +
            "    \"frontModelName\": \"gerConfiguraDoc\",\n" +
            "    \"pesCod\": " + data.get("COD_PESSOA_CEDENTE") + ",\n" +
            "    \"endCod\": " + data.get("endCod") + ",\n" +
            "    \"gcdDesNome\": \"DESPESAS RECIBO IMPORTAÇÃO - PRÓPRIO\",\n" +
            "    \"docDtaEntrada\": " + LocalDate.now().atStartOfDay(ZoneId.of("GMT")).toInstant().toEpochMilli() + ",\n" +
            "    \"dtaEmissao\": " + convertDate(data.get("EMISSAO").asString()) + ",\n" +
            "    \"dtaValidade\": " + convertDate(data.get("VENCIMENTO").asString()) + ",\n" +
            "    \"pdcDocFederal\": \"" + data.get("CNPJ").asString().replaceAll("[^a-zA-Z0-9]", "") + "\",\n" +
            "    \"items\": [\n" +
            "        {\n" +
            "            \"pgsCodSeq\": " + pgsCodSeq + "\n" +
            "        }\n" +
            "    ]\n" +
            "}");

//        generateSP(pgsCodSeq, data);
    }

    private void setEmptyDates(ObjectNode data) {
        LocalDate date = LocalDate.ofEpochDay(LocalDate.now().atStartOfDay(ZoneId.of("GMT")).toInstant().toEpochMilli());
        if (data.get("VENCIMENTO") == null) data.put("VENCIMENTO", String.valueOf(date));
        if (data.get("EMISSAO") == null) data.put("EMISSAO", String.valueOf(date));
    }

    private void checkMandatoryFields(ObjectNode data) throws Exception {
        if (data.get("VALOR_TOTAL") == null || data.get("priCod") == null ||
            data.get("COD_PESSOA_CEDENTE") == null || data.get("endCod") == null ||
            data.get("CNPJ") == null)
            throw new IOException("Campos obrigatórios não preenchidos");
    }

    private String addSP(ObjectNode data) throws IOException, InterruptedException {
        String valor = data.get("VALOR_TOTAL").asString().replace(",", ".");
        HttpResponse<String> response = conexosApi.PostRequest("imp021/processoSp", "{\n" +
                "    \"priCod\": " + data.get("priCod").asString() + ",\n" +
                "    \"prjCod\": " + data.get("prjCod").asString() + ",\n" +
                "    \"ctpCod\": " + data.get("ctpCod").asString() + ",\n" +
                "    \"vldStatus\": 2,\n" +
                "    \"pgsMnyMneg\": " + valor + ",\n" +
                "    \"pgsMnyMnac\": " + valor + ",\n" +
                "    \"moeCod\": 790,\n" +
                "    \"pgsFltTaxa\": 1,\n" +
                "    \"pgsDtaVcto\": " + convertDate(data.get("VENCIMENTO").asString()) + "\n" +
                "}");
        System.out.println(response.body());
        if (response.statusCode() != 200)
            throw new RuntimeException(response.body());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseData = mapper.readTree(response.body());

        return responseData.get("pgsCodSeq").asString();
    }

    private long convertDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.atStartOfDay(ZoneId.of("GMT")).toInstant().toEpochMilli();
    }

    private void generateSP(String pgsCodSeq, ObjectNode data) throws IOException, InterruptedException {
        HttpResponse<String> response = conexosApi.PostRequest("imp021/processoSp/btConfirmarGerarSp", "{\n" +
                "    \"priCod\": " + data.get("priCod").asString() + ",\n" +
                "    \"filCod\": 1," +
                "    \"docVldTipo\": 9,\n" +
                "    \"gcdCod\": 80,\n" +
                "    \"frontModelName\": \"gerConfiguraDoc\",\n" +
                "    \"pesCod\": " + data.get("COD_PESSOA_CEDENTE").asString() + ",\n" +
                "    \"endCod\": " + data.get("endCod").asString() + ",\n" +
                "    \"gcdDesNome\": \"DESPESAS RECIBO IMPORTAÇÃO - PRÓPRIO\",\n" +
                "    \"docDtaEntrada\": " + LocalDate.now().atStartOfDay(ZoneId.of("GMT")).toInstant().toEpochMilli() + ",\n" +
                "    \"dtaEmissao\": " + convertDate(data.get("EMISSAO").asString()) + ",\n" +
                "    \"dtaValidade\": " + convertDate(data.get("VENCIMENTO").asString()) + ",\n" +
                "    \"pdcDocFederal\": \"" + data.get("CNPJ").asString().replaceAll("[^a-zA-Z0-9]", "") + "\",\n" +
                "    \"espNumero\": \"" + data.get("NUM_DOCUMENTO").asString() + "\",\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"pgsCodSeq\": " + pgsCodSeq + "\n" +
                "        }\n" +
                "    ]\n" +
                "}");
        if (response.statusCode() != 200) throw new RuntimeException(response.body());
        System.out.println(response.body());
    }
}
