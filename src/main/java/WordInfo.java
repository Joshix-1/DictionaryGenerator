import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class WordInfo {
    private final String word;
    private final HashMap<String, Integer> nextWords = new HashMap<>();
    private int count;

    public WordInfo(String word) {
        this.word = word;
        count = 1;
    }

    public int getCount() {
        return count;
    }

    public void increaseCount() {
        count++;
    }

    public void addNextWord(String word) {
        addNextWord(word, 1);
    }

    private void addNextWord(String word, int count) {
        nextWords.put(word, nextWords.getOrDefault(word, 0) + count);
    }

    public String toString(HashMap<String, String> wordsIndex) {
        return /*wordsIndex.get(word) + ":" +*/ word + ";" + count + ";" + getNextWordsAsString(wordsIndex);
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
                    sb.append(wordsIndex.getOrDefault(entry.getKey(), entry.getKey()))
                            .append(":")
                            .append(entry.getValue())
                            .append(",");
        });
        sb.delete(sb.length() - 1, sb.length()) // remove ", " in the end.
                .append("}");

        return sb.toString();
    }

    public void merge(WordInfo wordInfo) {
        if (getWord().equals(wordInfo.getWord())) {
            wordInfo.nextWords.forEach(this::addNextWord);
            count += wordInfo.count;
        }
    }

    public String getWord() {
        return word;
    }
}
