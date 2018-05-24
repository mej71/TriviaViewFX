package application;

import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.BufferedInputStream;
import java.io.Console;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.print.attribute.standard.DateTimeAtCompleted;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.controls.events.JFXDialogEvent;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class TriviaController implements Initializable {
	
	@FXML
	private BorderPane mainPane;
	
	@FXML
	private JFXTreeTableView<TriviaQuestion> treeView;
	
	@FXML
	private JFXComboBox<SearchTypes> searchCatBox;
	
	@FXML
	private JFXTextField searchTextField;
	
	@FXML
	private ImageView searchImage;
	
	private JFXSnackbar snackBar;
	
	@FXML 
	private Pane snackPane;
	
	@FXML
	private StackPane mainStackPane;
	
	@FXML
	private ImageView addButton;
	@FXML
	private ImageView removeButton;
	@FXML 
	private ImageView aboutButton;
	@FXML
	private ImageView settingsButton;
	
	
	//holds all questions for quick switching
	private ObservableList<TriviaQuestion> allQuestions = FXCollections.observableArrayList();
	//holds questions that match search criteria
	private ObservableList<TriviaQuestion> tempQuestions = FXCollections.observableArrayList();
	public static String dateFormatString = "EEE, MMM d, yy";
	public Settings userSettings = new Settings();
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//init columns info
		JFXTreeTableColumn<TriviaQuestion, String> questionCol = new JFXTreeTableColumn<>("Question");
		questionCol.setPrefWidth(150);
		questionCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TriviaQuestion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<TriviaQuestion, String> param) {
            	ObservableValue<String> ov = new SimpleStringProperty(param.getValue().getValue().question);
                return ov;
            }
        });
		questionCol.setCellFactory((TreeTableColumn<TriviaQuestion, String> param) -> new GenericEditableTreeTableCell<>(new TextFieldEditorBuilder()));
		questionCol.setOnEditCommit((CellEditEvent<TriviaQuestion, String> t) -> t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue().question = t.getNewValue());
		questionCol.setOnEditCommit(new EventHandler<CellEditEvent<TriviaQuestion, String>>() {
        	@Override
        	public void handle(CellEditEvent<TriviaQuestion, String> event) {
        		event.getTreeTableView().getTreeItem(event.getTreeTablePosition().getRow()).getValue().question = event.getNewValue();
        		if (!userSettings.saveManually) {
        			saveQuestions();
        		}
        		return;
        	}
        });
		
		JFXTreeTableColumn<TriviaQuestion, String> answerCol = new JFXTreeTableColumn<>("Answer");
		answerCol.setPrefWidth(150);
		answerCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TriviaQuestion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<TriviaQuestion, String> param) {
            	ObservableValue<String> ov = new SimpleStringProperty(param.getValue().getValue().answer);
                return ov;
            }
        });
		answerCol.setCellFactory((TreeTableColumn<TriviaQuestion, String> param) -> new GenericEditableTreeTableCell<>(new TextFieldEditorBuilder()));
		answerCol.setOnEditCommit(new EventHandler<CellEditEvent<TriviaQuestion, String>>() {
        	@Override
        	public void handle(CellEditEvent<TriviaQuestion, String> event) {
        		event.getTreeTableView().getTreeItem(event.getTreeTablePosition().getRow()).getValue().answer = event.getNewValue();
        		if (!userSettings.saveManually) {
        			saveQuestions();
        		}
        		return;
        	}
        });
		
		JFXTreeTableColumn<TriviaQuestion, String> catCol = new JFXTreeTableColumn<>("Category");
		catCol.setPrefWidth(150);
		catCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TriviaQuestion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<TriviaQuestion, String> param) {
            	ObservableValue<String> ov = new SimpleStringProperty(param.getValue().getValue().category);
                return ov;
            }
        });
		catCol.setCellFactory((TreeTableColumn<TriviaQuestion, String> param) -> new GenericEditableTreeTableCell<>(new TextFieldEditorBuilder()));
		catCol.setOnEditCommit(new EventHandler<CellEditEvent<TriviaQuestion, String>>() {
        	@Override
        	public void handle(CellEditEvent<TriviaQuestion, String> event) {
        		event.getTreeTableView().getTreeItem(event.getTreeTablePosition().getRow()).getValue().category = event.getNewValue();
        		if (!userSettings.saveManually) {
        			saveQuestions();
        		}
        		return;
        	}
        });
		
		JFXTreeTableColumn<TriviaQuestion, String> tagCol = new JFXTreeTableColumn<>("Tags");
		tagCol.setPrefWidth(150);
		tagCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TriviaQuestion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<TriviaQuestion, String> param) {
            	ObservableValue<String> ov = new SimpleStringProperty(param.getValue().getValue().tags);
                return ov;
            }
        });
		tagCol.setCellFactory((TreeTableColumn<TriviaQuestion, String> param) -> new GenericEditableTreeTableCell<>(new TextFieldEditorBuilder()));
		tagCol.setOnEditCommit(new EventHandler<CellEditEvent<TriviaQuestion, String>>() {
        	@Override
        	public void handle(CellEditEvent<TriviaQuestion, String> event) {
        		event.getTreeTableView().getTreeItem(event.getTreeTablePosition().getRow()).getValue().tags = event.getNewValue();
        		if (!userSettings.saveManually) {
        			saveQuestions();
        		}
        		return;
        	}
        });
		
		JFXTreeTableColumn<TriviaQuestion, String> dateCol = new JFXTreeTableColumn<>("Date Last Used");
		dateCol.setPrefWidth(150);
		dateCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TriviaQuestion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<TriviaQuestion, String> param) {
            	ObservableValue<String> ov = new SimpleStringProperty(param.getValue().getValue().lastDateUsed);
                return ov;
            }
        });
        dateCol.setOnEditStart(new EventHandler<CellEditEvent<TriviaQuestion, String>>() {
        	@Override
        	public void handle(CellEditEvent<TriviaQuestion, String> event) {
        		editDateDialog(event.getRowValue().getValue());
        		return;
        	}
        });
        
        Path ipath = Paths.get("images//green_add.png");
        InputStream fileImage;
		try {
			fileImage = new FileInputStream(ipath.toFile());
			addButton.setImage(new Image(fileImage));
			
			ipath = Paths.get("images//green_remove.png");
			fileImage = new FileInputStream(ipath.toFile());
			removeButton.setImage(new Image(fileImage));
			
			ipath = Paths.get("images//green_help.png");
			fileImage = new FileInputStream(ipath.toFile());
			aboutButton.setImage(new Image(fileImage));
			
			ipath = Paths.get("images//green_settings.png");
			fileImage = new FileInputStream(ipath.toFile());
			settingsButton.setImage(new Image(fileImage));
			
			ipath = Paths.get("images//green_search.png");
			fileImage = new FileInputStream(ipath.toFile());
			searchImage.setImage(new Image(fileImage));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        Path spath = Paths.get("data//settings.dat");
        if (Files.exists(spath)) {
			try(
		      InputStream file = new FileInputStream(spath.toFile());
		      InputStream buffer = new BufferedInputStream(file);
		      ObjectInput input = new ObjectInputStream (buffer);
		    ){
		      //deserialize the List
				boolean cont = true;
				while (cont) {
					Object obj = input.readObject();
				    if(obj != null)
				       userSettings = (Settings)obj;
				    else
				       cont = false;
				}
		    }
			catch (EOFException eof) {
				System.out.println("End of file");
			}
		    catch(ClassNotFoundException ex){
		      System.out.println("Cannot perform input. Class not found. " + ex);
		    }
		    catch(IOException ex){
		    	ex.printStackTrace();
		      System.out.println("Cannot perform input. " + ex);
		    }
        }
		
        
		//load questions from questions.dat if it exists
		Path qpath = Paths.get(userSettings.savePath+"//questions.dat");
		if (Files.exists(qpath)) {
			try(
		      InputStream file = new FileInputStream(qpath.toFile());
		      InputStream buffer = new BufferedInputStream(file);
		      ObjectInput input = new ObjectInputStream (buffer);
		    ){
		      //deserialize the List
				boolean cont = true;
				while (cont) {
					Object obj = input.readObject();
				    if(obj != null)
				       allQuestions.add((TriviaQuestion) obj);
				    else
				       cont = false;
				}
				allQuestions = (ObservableList<TriviaQuestion>)input.readObject();
		    }
			catch (EOFException eof) {
				System.out.println("End of file");
			}
		    catch(ClassNotFoundException ex){
		      System.out.println("Cannot perform input. Class not found. " + ex);
		    }
		    catch(IOException ex){
		    	ex.printStackTrace();
		      System.out.println("Cannot perform input. " + ex);
		    }
		} else {
			//add test data for now.  final version will show the user how to add a 
			allQuestions.add(new TriviaQuestion("What is love?", "Baby don't hurt me", "music", (List<String>) Arrays.asList("no","more"), null));
			allQuestions.add(new TriviaQuestion("What isn't love?", "Baby don't hurt me", "meta", (List<String>) Arrays.asList("no","more"), null));
			allQuestions.add(new TriviaQuestion("Another test", "blah blah", "none", (List<String>) Arrays.asList("yes","more"), null));
			allQuestions.add(new TriviaQuestion("Can't stop me now", "Or can you", "hello", (List<String>) Arrays.asList("what","yes"), null));
		}
		
		//add columns and questions to the root
		TreeItem<TriviaQuestion> root = new RecursiveTreeItem<TriviaQuestion>(allQuestions, RecursiveTreeObject::getChildren);
		treeView.getColumns().setAll(questionCol, answerCol, catCol, tagCol, dateCol);
		treeView.setRoot(root);
		treeView.setShowRoot(false);
		
		//init combobox
		searchCatBox.getItems().setAll(SearchTypes.values());
		searchCatBox.setValue(SearchTypes.ALL);
		
		//triggers for text box\
		searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			 
		    @Override
		    public void handle(KeyEvent event) {
		        if(event.getCode().equals(KeyCode.ENTER)) {
		        	updateSearch();
		        }
		    }
		});
		
		//add snackbar manually because wtf jfoenix
		snackBar = new JFXSnackbar(snackPane);
		snackBar.setPrefWidth(400);
		
		//list for keys
		mainPane.setOnKeyPressed(new EventHandler<KeyEvent>(){
		    @Override
		    public void handle(KeyEvent event) {
		    	KeyCombination comb = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
		        if(comb.match(event)){
		           saveQuestions();
		        }
		    }
		});
	}
	
	@FXML
	public void changeMouseToHand(MouseEvent me) {
		mainPane.getScene().setCursor(Cursor.HAND);
	}
	
	@FXML
	public void changeMouseDefault(MouseEvent me) {
		mainPane.getScene().setCursor(Cursor.DEFAULT);
	}

	public void updateSearch() {
		if (searchTextField.getText().isEmpty()) {
			clearSearch();
			return;
		}
		tempQuestions.clear();
		Pattern pattern = Pattern.compile(searchTextField.getText(), Pattern.CASE_INSENSITIVE);
		int count = 0;
		TriviaQuestion tempQ;
		switch (searchCatBox.getValue()) {
		case ALL:
			for (int i = 0; i<allQuestions.size(); ++i) {
				tempQ = allQuestions.get(i);
				if (pattern.matcher(tempQ.question).find() ||
						pattern.matcher(tempQ.answer).find() ||
						pattern.matcher(tempQ.category).find() ||
						pattern.matcher(tempQ.lastDateUsed).find() ||
						pattern.matcher(tempQ.tags).find()) {
					tempQuestions.add(tempQ);
					++count;
				} 
			}
			break;
		case QUESTION:
			for (int i = 0; i<allQuestions.size(); ++i) {
				if (pattern.matcher(allQuestions.get(i).question).find()) {
					tempQuestions.add(allQuestions.get(i));
					++count;
				}
			}
			break;
		case ANSWER:
			for (int i = 0; i<allQuestions.size(); ++i) {
				if (pattern.matcher(allQuestions.get(i).answer).find()) {
					tempQuestions.add(allQuestions.get(i));
					++count;
				}
			}
			break;
		case CATEGORY:
			for (int i = 0; i<allQuestions.size(); ++i) {
				if (pattern.matcher(allQuestions.get(i).category).find()) {
					tempQuestions.add(allQuestions.get(i));
					++count;
				}
			}
			break;
		case TAGS:
			for (int i = 0; i<allQuestions.size(); ++i) {
				if (pattern.matcher(allQuestions.get(i).tags).find()) {
					tempQuestions.add(allQuestions.get(i));
					++count;
				}
			}
			break;
		case DATE:
			for (int i = 0; i<allQuestions.size(); ++i) {
				if (pattern.matcher(allQuestions.get(i).lastDateUsed).find()) {
					tempQuestions.add(allQuestions.get(i));
					++count;
				}
			}
			break;
		}
		TreeItem<TriviaQuestion> root = new RecursiveTreeItem<TriviaQuestion>(tempQuestions, RecursiveTreeObject::getChildren);
		treeView.setRoot(root);
		treeView.setShowRoot(false);
		if (count==0) {
			treeView.setPlaceholder(new Label("No items found"));
		} else {
			snackBar.show(count + " items found", "Okay", 10000L, e -> snackBar.close());
		}
	}
	
	public void clearSearch() {
		TreeItem<TriviaQuestion> root = new RecursiveTreeItem<TriviaQuestion>(allQuestions, RecursiveTreeObject::getChildren);
		treeView.setRoot(root);
		treeView.setShowRoot(false);
		tempQuestions.clear();
	}
	
	@FXML
	public void openAddDialog() {
		mainStackPane.setDisable(false);
		mainStackPane.setAlignment(Pos.BOTTOM_CENTER);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatString);
		JFXDialogLayout content = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(mainStackPane, content, JFXDialog.DialogTransition.TOP);
		content.setHeading(new Text("Add Trivia Question"));
		GridPane gridPane = new GridPane();
	    gridPane.setHgap(10);
	    gridPane.setVgap(10);	    
	    JFXTextField question = new JFXTextField();
	    question.setPromptText("Trivia question");
	    JFXTextField answer = new JFXTextField();
	    answer.setPromptText("Answer to question");
	    JFXTextField category = new JFXTextField();
	    category.setPromptText("Category");
	    JFXTextField tags = new JFXTextField();
	    tags.setPromptText("Tags (Comma seperated)");
	    JFXDatePicker date = new JFXDatePicker();
	    date.setValue(LocalDate.now());
	    Label errorMessage = new Label("All fields are required");
	    errorMessage.setVisible(false);
	    errorMessage.setStyle("-fx-text-fill: red");
	    
	    gridPane.add(question, 0, 0);
	    gridPane.add(answer, 1, 0);
	    gridPane.add(category, 0, 1);
	    gridPane.add(tags, 1, 1);
	    gridPane.add(date, 0, 2);
	    gridPane.setColumnSpan(date, 2);
	    gridPane.add(errorMessage, 0,  3);
	    content.setBody(gridPane);
	    JFXButton addButton = new JFXButton("Add Question");
	    JFXButton cancelButton = new JFXButton("Cancel");
	    cancelButton.setStyle("-fx-background-color:red");
		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (question.getText().equals("") || answer.getText().equals("") || category.getText().equals("") || tags.getText().equals("") ||
						date.getValue()==null) {
					errorMessage.setVisible(true);
					return;
				} else {
					List<String> tagList = (List<String>) Arrays.asList(tags.getText().split(","));
					allQuestions.add(new TriviaQuestion(question.getText(), answer.getText(), category.getText(), tagList, date.getValue().format(formatter)));
				}
				if (!userSettings.saveManually) {
					saveQuestions();
				}
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		content.setActions(addButton, cancelButton);
		dialog.setOverlayClose(false);
		dialog.show();		
	}
	
	@FXML
	public void openSettingsDialog() {
		mainStackPane.setDisable(false);
		mainStackPane.setAlignment(Pos.BOTTOM_CENTER);
		JFXDialogLayout content = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(mainStackPane, content, JFXDialog.DialogTransition.CENTER);
		content.setHeading(new Text("Settings"));
		GridPane gridPane = new GridPane();
	    gridPane.setHgap(10);
	    gridPane.setVgap(10);	    
	    JFXToggleButton toggle = new JFXToggleButton();
	    toggle.setText("Automatically Save");

	    toggle.setSelected(userSettings.saveManually);
	    JFXButton chooseFilePath = new JFXButton("Choose File Path");
	    DirectoryChooser chooser = new DirectoryChooser();
	    Label filePath = new Label(userSettings.savePath+"/questions.dat");
	    chooseFilePath.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				File file = chooser.showDialog(mainStackPane.getScene().getWindow());
				String path = "";
				if (file!=null) {
					path = file.getAbsolutePath();
				}
				if (path!=null && !path.equals("")) {
					filePath.setText(path);
				}
			}
		});
	   
	   
	    
	    gridPane.add(toggle, 0, 0);
	    gridPane.add(chooseFilePath, 0, 1);
	    gridPane.add(filePath, 1, 1);
	    gridPane.setColumnSpan(toggle, 2);
	    content.setBody(gridPane);
	    JFXButton addButton = new JFXButton("Confirm changes");
	    JFXButton cancelButton = new JFXButton("Cancel");
	    cancelButton.setStyle("-fx-background-color:red");
		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				userSettings.saveManually = Boolean.parseBoolean(toggle.selectedProperty().toString());
				userSettings.savePath = filePath.getText();
				saveSettings();
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		content.setActions(addButton, cancelButton);
		dialog.setOverlayClose(false);
		dialog.show();		
	}
	
	public void editDateDialog(TriviaQuestion tq) {
		mainStackPane.setDisable(false);
		mainStackPane.setAlignment(Pos.BOTTOM_CENTER);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatString);
		JFXDialogLayout content = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(mainStackPane, content, JFXDialog.DialogTransition.CENTER);
		content.setHeading(new Text("Choose a new date")); 
		JFXDatePicker date = new JFXDatePicker();
	    date.setValue(LocalDate.now());
		content.setBody(date);
		JFXButton confirmButton = new JFXButton("Confirm");
		JFXButton cancelButton = new JFXButton("Cancel");
	    cancelButton.setStyle("-fx-background-color:red");
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				for(int i = 0; i<allQuestions.size(); ++i) {
					if (allQuestions.get(i).id.equals(tq.id)) {
						allQuestions.get(i).lastDateUsed = date.getValue().format(formatter);
						break;
					}
				}
				for(int i = 0; i<tempQuestions.size(); ++i) {
					if (tempQuestions.get(i).id.equals(tq.id)) {
						tempQuestions.get(i).lastDateUsed = date.getValue().format(formatter);
						break;
					}
				}
				if (!userSettings.saveManually) {
					saveQuestions();
				}
				updateSearch();
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		dialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
			@Override
			public void handle(JFXDialogEvent event) {
				mainStackPane.setDisable(true);
			}
		});
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		content.setActions(confirmButton);
		dialog.show();		
	}
	
	public void saveQuestions() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;

		try {

			fout = new FileOutputStream(userSettings.savePath+"//questions.dat");
			oos = new ObjectOutputStream(fout);
			for(int i = 0; i<allQuestions.size(); ++i) {
				oos.writeObject(allQuestions.get(i));
			}
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void saveSettings() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;

		try {

			fout = new FileOutputStream("data//settings.dat");
			oos = new ObjectOutputStream(fout);
			oos.writeObject(userSettings);
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@FXML
	public void removeSelected() {
		if (treeView.getSelectionModel().getSelectedItem()==null) {
			return;
		}
		mainStackPane.setDisable(false);
		mainStackPane.setAlignment(Pos.BOTTOM_CENTER);
		JFXDialogLayout content = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(mainStackPane, content, JFXDialog.DialogTransition.CENTER);
		content.setHeading(new Text("Remove Question")); 
		content.setBody(new Text("Are you sure you want to remove this question?"));
		JFXButton confirmButton = new JFXButton("Okay");
		JFXButton cancelButton = new JFXButton("Cancel");
		cancelButton.setStyle("-fx-background-color:red");
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String id = treeView.getSelectionModel().getSelectedItem().getValue().id;
				allQuestions.removeIf(q -> q.id==id);
				tempQuestions.removeIf(q -> q.id==id);
				//if all questions from search list were removed, clear the search and go back to the list of all
				if (tempQuestions.size()==0) {
					clearSearch();
				}
				if (!userSettings.saveManually) {
					saveQuestions();
				}
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		//treat clicking outside as canceling
		dialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
			@Override
			public void handle(JFXDialogEvent event) {
				mainStackPane.setDisable(true);
			}
		});
		content.setActions(confirmButton, cancelButton);
		dialog.show();		
	}
	
	
	@FXML
	public void openAboutDialog() {
		mainStackPane.setDisable(false);
		mainStackPane.setAlignment(Pos.BOTTOM_CENTER);
		JFXDialogLayout content = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(mainStackPane, content, JFXDialog.DialogTransition.CENTER);
		content.setHeading(new Text("About")); 
		content.setBody(new Text("This program was created by John (Jem) Milam\n©2018"));
		JFXButton button = new JFXButton("Okay");
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dialog.close();
				mainStackPane.setDisable(true);
			}
		});
		dialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
			@Override
			public void handle(JFXDialogEvent event) {
				mainStackPane.setDisable(true);
			}
		});
		content.setActions(button);
		dialog.show();		
	}
	
	
	
	public enum SearchTypes {
		ALL,
		QUESTION,
		ANSWER,
		CATEGORY,
		TAGS,
		DATE			
	}

}

