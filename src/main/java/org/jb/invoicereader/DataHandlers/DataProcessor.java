package org.jb.invoicereader.DataHandlers;

import org.jb.invoicereader.DbHandler;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataProcessor {
    private final ObjectNode data;

    public DataProcessor(JsonNode extracted_data) throws SQLException {
        System.out.println(extracted_data);
//        System.out.println(extracted_data.get("CEDENTE").get(0));
        data = JsonMapper.shared().createObjectNode();
//        data.put("CEDENTE","MULTILOG BRASIL S.A.")
//            .put("CNPJ","60.526.977/0019-06")
//            .put("NUM_DOCUMENTO","128159")
//            .put("VENCIMENTO","13/08/2025")
//            .put("EMISSAO","29/07/2025")
//            .put("PAGADOR","CENTRAIS ELETRICAS BRASILEIRAS SA")
//            .put("VALOR_TOTAL","2060,74")
//            .put("COD_PESSOA_CEDENTE", "7473")
//            .put("COD_PESSOA_PAGADOR", "8000")
//            .putNull("CNPJ_PAGADOR");
        String cnpj = checkField(extracted_data.get("CNPJ"));
        String cnpj_pagador = checkField(extracted_data.get("CNPJ_PAGADOR"));
        data.put("CEDENTE",checkField(extracted_data.get("CEDENTE")))
                .put("CNPJ",cnpj)
                .put("NUM_DOCUMENTO",checkField(extracted_data.get("NUM_DOCUMENTO")))
                .put("VENCIMENTO",checkField(extracted_data.get("VENCIMENTO")))
                .put("EMISSAO",checkField(extracted_data.get("EMISSAO")))
                .put("PAGADOR",checkField(extracted_data.get("PAGADOR")))
                .put("VALOR_TOTAL",checkValorTotal(extracted_data.get("VALOR_TOTAL")))
                .put("COD_PESSOA_CEDENTE", fetchCodPessoa(cnpj))
                .put("COD_PESSOA_PAGADOR", fetchCodPessoa(cnpj_pagador))
                .put("CNPJ_PAGADOR", cnpj_pagador);
    }

    public ObjectNode getData() {
        return data;
    }

    private String checkField(JsonNode data) {
        if (data == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        List<String> dataList = mapper.convertValue(data, new TypeReference<>() {});
        if (dataList.size() == 1 || dataList.stream().allMatch(element -> Objects.equals(dataList.getFirst(), element))) {
            System.out.println("Equal");
            return dataList.getFirst();
        }

        return null;
    }

    private String checkValorTotal(JsonNode data) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> dataList = mapper.convertValue(data, new TypeReference<>() {});
        String value = null;
        System.out.println("here");
        if (dataList.size() == 1 || dataList.stream().allMatch(element -> Objects.equals(dataList.getFirst(), element))) {
            value = dataList.getFirst();
        } else {
            System.out.println("here2");
            Map<String, Integer> entryCount = new HashMap<>();
            for (int i = 0; i < dataList.size() - 1; i++) {
                for (int j = i + 1; j < dataList.size(); j++) {
                    if (Objects.equals(dataList.get(i), dataList.get(j))) {
                        if (entryCount.containsKey(dataList.get(i))) {
                            entryCount.put(dataList.get(i), entryCount.get(dataList.get(i)) + 1);
                        } else {
                            entryCount.put(dataList.get(i), 1);
                        }
                    }
                }
            }
            value = entryCount.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        }
        System.out.println("Value: " + value);
        String formattedDefault = value.replaceAll(",", "")
                                        .replaceAll(" ", "")
                                        .replaceAll("\\.", "");
        formattedDefault = new StringBuilder(formattedDefault).insert(formattedDefault.length() - 2, ",").toString();
        return formattedDefault;
    }

    private String fetchCodPessoa(String cnpj) throws SQLException {
        if (cnpj == null) return null;
        System.out.println(cnpj);
        DbHandler dbHandler = DbHandler.getInstance();
        String formattedCnpj = cnpj.replaceAll("\\.", "")
                .replaceAll("/", "")
                .replaceAll("-", "");
        String pesCod = dbHandler.getPesCod(formattedCnpj);
        System.out.println(pesCod);
        if (pesCod == null) {
            System.out.println(formattedCnpj.substring(0, 8));
            pesCod = dbHandler.getPesCod(formattedCnpj.substring(0, 8));
        }
        System.out.println(pesCod);
        return pesCod;
    }
}
