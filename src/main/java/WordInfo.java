import java.util.concurrent.ConcurrentHashMap;

public class WordInfo {
    private final String word;
    private final ConcurrentHashMap<String, Integer> nextWords = new ConcurrentHashMap<>();
    private int count;

    public WordInfo(String word) {
        this.word = word;
        count = 1;
    }

    public String getWord() {
        return word;
    }

    synchronized public void increaseCount() {
        count++;
    }

    synchronized public void addNextWord(String word) {
        nextWords.put(word, nextWords.getOrDefault(word, 0) + 1);
    }
}
