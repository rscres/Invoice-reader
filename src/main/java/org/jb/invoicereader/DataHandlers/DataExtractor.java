package org.jb.invoicereader.DataHandlers;

public class DataExtractor {
    DataProcessor processor;

    public DataExtractor(String filePath) {
        processor = new DataProcessor();
        System.out.println(filePath);
    }
}
