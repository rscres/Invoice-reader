package org.jb.invoicereader;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jb.invoicereader.DataHandlers.DataExtractor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GuiController {
    public TextField codProcesso;
    public TextField cedente;
    public TextField cnpjCedente;
    public TextField pagador;
    public TextField cnpjPagador;
    public DatePicker emissao;
    public DatePicker vencimento;
    public TextField valorTotal;
    public ComboBox despesa;
    public TextField codPessoaCedente;
    public TextField codPessoaPagador;
    public TextField numFatura;
    @FXML
    private Label welcomeText;
    @FXML
    private Button createButton;
    @FXML
    private HBox fileInput;

    @FXML
    protected void onCreateButtonClick() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        data.put("CEDENTE", cedente.getText())
                .put("CNPJ", cnpjCedente.getText())
                .put("EMISSAO", String.valueOf(emissao.getValue()))
                .put("VENCIMENTO", String.valueOf(vencimento.getValue()))
                .put("VALOR_TOTAL", valorTotal.getText())
                .put("COD_PESSOA_CEDENTE", codPessoaCedente.getText())
                .put("NUM_DOCUMENTO", numFatura.getText());
        try {
            new CreateExpense(data);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao criar a despesa:\n" + e, ButtonType.OK);
        }
    }

    @FXML
    protected void onExtractButtonClick() {
        TextField text = (TextField) fileInput.getChildren().getFirst();
        if (text.getText().isEmpty()) return;
        try {
            DataExtractor extractor = new DataExtractor(text.getText());
            setProcessedData(extractor.getProcessedData());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao extrair os dados do pdf:\n" + e, ButtonType.OK);
        }

    }
    
    private void setProcessedData(ObjectNode data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        cedente.setText(data.get("CEDENTE").asString(null));
        cnpjCedente.setText(data.get("CNPJ").asString(null));
        pagador.setText(data.get("PAGADOR").asString(null));
        cnpjPagador.setText(data.get("CNPJ_PAGADOR").asString(null));
        emissao.setValue(LocalDate.parse(data.get("EMISSAO").asString(null), formatter));
        vencimento.setValue(LocalDate.parse(data.get("VENCIMENTO").asString(null), formatter));
        valorTotal.setText(data.get("VALOR_TOTAL").asString(null));
        codPessoaCedente.setText(data.get("COD_PESSOA_CEDENTE").asString(null));
        codPessoaPagador.setText(data.get("COD_PESSOA_PAGADOR").asString(null));
        numFatura.setText(data.get("NUM_DOCUMENTO").asString(null));
    }

    @FXML
    protected void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            db.getFiles().forEach(file -> {
                System.out.println("Received file: " + file.getAbsolutePath());
                welcomeText.setText("Dropped: " + file.getName());
                TextField text = (TextField) fileInput.getChildren().getFirst();
                text.setText(file.getAbsolutePath());
            });
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    void handleFileOverEvent(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        if (db.hasFiles())
        {
            event.acceptTransferModes(TransferMode.COPY);
        }
        else
        {
            event.consume();
        }
    }

    @FXML
    void onDragEntered(DragEvent event) {
        VBox vbox = (VBox) event.getSource();
        vbox.setStyle("-fx-border-color: blue; -fx-background-color: #e6f3ff;");
    }

    @FXML
    void onDragExited(DragEvent event) {
        VBox vbox = (VBox) event.getSource();
        vbox.setStyle("");
    }

    @FXML
    void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Escolher fatura");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showOpenDialog(new Stage());
        if (file == null) return;
        TextField text = (TextField) fileInput.getChildren().getFirst();
        text.setText(file.getAbsolutePath());
    }
}