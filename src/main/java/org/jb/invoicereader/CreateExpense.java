package org.jb.invoicereader;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CreateExpense {
    private ConexosAPI conexosApi = ConexosAPI.getInstance();

    public CreateExpense(ObjectNode data) throws IOException, InterruptedException {
        System.out.println(data);
        String pgsCodSeq = addSP(data);
        System.out.println(pgsCodSeq);
    }

    private String addSP(ObjectNode data) throws IOException, InterruptedException {
        HttpResponse<String> response = conexosApi.PostRequest("imp021/processoSp", "{\n" +
                "    \"priCod\": " + data.get("priCod") + ",\n" +
                "    \"prjCod\": " + data.get("prjCod") + ",\n" +
                "    \"ctpCod\": " + data.get("ctpCod") + ",\n" +
                "    \"vldStatus\": 2,\n" +
                "    \"pgsMnyMneg\": " + data.get("VALOR_TOTAL") + ",\n" +
                "    \"pgsMnyMnac\": " + data.get("VALOR_TOTAL") + ",\n" +
                "    \"moeCod\": 790,\n" +
                "    \"pgsFltTaxa\": 1,\n" +
                "    \"pgsDtaVcto\": " + convertDate(data.get("VENCIMENTO").asString()) + "\n" +
                "}");
        if (response.statusCode() != 200)
            throw new RuntimeException(response.body());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseData = mapper.readTree(response.body());

        return responseData.get("pgsCodSeq").asString();
//        convertDate(data.get("VENCIMENTO").textValue());
//        return null;
    }

    private long convertDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("\"yyyy-MM-dd\"");
        System.out.println(date);
        LocalDate localDate = LocalDate.parse(date, formatter);
        long epoch = localDate.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant().toEpochMilli();
        System.out.println(epoch);
        return epoch;
    }

//
//    private boolean generateSP() throws IOException, InterruptedException {
//        HttpResponse<String> response = conexosApi.PostRequest("imp021/processoSp/btConfirmarGerarSp", "{\n" +
//                "    \"priCod\": 7844,\n" +
//                "    \"docVldTipo\": 9,\n" +
//                "    \"gcdCod\": 80,\n" +
//                "    \"frontModelName\": \"gerConfiguraDoc\",\n" +
//                "    \"pesCod\": " + pesCod + ",\n" +
//                "    \"endCod\": " + endCod + ",\n" +
//                "    \"gcdDesNome\": \"DESPESAS RECIBO IMPORTAÇÃO - PRÓPRIO\",\n" +
//                "    \"docDtaEntrada\": " + entrada + ",\n" +
//                "    \"dtaEmissao\": " + emissao + ",\n" +
//                "    \"dtaValidade\": " + vencimento + ",\n" +
//                "    \"pdcDocFederal\": \"" + cnpj + "\",\n" +
//                "    \"items\": [\n" +
//                "        {\n" +
//                "            \"pgsCodSeq\": " + pgsCodSeq + "\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}");
//        return true;
//    }

    private boolean getAddressCode() {
        return true;
    }
}
