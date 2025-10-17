package org.jb.invoicereader;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Paths;

public class ConexosAPI {
    private String USR;
    private String PWD;

    public ConexosAPI() {
        getConfig();
        System.out.println(USR + " " + PWD);
    }

    private void getConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(Paths.get("src/main/resources/config.json").toFile());
            USR = String.valueOf(data.get("conexos").get("usuario")).replaceAll("\"", "");
            PWD = String.valueOf(data.get("conexos").get("senha")).replaceAll("\"", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void login() {

    }
}
