package org.jb.invoicereader.DataHandlers;

import tools.jackson.core.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

public class DataProcessor {
    private final ObjectNode data;

    public DataProcessor() {
        data = JsonMapper.shared().createObjectNode();
        data.put("CEDENTE","MULTILOG BRASIL S.A.")
            .put("CNPJ","60.526.977/0019-06")
            .put("NUM_DOCUMENTO","128159")
            .put("VENCIMENTO","13/08/2025")
            .put("EMISSAO","29/07/2025")
            .put("PAGADOR","CENTRAIS ELETRICAS BRASILEIRAS SA")
            .put("VALOR_TOTAL","2060,74")
            .put("COD_PESSOA_CEDENTE", "7473")
            .put("COD_PESSOA_PAGADOR", "8000")
            .putNull("CNPJ_PAGADOR");
    }

    public ObjectNode getData() {
        return data;
    }
}
