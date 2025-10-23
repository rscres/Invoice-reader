package org.jb.invoicereader;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CreateExpense {
    private ConexosAPI conexosApi = ConexosAPI.getInstance();

    public CreateExpense(ObjectNode data) throws IOException, InterruptedException {
        System.out.println(data);
//        String pgsCodSeq = addSP(data);
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
        String pgsCodSeq = "3";
        System.out.println("{\n" +
            "    \"priCod\": " + data.get("priCod").asString() + ",\n" +
                    "    \"docVldTipo\": 9,\n" +
                    "    \"gcdCod\": 80,\n" +
                    "    \"frontModelName\": \"gerConfiguraDoc\",\n" +
                    "    \"pesCod\": " + data.get("COD_PESSOA_CEDENTE") + ",\n" +
                    "    \"endCod\": " + data.get("endCod") + ",\n" +
                    "    \"gcdDesNome\": \"DESPESAS RECIBO IMPORTAÇÃO - PRÓPRIO\",\n" +
                    "    \"docDtaEntrada\": " + LocalDate.now().atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant().toEpochMilli() + ",\n" +
                    "    \"dtaEmissao\": " + convertDate(data.get("EMISSAO").asString()) + ",\n" +
                    "    \"dtaValidade\": " + convertDate(data.get("VENCIMENTO").asString()) + ",\n" +
                    "    \"pdcDocFederal\": \"" + data.get("CNPJ").asString().replaceAll("[^a-zA-Z0-9]", "") + "\",\n" +
                    "    \"items\": [\n" +
                    "        {\n" +
                    "            \"pgsCodSeq\": " + pgsCodSeq + "\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}");
    }

    private void checkMandatoryFields(ObjectNode data) {
        try {
            data.get("VALOR_TOTAL").asString();
            data.get("priCod").asString();
            data.get("COD_PESSOA_CEDENTE");
            data.get("endCod");
            data.get("CNPJ").asString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String addSP(ObjectNode data) throws IOException, InterruptedException {
        HttpResponse<String> response = conexosApi.PostRequest("imp021/processoSp", "{\n" +
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
        if (response.statusCode() != 200)
            throw new RuntimeException(response.body());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseData = mapper.readTree(response.body());

        return responseData.get("pgsCodSeq").asString();
    }

    private long convertDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println(date);
        LocalDate localDate = LocalDate.parse(date, formatter);
        long epoch = localDate.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant().toEpochMilli();
        System.out.println(epoch);
        return epoch;
    }

    private boolean generateSP(String pgsCodSeq, ObjectNode data) throws IOException, InterruptedException {
        HttpResponse<String> response = conexosApi.PostRequest("imp021/processoSp/btConfirmarGerarSp", "{\n" +
                "    \"priCod\": " + data.get("priCod").asString() + ",\n" +
                "    \"docVldTipo\": 9,\n" +
                "    \"gcdCod\": 80,\n" +
                "    \"frontModelName\": \"gerConfiguraDoc\",\n" +
                "    \"pesCod\": " + data.get("COD_PESSOA_CEDENTE").asString() + ",\n" +
                "    \"endCod\": " + data.get("endCod").asString() + ",\n" +
                "    \"gcdDesNome\": \"DESPESAS RECIBO IMPORTAÇÃO - PRÓPRIO\",\n" +
                "    \"docDtaEntrada\": " + Instant.now().toEpochMilli() + ",\n" +
                "    \"dtaEmissao\": " + convertDate(data.get("EMISSAO").asString()) + ",\n" +
                "    \"dtaValidade\": " + convertDate(data.get("VENCIMENTO").asString()) + ",\n" +
                "    \"pdcDocFederal\": \"" + data.get("CNPJ").asString().replaceAll("[^a-zA-Z0-9]", "") + "\",\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"pgsCodSeq\": " + pgsCodSeq + "\n" +
                "        }\n" +
                "    ]\n" +
                "}");
        return true;
    }

    private boolean getAddressCode() {
        return true;
    }
}
