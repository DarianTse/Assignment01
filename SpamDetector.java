import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class SpamDetector {

    //Maps to store training results for both ham and spam. (key: word, value: WordInfo class to store frequency)
    private HashMap<String, WordInfo> trainingMap;

    //Total files of type read
    int totalHamFiles;
    int totalSpamFiles;

    //Results
    int numTrueHam;
    int numTrueSpam;
    int spamGuesses;
    int numGuesses;

    //List of test files
    ObservableList<TestFile> files = FXCollections.observableArrayList();

    public SpamDetector(){
        trainingMap = new HashMap<String, WordInfo>();

        totalHamFiles = 0;
        totalSpamFiles = 0;

        numTrueHam = 0;
        numTrueSpam = 0;
        spamGuesses = 0;
        numGuesses = 0;
    }

    public ObservableList<TestFile> getAllFiles() {
        return files;
    }

    //read through files and check for word. Using probabilities from the training map
    public void TestSpam(File file, Boolean isHam) throws FileNotFoundException {
        if(file.isDirectory()){
            File[] contents = file.listFiles();
            for(File current: contents){
                TestSpam(current, isHam);
            }
        }else if(file.exists()){
            //which words occur in the file
            List<WordInfo> wordOccurrences = new ArrayList<WordInfo>();

            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("[\\s|.|;|:|?|!|,]");

            while(scanner.hasNext()){
                String word = scanner.next();
                word = word.toLowerCase();

                //if this word is found in our trainingMap, add it to the List of wordOccurrences
                if(trainingMap.containsKey(word) && !wordOccurrences.contains(trainingMap.get(word))){
                    WordInfo wordInfo = trainingMap.get(word);
                    wordOccurrences.add(wordInfo);
                }
            }

            //Once we read through the file, use bayes formula. 1/1+e^(summation of log(1-pr.w)-log(pr.w))
            double summation = 0;
            for(WordInfo word: wordOccurrences){
                double prob = word.getProbabilityOfSpam();
                if(prob != 0){
                    summation += (Math.log((1.0d-prob)) - Math.log(prob));
                }
            }
            double probOfSpam = 1.0d/(1.0d + Math.pow(Math.E, summation));

           String actualClass = isHam ? "HAM" : "SPAM";
            //Create new TestFile and add to the observable list
            files.add(new TestFile(file.getName(), probOfSpam, actualClass));

            if(isHam && probOfSpam < 0.3) {
                numTrueHam++;
            }
            if(!isHam && probOfSpam >= 0.3){
                numTrueSpam++;
                spamGuesses++;
            }
            if(isHam && probOfSpam >= 0.3){
                spamGuesses++;
            }

            numGuesses++;
        }
    }

    public float GetAccuracy(){
        return ((float)numTrueHam + (float)numTrueSpam)/(float)numGuesses;
    }

    public float GetPrecision(){
        return (float)numTrueSpam/ (float)spamGuesses;
    }

    //Takes all files in directory and reads them. Goes through each word and adds them to a map. If it occurs in a spam file, spam counter increases for that word. If it occurs in a ham file, ham counter increases.
    public void MapData(File file, Boolean isHam) throws FileNotFoundException {
        if(file.isDirectory()){
            File[] contents = file.listFiles();
            for(File current: contents){
                MapData(current, isHam);
            }
        }
        else if(file.exists()){
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("[\\s|.|;|:|?|!|,]");

            //use to check for duplicate words in current file since we only want to count a word once per file.
            List<String> wordOccurrences = new ArrayList<>();

            while(scanner.hasNext()){

                String word = scanner.next();

                word = word.toLowerCase();
                WordInfo wordInfo = null;

                //check if its actually a word
                if(word.matches("^[a-zA-Z]+$")) {
                    //check if the word has occurred in this file before
                    if(!wordOccurrences.contains(word)) {
                        //check if the word has occurred in any file before
                        if (trainingMap.containsKey(word)) {
                            wordInfo = trainingMap.get(word);
                        } else {
                            wordInfo = new WordInfo(word);
                            trainingMap.put(word, wordInfo);
                        }
                        //increment number of occurrences word appears
                        if (isHam) {
                            wordInfo.incrementHamCount();
                        } else {
                            wordInfo.incrementSpamCount();
                        }
                        wordOccurrences.add(word);
                    }
                }
            }
            //increment total files read.
            if(isHam){
                totalHamFiles++;
            }else {
                totalSpamFiles++;
            }
        }
    }

    public void CalculateProbabilities(){
        for(String key: trainingMap.keySet()){
            trainingMap.get(key).CalculateProbability(totalHamFiles, totalSpamFiles);
        }
    }

    public void OutputTrainingToFile(File outFile) throws IOException {
        System.out.println("# of words: " + trainingMap.keySet().size());

        if (!outFile.exists()) {
            outFile.createNewFile();
            if (outFile.canWrite()) {
                PrintWriter fileOut = new PrintWriter(outFile);

                Set<String> keys = trainingMap.keySet();
                Iterator<String> keyIterator = keys.iterator();

                fileOut.println("Total Spam Files: " + totalSpamFiles + ". Total Ham Files: " + totalHamFiles);

                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    int hamCount = trainingMap.get(key).getHamCount();
                    int spamCount = trainingMap.get(key).getSpamCount();
                    double hamRate = trainingMap.get(key).getPrWordInHam();
                    double spamRate = trainingMap.get(key).getPrWordInSpam();
                    double probability = trainingMap.get(key).getProbabilityOfSpam();

                    fileOut.println(key + "; HamCount: " + hamCount + "; SpamCount: " + spamCount + "; Ham Rate: " + hamRate + "; Spam Rate: " + spamRate +  "; Probability of Spam: " + probability);
                }

                fileOut.close();
            } else {
                System.err.println("Error:  Cannot write to file: " + outFile.getAbsolutePath());
            }
        } else {
            System.err.println("Error:  File already exists: " + outFile.getAbsolutePath());
            System.out.println("outFile.exists(): " + outFile.exists());
            System.out.println("outFile.canWrite(): " + outFile.canWrite());
        }
    }

}
