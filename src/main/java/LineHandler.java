import java.util.concurrent.ConcurrentHashMap;

public class LineHandler {
    private final ConcurrentHashMap<String, WordInfo> wordInfoHashMap = new ConcurrentHashMap<>();
    private String lastWord = null;

    public ConcurrentHashMap<String, WordInfo> getWordInfoHashMap() {
        return wordInfoHashMap;
    }

    public void handle(String line) {
        String lastWord = "";

        int wordBegin = -1;
        char[] chars = line.toCharArray();
        for (int i = 0, charArrayLength = chars.length; i < charArrayLength; i++) {
            if (charBelongsToWord(chars[i])) {
                // here begins a word:
                if (wordBegin == -1) wordBegin = i;
            } else if (wordBegin != -1){
                String word = line.substring(wordBegin, i);
                handleWord(word);
                wordBegin = -1;
            }

        }
    }

    private static boolean charBelongsToWord(char ch) {
        return ch == '-' || Character.isLetter(ch);
    }

    private void handleWord(String currentWord) {
        if (currentWord.length() < 2) {
            lastWord = null;
            return;
        }

        if (wordInfoHashMap.containsKey(currentWord)) {
            // increase the count, because map contains word
            wordInfoHashMap.get(currentWord).increaseCount();
        } else {
            // create new word info and add it to map
            wordInfoHashMap.put(currentWord, new WordInfo(currentWord));
        }

        if (lastWord == null) return;

        // map contains key because last word gets set after putting it in the map
        wordInfoHashMap.get(lastWord).addNextWord(currentWord);

        // set new last word
        lastWord = currentWord;
    }

}
