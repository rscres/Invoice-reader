package org.jb.invoicereader.DataHandlers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataExtractor {
    DataProcessor processor;

    public DataExtractor(String filePath) throws IOException {

        System.out.println(filePath);
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "source /home/renato/Documents/Code/Invoice-extractor/.venv/bin/activate && python /home/renato/Documents/Code/Invoice-extractor/test.py '" + filePath + "'");
        pb.redirectErrorStream(true);
        Process p = pb.start();

        StringBuilder fullText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fullText.append(line);
//                System.out.println(line);
            }
        }
        p.destroy();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(fullText.toString());
        processor = new DataProcessor(data);
    }

    public ObjectNode getProcessedData() {
        return processor.getData();
    }
}
