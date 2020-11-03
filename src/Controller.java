import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    Stage thisStage;
    FileChooser fileChooser;

    char pnpSeparator = ',';
    String designatorPostfix = "-";
    String boardPrefix = "B-";

    String csvSeperator = ",";
    String csvPartSeperator = " ";

    //  PnP
    int iPnpDesignator = 0;
    int iPnpMidX = 1;
    int iPnpMidY = 2;
    int iPnpRotation = 3;
    int iPnpValue = 4;
    int iPnpPackage = 5;

    int minPnpSentenceLength = 6;


    @FXML
    public TextField pnpPath;
    @FXML
    public Button pnpButton;

    @FXML
    public TextField firstBoardXField;
    @FXML
    public TextField firstBoardYField;
    @FXML
    public TextField amoutXField;
    @FXML
    public TextField amoutYField;
    @FXML
    public TextField offsetXField;
    @FXML
    public TextField offsetYField;

    @FXML
    public Button createBoardsButton;
    @FXML
    public Button createPanelButton;
    @FXML
    public Button clearButton;
    @FXML
    public Button exportButton;

    @FXML
    public TableView partsTable;
    @FXML
    public TableView boardsTable;


    PartList activePartList;
    BoardList activeBoardList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
        activeBoardList = new BoardList();
        preBuildPartTable();
        preBuildBoardTable();
    }

    public void setStage(Stage stage) {
        thisStage = stage;
    }

    @FXML
    void onPnpLoad() throws Exception {
        File file = fileChooser.showOpenDialog(thisStage);
        if (file != null) {
            pnpPath.setText(file.getAbsolutePath());

            Table table = tokenise(file, pnpSeparator);

            if (table != null) {
                pnpButton.setDisable(true);
                doPartList(table);

                createPanelButton.setDisable(false);
                clearButton.setDisable(false);
                exportButton.setDisable(false);
            }
        }
    }

    @FXML
    void onBoardsCreate() {
        float firstX = Float.parseFloat(firstBoardXField.getText());
        float firstY = Float.parseFloat(firstBoardYField.getText());
        float offX = Float.parseFloat(offsetXField.getText());
        float offY = Float.parseFloat(offsetYField.getText());
        int amountX = Integer.parseInt(amoutXField.getText());
        int amountY = Integer.parseInt(amoutYField.getText());

        for (int x = 0; x < amountX; x++) {
            for (int y = 0; y < amountY; y++) {
                Board newBoard = new Board();
                newBoard.designator = boardPrefix + activeBoardList.boards.size();
                newBoard.x = firstX + ((float)x * offX);
                newBoard.y = firstY + ((float)y * offY);

                activeBoardList.boards.add(newBoard);
            }
        }

        updateBoardTable();
    }

    @FXML
    void onPanelCreate() {
        if (activeBoardList == null) {
            if (activeBoardList.boards.size() <= 0) {
                return;
            }
        }

        if (activePartList != null && activePartList.parts.size() > 0) {
            PartList originalParts = activePartList;
            activePartList = new PartList();

            for (Board board : activeBoardList.boards) {
                for (Part originalPart : originalParts.parts) {
                    Part newPart = new Part(originalPart);

                    newPart.midX += board.x;
                    newPart.midY += board.y;
                    newPart.designator += designatorPostfix + board.designator;

                    activePartList.parts.add(newPart);
                }
            }

            createBoardsButton.setDisable(true);
            createPanelButton.setDisable(true);
            updatePartTable();
        }
    }

    @FXML
    void clear() {
        createBoardsButton.setDisable(false);
        createPanelButton.setDisable(true);
        exportButton.setDisable(true);

        activePartList = null;
        activeBoardList = new BoardList();

        pnpPath.setText("");
        pnpButton.setDisable(false);

        updatePartTable();
        updateBoardTable();
    }

    @FXML
    public void export() throws Exception {
        // File Selection
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("CSV BOM file (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File bomFile;
        do {
            bomFile = fileChooser.showSaveDialog(thisStage);
        } while (bomFile == null);

        extensionFilter = new FileChooser.ExtensionFilter("CSV PnP file (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(extensionFilter);

        File pnpFile;
        do {
            pnpFile = fileChooser.showSaveDialog(thisStage);
        } while (pnpFile == null);

        // BOM Writing
        bomFile.createNewFile();
        FileWriter bomWriter = new FileWriter(bomFile);
        bomWriter.write("Comment" + csvSeperator + "Designator" + csvSeperator + "Footprint" + csvSeperator + "LCSC Part # (optional)\r\n");

        PartList bomList = new PartList();

        for (Part part : activePartList.parts) {
            bomList.parts.add(new Part(part));
        }

        for (int i = 0; i < bomList.parts.size(); i++) {
            Part part = bomList.parts.get(i);

            // First, check for equality
            for (int j = i+1; j < bomList.parts.size();) {
                Part mayEqualPart = bomList.parts.get(j);

                if (part.comment.equals(mayEqualPart.comment) &&
                    part.footprint.equals(mayEqualPart.footprint)) {
                    part.designator += csvPartSeperator + mayEqualPart.designator;
                    bomList.parts.remove(mayEqualPart);
                } else {
                    j++;
                }
            }

            // Next, write to File
            bomWriter.write(part.comment + csvSeperator + part.designator + csvSeperator + part.footprint + csvSeperator + part.lcscPart + "\r\n");
        }

        // Next, PnP Writing
        pnpFile.createNewFile();
        FileWriter pnpWriter = new FileWriter(pnpFile);
        pnpWriter.write("Designator" + csvSeperator + "Mid X" + csvSeperator + "Mid Y" + csvSeperator + "Layer" + csvSeperator + "Rotation\r\n");

        PartList pnpList = new PartList();

        for (Part part : activePartList.parts) {
            pnpList.parts.add(new Part(part));
        }

        for (Part part : pnpList.parts) {
            pnpWriter.write(part.designator + csvSeperator + part.midX + csvSeperator + part.midY + csvSeperator + part.layer + csvSeperator + part.rotation + "\r\n");
        }

        fileChooser.getExtensionFilters().clear();

        bomWriter.close();
        pnpWriter.close();

        clear();
    }

    Table tokenise(File file, char separator) throws Exception {
        FileReader fileReader = new FileReader(file);
        Table thisTable = new Table();

        Sentence currentSentence = new Sentence();
        Token currentToken = new Token();

        boolean endReached = false;
        boolean gotRbefore = false;
        while (endReached == false) {
            int currentChar = fileReader.read();

            if (currentChar == -1) {
                endReached = true;
                currentSentence.tokens.add(currentToken);
                thisTable.sentences.add(currentSentence);
            } else {
                // Tokenisation here
                if (currentChar == separator) {
                    // Only add new Token after Separator
                    currentSentence.tokens.add(currentToken);
                    currentToken = new Token();
                } else if (currentChar == '\n') {
                    // Add Token to Sentence and start new Sentence
                    if (gotRbefore) {
                        gotRbefore = false;
                    } else {
                        currentSentence.tokens.add(currentToken);
                        currentToken = new Token();
                        thisTable.sentences.add(currentSentence);
                        currentSentence = new Sentence();
                    }
                } else if (currentChar == '\r') {
                    currentSentence.tokens.add(currentToken);
                    currentToken = new Token();
                    thisTable.sentences.add(currentSentence);
                    currentSentence = new Sentence();

                    gotRbefore = true;
                } else {
                    // Add char to Token only
                    gotRbefore = false;

                    if (currentChar != ' ' &&
                        currentChar != '"')
                        currentToken.name += Character.toString((char)currentChar);
                }
            }
        }

        fileReader.close();
        return thisTable;
    }

    void doPartList(Table table) {
        PartList newPartlist = makePartsList(table);

        if (newPartlist != null) {
            activePartList = newPartlist;
        }

        updatePartTable();
    }

    /**
     * Convert table into partlist (currently, only PnP data is deeded as it contains all information in Eagle
     */
    PartList makePartsList(Table _pnpTable) {
        // First, create Parts from _pnpTable
        PartList currentPartList = new PartList();

        for (Sentence currentSentence : _pnpTable.sentences) {
            if (currentSentence.tokens.size() < minPnpSentenceLength) break;

            Part currentPart = new Part();

            // Read all Data from Sentence in its field in Part
            currentPart.designator = currentSentence.tokens.get(iPnpDesignator).name;
            currentPart.midX = Float.parseFloat(currentSentence.tokens.get(iPnpMidX).name);
            currentPart.midY = Float.parseFloat(currentSentence.tokens.get(iPnpMidY).name);
            currentPart.rotation = Float.parseFloat(currentSentence.tokens.get(iPnpRotation).name);
            currentPart.comment = currentSentence.tokens.get(iPnpValue).name;
            currentPart.footprint = currentSentence.tokens.get(iPnpPackage).name;

            currentPartList.parts.add(currentPart);
        }

        return currentPartList;
    }

    void preBuildBoardTable() {
        TableColumn<Board, String> designatorColumn = new TableColumn<>("Designator");
        designatorColumn.setCellValueFactory(new PropertyValueFactory<>("designator"));

        TableColumn<Board, Float> xColumn = new TableColumn<>("X");
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));

        TableColumn<Board, Float> yColumn = new TableColumn<>("Y");
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));

        boardsTable.getColumns().addAll(designatorColumn, xColumn, yColumn);
    }

    void updateBoardTable() {
        // Clear Table
        boardsTable.getItems().clear();

        if (activeBoardList != null) {
            for (Board board : activeBoardList.boards) {
                boardsTable.getItems().add(board);
            }
        }

        boardsTable.refresh();
    }

    void preBuildPartTable() {
        TableColumn<Part, String> designatorColumn = new TableColumn<>("Designator");
        designatorColumn.setCellValueFactory(new PropertyValueFactory<>("designator"));

        TableColumn<Part, String> commentColumn = new TableColumn<>("Comment");
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        TableColumn<Part, String> footprintColumn = new TableColumn<>("Footprint");
        footprintColumn.setCellValueFactory(new PropertyValueFactory<>("footprint"));

        TableColumn<Part, Float> midXColumn = new TableColumn<>("Mid X");
        midXColumn.setCellValueFactory(new PropertyValueFactory<>("midX"));

        TableColumn<Part, Float> midYColumn = new TableColumn<>("Mid Y");
        midYColumn.setCellValueFactory(new PropertyValueFactory<>("midY"));

        TableColumn<Part, Integer> rotationColumn = new TableColumn<>("Rotation");
        rotationColumn.setCellValueFactory(new PropertyValueFactory<>("rotation"));

        TableColumn<Part, Float> lcscPartColumn = new TableColumn<>("LCSC Part");
        lcscPartColumn.setCellValueFactory(new PropertyValueFactory<>("lcscPart"));

        partsTable.getColumns().addAll(designatorColumn, commentColumn, footprintColumn, midXColumn, midYColumn, rotationColumn, lcscPartColumn);
    }

    void updatePartTable() {
        // Clear Table
        partsTable.getItems().clear();

        if (activePartList != null) {
            for (Part part : activePartList.parts) {
                partsTable.getItems().add(part);
            }
        }

        partsTable.refresh();
    }
}
