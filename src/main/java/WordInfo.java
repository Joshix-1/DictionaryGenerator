import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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

    public int getCount() {
        return count;
    }

    synchronized public void increaseCount() {
        count++;
    }

    synchronized public void addNextWord(String word) {
        nextWords.put(word, nextWords.getOrDefault(word, 0) + 1);
    }

    public String getNextWordsAsString(HashMap<String, String> wordsIndex) {
        if (nextWords.size() == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        nextWords.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEach(entry -> {
            sb.append(wordsIndex.getOrDefault(entry.getKey(), entry.getKey()))
                    .append(":")
                    .append(entry.getValue())
                    .append(",");
        });
        sb.delete(sb.length() - 1, sb.length()) // remove ", " in the end.
                .append("}");

        return sb.toString();
    }

    public String toString(HashMap<String, String> wordsIndex) {
        return wordsIndex.get(word) + ":" + word + ";" + count + ";" + getNextWordsAsString(wordsIndex);
    }
}
