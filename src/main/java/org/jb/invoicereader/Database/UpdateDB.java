package org.jb.invoicereader.Database;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

public class UpdateDB {
    private final DbHandler db;

    public UpdateDB() throws SQLException, IOException, InterruptedException {
        db = DbHandler.getInstance();

        if (checkIfDbExists() == false || db.isTableEmpty("pessoas") || db.isTableEmpty("despesas")) {
            InitDB initialiazer = new InitDB();
        }
    }

    private boolean checkIfDbExists() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(Paths.get("src/main/resources/config.json").toFile());
        File file = new File("./" + String.valueOf(data.get("database").get("filename")).replaceAll("\"", ""));
        return file.exists();
    }
}
