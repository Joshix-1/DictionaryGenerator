import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WordInfo {
    private final static ConcurrentHashMap<String, Integer> WORDS = new ConcurrentHashMap<>();

    private static int wordToInt(String word) {
        if (!WORDS.containsKey(word)) {
            WORDS.put(word, WORDS.size());
        }
        return WORDS.get(word);
    }

    private static String[] wordArr = new String[0];
    synchronized private static String intToWord(int w) {
        if (wordArr.length == WORDS.size()) {
            return wordArr[w];
        }

        wordArr = new String[WORDS.size()];
        WORDS.forEach((str, i) -> {
            wordArr[i] = str;
        });

        return wordArr[w];
    }

    private final int word;
    private final ConcurrentHashMap<Integer, Integer> nextWords = new ConcurrentHashMap<>();

    private int count;

    public WordInfo(String word) {
        this.word = wordToInt(word);
        count = 1;
    }

    public int getCount() {
        return count;
    }

    public String getWord() {
        return intToWord(word);
    }

    synchronized public void increaseCount() {
        count++;
    }

    synchronized public void addNextWord(String word) {
        int w = wordToInt(word);
        nextWords.put(w, nextWords.getOrDefault(w, 0) + 1);
    }

    public String getNextWordsAsString(HashMap<String, String> wordsIndex) {
        if (nextWords.size() == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        nextWords.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    String word = intToWord(entry.getKey());
                    sb.append(wordsIndex.getOrDefault(word, word))
                            .append(":")
                            .append(entry.getValue())
                            .append(",");
        });
        sb.delete(sb.length() - 1, sb.length()) // remove ", " in the end.
                .append("}");

        return sb.toString();
    }

    public String toString(HashMap<String, String> wordsIndex) {
        String word = getWord();
        return wordsIndex.get(word) + ":" + word + ";" + count + ";" + getNextWordsAsString(wordsIndex);
    }
}
