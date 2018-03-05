import java.math.RoundingMode;
import java.text.DecimalFormat;

public class WordInfo {

    String word;
    int hamCount; //number of times the word occurs in ham
    int spamCount; //number of times the word occurs in spam
    double prWordInHam; //probability of word appearing in ham file (# of ham files containing w/ # of ham files)
    double prWordInSpam; //probability of word appearing in spam file (# of spam files containing w/ # of spam files)
    double probabilityOfSpam; //probability of spam

    public WordInfo(String _word){
        word = _word;
        hamCount = 0;
        spamCount = 0;
        prWordInHam = 0;
        prWordInSpam = 0;
        probabilityOfSpam = 0;
    }

    public void CalculateProbability(int totalHam, int totalSpam) {
        prWordInHam = (float)hamCount /(float)totalHam;
        prWordInSpam = (float)spamCount  / (float)totalSpam;

        if(prWordInSpam + prWordInHam > 0) {
            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);
            probabilityOfSpam = prWordInSpam / (prWordInSpam + prWordInHam);
            probabilityOfSpam = Double.parseDouble(df.format((probabilityOfSpam)));
        }
    }

    public void incrementHamCount(){
        hamCount++;
    }

    public void incrementSpamCount(){
        spamCount++;
    }

    public String getWord(){return word;}
    public int getHamCount(){return hamCount;}
    public int getSpamCount(){return spamCount;}
    public double getPrWordInHam(){ return prWordInHam;}
    public double getPrWordInSpam(){ return prWordInSpam;}
    public double getProbabilityOfSpam(){return probabilityOfSpam;}
}