class TriviaQuestion extends RecursiveTreeObject<TriviaQuestion> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String id;
	public String question;
	public String answer;
	public String category;
	public String tags;
	public String lastDateUsed;
	private List<String> tagList = new ArrayList<String>();
	
	
	public TriviaQuestion(String question, String answer, String category, List<String> tags, String date) {
		this.id = UUID.randomUUID().toString();
		this.question = question;
		this.answer= answer;
		this.category= category;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TriviaController.dateFormatString);
		if (date==null) {
			this.lastDateUsed = LocalDate.now().format(formatter);
		} else {
			this.lastDateUsed = date;
		}
		this.tagList = tags;
		this.updateTags();
	}
	
	public void updateTags() {
		String tempTags = "";
		for (int i = 0; i < tagList.size(); i++) {
			if (i>0) {
				tempTags += ", ";
			}
			tempTags += tagList.get(i);
		}
		this.tags= tempTags;
	}
	
	public void addTag(String tag) {
		this.tagList.add(tag);
		this.updateTags();			
	}
	
	public void removeTag(String tag) {
		this.tagList.removeIf(tag::equals);
		this.updateTags();
	}
	
	public void setTags(List<String> newTags) {
		this.tagList = newTags;
		this.updateTags();
	}
}

class Settings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean saveManually = true;
	public String savePath = new String("data");
	
}


