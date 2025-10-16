package org.jb.invoicereader.DataHandlers;

import tools.jackson.databind.node.ObjectNode;

public class DataExtractor {
    DataProcessor processor;

    public DataExtractor(String filePath) {
        processor = new DataProcessor();
        System.out.println(filePath);
    }

    public ObjectNode getProcessedData() {
        return processor.getData();
    }
}
