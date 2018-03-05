import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {

    //Table for the results
    private TableView<TestFile> files;

    //Directories to pass to SpamDetector class
    File trainHamDir = new File("");
    File trainSpamDir = new File("");
    File testHamDir = new File("");
    File testSpamDir = new File("");

    File outFile = new File("TrainingData.txt");

    //Instance of spam detector
    SpamDetector spamDetector = new SpamDetector();

    Label labelCorrectGuesses;
    Label labelPrecision;

    @Override public void start(Stage primaryStage) {
        primaryStage.setTitle("Assignment 1 - Spam Detector");

        //Directories
        GridPane bottom = new GridPane();
        bottom.setPadding(new Insets(10));
        bottom.setHgap(10);
        bottom.setVgap(10);

        Label labelTrainHamDir = new Label("Train Ham Folder");
        Button btnTrainHamDir = new Button();
        btnTrainHamDir.setText("Choose Train Ham Folder");
        btnTrainHamDir.setOnAction(e -> openDirectory(0, labelTrainHamDir, primaryStage));
        bottom.add(labelTrainHamDir, 0,0);
        bottom.add(btnTrainHamDir, 1, 0);

        Label labelTrainSpamDir = new Label("Train Spam Folder");
        Button btnTrainSpamDir = new Button();
        btnTrainSpamDir.setText("Choose Train Spam Folder");
        btnTrainSpamDir.setOnAction(e -> openDirectory(1, labelTrainSpamDir, primaryStage));
        bottom.add(labelTrainSpamDir, 0,1);
        bottom.add(btnTrainSpamDir, 1, 1);

        Label labelTestHamDir = new Label("Test Ham Folder");
        Button btnTestHamDir = new Button();
        btnTestHamDir.setText("Choose Train Ham Folder");
        btnTestHamDir.setOnAction(e -> openDirectory(2, labelTestHamDir, primaryStage));
        bottom.add(labelTestHamDir, 2,0);
        bottom.add(btnTestHamDir, 3, 0);

        Label labelTestSpamDir = new Label("Test Spam Folder");
        Button btnTestSpamDir = new Button();
        btnTestSpamDir.setText("Choose Train Spam Folder");
        btnTestSpamDir.setOnAction(e -> openDirectory(3, labelTestSpamDir, primaryStage));
        bottom.add(labelTestSpamDir, 2,1);
        bottom.add(btnTestSpamDir, 3, 1);

        Label labelOutputFolder = new Label("Output Folder");
        Button btnOutputFolder = new Button();
        btnOutputFolder.setText("Choose Training Data Output Folder");
        btnOutputFolder.setOnAction(e -> openDirectory(4, labelOutputFolder, primaryStage));
        bottom.add(labelOutputFolder, 0,2);
        bottom.add(btnOutputFolder, 1, 2);

        Button btnStartTraining = new Button();
        btnStartTraining.setText("Start Training");
        btnStartTraining.setOnAction(e -> StartTraining());
        bottom.add(btnStartTraining, 0, 4);

        Button btnTest = new Button();
        btnTest.setText("Test Files");
        btnTest.setOnAction(e -> Test());
        bottom.add(btnTest, 2, 4);

        labelCorrectGuesses = new Label("Accuracy: ");
        bottom.add(labelCorrectGuesses, 0, 6);

        labelPrecision = new Label("Precision: ");
        bottom.add(labelPrecision, 1, 6);

        //Table stuff
        BorderPane layout = new BorderPane();

        TableColumn<TestFile, String> fileNameCol = new TableColumn<>("File Name");
        fileNameCol.setPrefWidth(500);
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));

        TableColumn<TestFile, Float> spamProbabilityCol = new TableColumn<>("Spam Probability");
        spamProbabilityCol.setPrefWidth(250);
        spamProbabilityCol.setCellValueFactory(new PropertyValueFactory<>("spamProbability"));

        TableColumn<TestFile, String> actualClassCol = new TableColumn<>("Actual Class");
        actualClassCol.setPrefWidth(250);
        actualClassCol.setCellValueFactory(new PropertyValueFactory<>("actualClass"));

        this.files = new TableView<>();
        this.files.getColumns().add(fileNameCol);
        this.files.getColumns().add(spamProbabilityCol);
        this.files.getColumns().add(actualClassCol);

        layout.setCenter(files);
        layout.setBottom(bottom);

        Scene scene = new Scene(layout, 1000, 1000);
        primaryStage.setScene(scene);
        primaryStage.show();
       // this.files.setItems(Data.getAllFiles());

    }

    //Open directory chooser and assign the relevent directory.
    public void openDirectory(int dirChoice, Label label, Stage stage)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(stage);
        //Rip. Java is pass by value
        if(dirChoice == 0){ trainHamDir = directory; System.out.println(trainHamDir.getAbsolutePath()); }
        else if(dirChoice == 1){ trainSpamDir = directory; System.out.println(trainSpamDir.getAbsolutePath()); }
        else if(dirChoice == 2){ testHamDir = directory; System.out.println(testHamDir.getAbsolutePath()); }
        else if(dirChoice == 3){ testSpamDir = directory; System.out.println(testSpamDir.getAbsolutePath()); }
        else if(dirChoice == 4){ outFile = new File(directory.getAbsolutePath() + "/TrainingData.txt"); }
        label.setText(directory.getAbsolutePath());
    }

    public void StartTraining(){
        try {
            //Read through files and map them
            spamDetector.MapData(trainHamDir, true);
            spamDetector.MapData(trainSpamDir, false);

            //Calculate probability of each word
            spamDetector.CalculateProbabilities();
            //Write results to file
            spamDetector.OutputTrainingToFile(outFile);
	    System.out.println("Training Finished");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Test(){
        try {
            //Test files
            spamDetector.TestSpam(testHamDir, true);
            spamDetector.TestSpam(testSpamDir, false);

            //Get the results and set them to the table
            this.files.setItems(spamDetector.getAllFiles());

            float accuracy = spamDetector.GetAccuracy();
            labelCorrectGuesses.setText("Accuracy: " + accuracy);

            float precision = spamDetector.GetPrecision();
            labelPrecision.setText("Precision: " + precision);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Application.launch(args);
    }


}
