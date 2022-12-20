package project.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import project.inference.Clause;
import project.inference.Condition;
import project.inference.InferenceEngine;
import project.inference.Rule;
import project.inference.Variable;

public class MainController {

    @FXML private AnchorPane root;
    @FXML private BorderPane rootBorderPane;
    @FXML private ScrollPane rootsc;
    @FXML private HBox titleHBox;
    @FXML private Label titleLabel;

    @FXML private VBox tableVBox;
    @FXML private TableView<Rule> knowledgeBaseTableView;
    @FXML private TableColumn<Rule, String> labelColumn;
    @FXML private TableColumn<Rule, String> antecedentsColumn;
    @FXML private TableColumn<Rule, String> consequentColumn;
    @FXML private HBox buttonsHBox;
    @FXML private Button forwardButton;
    @FXML private MenuButton basesMenuButton;

    @FXML private VBox rightVBox;
    @FXML private VBox variablesVBox;
    @FXML private TextArea logTextArea;

    private String targetVariable;
    private HashMap<String, ArrayList<String>> variables;
    private HashMap<String, Variable> memory;
    private HashMap<String, Rule> knowledgeBase;
    private InferenceEngine inferenceEngine;
    private HashMap<String, ComboBox<String>> variablesComboBoxes;


    @FXML
    protected void initialize() {

        // initializing knowledge base tableview's columns
        labelColumn.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Rule,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Rule, String> p) {
                return new ReadOnlyStringWrapper(p.getValue().getLabel());
            }
        });
        antecedentsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Rule,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Rule, String> p) {
                return new ReadOnlyStringWrapper(p.getValue().getAntecedentsString());
            }
        });
        consequentColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Rule,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Rule, String> p) {
                return new ReadOnlyStringWrapper(p.getValue().getConsequentString());
            }
        });

        forwardButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // disabling ComboBoxes 
                for (ComboBox<String> tf : variablesComboBoxes.values()) {
                    tf.setDisable(true);
                }

                String selectedRule = inferenceEngine.forwardPass();
                while (true) {
                    if (!memory.get(targetVariable).getValue().equals("")) {
                        logTextArea.appendText("[ Inference Engine ] : Target variable found.\n");
                        break;
                    } else if (selectedRule == null) {
                        logTextArea.appendText("[ Inference Engine ] : No rule can be applied.\n");
                        break;
                    } else {
                        logTextArea.appendText("[ Inference Engine ] : Conflict set : " + String.join(", ", inferenceEngine.getConflictSet()) + "\n");
                        logTextArea.appendText("[ Inference Engine ] : Selected rule : " + selectedRule + "\n");
                        knowledgeBase.get(selectedRule).fire();

                        // updating ComboBoxes 
                        for (Map.Entry<String, ComboBox<String>> e : variablesComboBoxes.entrySet()) {
                            e.getValue().getSelectionModel().select(memory.get(e.getKey()).getValue());
                        }
                    }

                    selectedRule = inferenceEngine.forwardPass();
                }
            }
        });

        Set<String> basesFileNames = Stream.of(new File(this.getClass().getResource("/bases").getFile()).listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());
    
        for (String fileName : basesFileNames) {
            MenuItem baseMenuItem = new MenuItem(fileName);

            baseMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setBase(((MenuItem)event.getSource()).getText());
                }
            });

            basesMenuButton.getItems().add(baseMenuItem);
        }

        setBase("base.json");
    }

    private void setBase(String fileName) {
        variables = new HashMap<String, ArrayList<String>>();
        memory = new HashMap<String, Variable>();
        knowledgeBase = new HashMap<String, Rule>();
        jsonToExpertSystem("/bases/" + fileName);
        inferenceEngine = new InferenceEngine(this.memory, this.knowledgeBase);
    
        knowledgeBaseTableView.getItems().clear();
        knowledgeBaseTableView.getItems().addAll(knowledgeBase.values());

        // initialize variables vbox
        variablesVBox.getChildren().clear();
        variablesComboBoxes = new HashMap<String, ComboBox<String>>();
        for (Map.Entry<String, Variable> e : this.memory.entrySet()) {
            Label label = new Label(e.getKey());

            ComboBox<String> comboBox = new ComboBox<String>();
            for (String value : this.variables.get(e.getKey())) {
                comboBox.getItems().add(value);
            }
            comboBox.getSelectionModel().select(e.getValue().getValue());

            variablesComboBoxes.put(e.getKey(), comboBox);

            comboBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    e.getValue().setValue(comboBox.getValue());
                }
            });

            variablesVBox.getChildren().add(label);
            variablesVBox.getChildren().add(comboBox);
        }

        logTextArea.setText("");
    }

    private void jsonToExpertSystem(String baseFile) {
        JSONParser parser = new JSONParser();
        JSONObject base = null;
        try {
            base = (JSONObject) parser.parse(
                new FileReader(
                    this.getClass().getResource(baseFile).getFile()
                )
            );

            targetVariable = (String) base.get("target");

            JSONArray variables = (JSONArray) base.get("variables");
            for (Object variable : variables) {
                String name = (String) ((JSONObject) variable).get("name");
                JSONArray valuesJSON = (JSONArray) ((JSONObject) variable).get("values");
                ArrayList<String> values  = new ArrayList<String>();
                for (Object value : valuesJSON)
                    values.add((String) value);

                this.memory.put(name, new Variable(name));
                this.variables.put(name, values);
            }
            
            JSONArray memory = (JSONArray) base.get("memory");
            for (Object valuation : memory) {
                String variable = (String) ((JSONObject) valuation).get("variable");
                String value = (String) ((JSONObject) valuation).get("value");

                this.memory.get(variable).setValue(value);
            }

            JSONArray knowledgeBase = (JSONArray) base.get("knowledge base");
            for (Object rule : knowledgeBase) {
                String label = (String) ((JSONObject) rule).get("label");
                JSONArray antecedentsJSON = (JSONArray) ((JSONObject) rule).get("antecedents");
                JSONObject consequentJSON = (JSONObject) ((JSONObject) rule).get("consequent");

                ArrayList<Clause> antecedents = new ArrayList<Clause>();            
                for (Object antecedentJSON : antecedentsJSON) {
                    antecedents.add(objectToClause((JSONObject) antecedentJSON));
                }

                Clause consequent = objectToClause(consequentJSON);

                this.knowledgeBase.put(
                    label,
                    new Rule(label, antecedents, consequent)
                );
            }
        } catch (ParseException e) {
            System.out.println("Parser exception");
        } catch (IOException e) {
            System.out.println(this.getClass().getResource(
                "/bases/base.json").getFile() + " not found"
            );
        }
    }

    private Clause objectToClause(JSONObject object) {
        String variable = (String) object.get("variable");
        Condition condition = Condition.fromString(
            (String) object.get("condition")
        );
        String value = (String) object.get("value");
        return new Clause(
            this.memory.get(variable),
            condition,
            value
        );
    }
}

