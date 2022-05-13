package edu.mills.cs180a.wordui;

import java.net.*;
import java.util.*;
import edu.mills.cs180a.wordui.model.*;
import edu.mills.cs180a.wordui.model.WordRecord.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

public class FXMLController implements Initializable {
    @FXML
    private Label recordCountLabel;
    @FXML
    private TextField wordTextField;
    @FXML
    private TextField frequencyTextField;
    @FXML
    private TextArea definitionTextArea;
    @FXML
    private Button removeButton;
    @FXML
    private Button createButton;
    @FXML
    private Button updateButton;
    @FXML
    private ListView<WordRecord> listView;
    @FXML
    private ChoiceBox<WordRecord.SortOrder> sortChoiceBox;

    private final ObservableList<WordRecord> wordRecordList = FXCollections.observableArrayList();

    private WordRecord selectedWordRecord;
    private final BooleanProperty modifiedProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty freqValidProperty = new SimpleBooleanProperty(false);
    private ChangeListener<WordRecord> wordRecordChangeListener = new WordRecordChangeListener();
    private ChangeListener<WordRecord.SortOrder> sortOrderChangeListener =
            new SortOrderChangeListener();

    // Called when the user selects or unselects a WordRecord.
    private class WordRecordChangeListener implements ChangeListener<WordRecord> {
        @Override
        public void changed(ObservableValue<? extends WordRecord> observable, WordRecord oldValue,
                WordRecord newValue) {
            // newValue can be null if nothing is selected.
            System.out.println("Selected item: " + newValue);
            selectedWordRecord = newValue;
            modifiedProperty.set(false);
            if (newValue != null) {
                wordTextField.setText(selectedWordRecord.getWord());
                frequencyTextField.setText(Integer.toString(selectedWordRecord.getFrequency()));
                freqValidProperty.set(isValidFrequency(frequencyTextField.textProperty()));
                definitionTextArea.setText(selectedWordRecord.getDefinition());
            } else {
                wordTextField.setText("");
                frequencyTextField.setText("");
                freqValidProperty.set(false);
                definitionTextArea.setText("");
            }
        }
    }

    private class SortOrderChangeListener implements ChangeListener<WordRecord.SortOrder> {
        @Override
        public void changed(ObservableValue<? extends SortOrder> observable, SortOrder oldValue,
                SortOrder newValue) {
            setSortOrder(newValue);
        }
    }

    private void setSortOrder(WordRecord.SortOrder newOrder) {
        SortedList<WordRecord> sortedList = new SortedList<>(wordRecordList);
        sortedList.setComparator(newOrder.getComparator());
        listView.setItems(sortedList);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addListeners();
        setupListView();
        configureButtons();
        populateChoiceBox();
    }

    private void setupListView() {
        // Initialize the list.
        SampleData.fillSampleData(wordRecordList);

        // Sort list alphabetically.
        setSortOrder(WordRecord.SortOrder.ALPHABETICALLY_FORWARD);

        // Set up the record count. This must occur after listView is populated.
        recordCountLabel.textProperty().bind(Bindings.size(listView.getItems()).asString());

        // Pre-select the first item.
        listView.getSelectionModel().selectFirst();
    }

    private void populateChoiceBox() {
        sortChoiceBox.setItems(FXCollections.observableArrayList(WordRecord.SortOrder.values()));
        sortChoiceBox.setValue(WordRecord.SortOrder.ALPHABETICALLY_FORWARD);
    }

    private void configureButtons() {
        // Disable the Remove button if nothing is selected in the ListView control.
        removeButton.disableProperty()
                .bind(listView.getSelectionModel().selectedItemProperty().isNull());

        // Disable the Update button if nothing is selected, no modifications have
        // been made, or any field is empty or invalid.
        updateButton.disableProperty()
                .bind(listView.getSelectionModel().selectedItemProperty().isNull()
                        .or(modifiedProperty.not()).or(freqValidProperty.not())
                        .or(wordTextField.textProperty().isEmpty())
                        .or(definitionTextArea.textProperty().isEmpty()));

        // Disable the Create button if an existing entry is selected or any
        // field is empty or invalid.
        createButton.disableProperty()
                .bind(listView.getSelectionModel().selectedItemProperty().isNotNull()
                        .or(modifiedProperty.not()).or(freqValidProperty.not())
                        .or(wordTextField.textProperty().isEmpty())
                        .or(definitionTextArea.textProperty().isEmpty()));

    }

    // A frequency is valid if it is an integer and is at least 0.
    private boolean isValidFrequency(StringProperty sp) {
        try {
            int value = Integer.parseInt(sp.get());
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void addListeners() {
        listView.getSelectionModel().selectedItemProperty().addListener(wordRecordChangeListener);
        sortChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener(sortOrderChangeListener);
    }

    @FXML
    private void handleKeyAction(KeyEvent keyEvent) {
        modifiedProperty.set(true);
        freqValidProperty.set(isValidFrequency(frequencyTextField.textProperty()));
    }

    @FXML
    private void createButtonAction(ActionEvent actionEvent) {
        System.out.println("Create");
        WordRecord wordRecord = new WordRecord(wordTextField.getText(),
                Integer.parseInt(frequencyTextField.getText()), definitionTextArea.getText());
        wordRecordList.add(wordRecord);
        listView.getSelectionModel().select(wordRecord); // select the new item
    }

    @FXML
    private void removeButtonAction(ActionEvent actionEvent) {
        System.out.println("Remove " + selectedWordRecord);
        wordRecordList.remove(selectedWordRecord);
    }

    @FXML
    private void updateButtonAction(ActionEvent actionEvent) {
        System.out.println("Update " + selectedWordRecord);
        WordRecord entry = listView.getSelectionModel().getSelectedItem();
        listView.getSelectionModel().selectedItemProperty()
                .removeListener(wordRecordChangeListener);
        entry.setWord(wordTextField.getText());
        entry.setFrequency(Integer.parseInt(frequencyTextField.getText()));
        entry.setDefinition(definitionTextArea.getText());
        listView.getSelectionModel().selectedItemProperty().addListener(wordRecordChangeListener);
        modifiedProperty.set(false);
    }
}
