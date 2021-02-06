import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class LineHandlerThread {

    private final HashMap<String, WordInfo> wordInfoHashMap = new HashMap<>();
    private String lastWord = null;

    public static CompletableFuture<HashMap<String, WordInfo>> handle(String[] lines, ExecutorService executorService) {
        CompletableFuture<HashMap<String, WordInfo>> future = new CompletableFuture<>();
        executorService.submit(() -> {
            LineHandlerThread lineHandlerThread = new LineHandlerThread();
            for (String line : lines) {
                lineHandlerThread.handle(line);
            }
            future.complete(lineHandlerThread.wordInfoHashMap);
        });
        return future;
    }

    private void handle(String line) {
        line = replace(line);
        // debug:
        //System.out.println(line);

        int wordBegin = -1;
        char[] chars = line.toCharArray();
        for (int i = 0, charArrayLength = chars.length; i < charArrayLength; i++) {
            char ch = chars[i];
            if (wordBegin == -1) { // currently outside of word:
                if (Character.isLetter(ch)) { //word begins here:
                    wordBegin = i;
                } else if (charStopsWordFlow(ch)) {
                    //last word isn't relevant anymore
                    lastWord = null;
                }
            } else { // inside of word:
                if (!charBelongsToWord(ch)) { // word is over
                    String word = line.substring(wordBegin, i);
                    handleWord(word);

                    // reset word begin:
                    wordBegin = -1;
                }
            }
        }
    }

    private static final Pattern BRACKETS = Pattern.compile("(?:\\[\\[)|(?:]])");
    private static final Pattern AMP = Pattern.compile("&amp;(?:\\w+;)?");
    // &lt;ref name=&quot;Low&quot;&gt;{{Literatur |Autor=George C. Low |DOI=10.1016/S0035-9203(16)90068-3}}&lt;/ref&gt;
    private static final Pattern LT_REF = Pattern.compile("&lt;ref.+;&gt;\\{\\{.+}}&lt;/ref&gt");
    private static final Pattern LG_GT = Pattern.compile("&lt;\\w{3,4}&gt;.+&lt;/\\w{3,4}&gt");
    private static final Pattern PARANTHESES = Pattern.compile("\\(.+\\)");
    private static String replace(String line) {
        line = BRACKETS.matcher(line).replaceAll("");
        line = AMP.matcher(line).replaceAll("");
        line = LT_REF.matcher(line).replaceAll("");
        line = LG_GT.matcher(line).replaceAll("");
        line = PARANTHESES.matcher(line).replaceAll("");

        return line;
    }

    private static boolean charBelongsToWord(char ch) {
        return ch == '-' || Character.isLetter(ch);
    }

    private static boolean charStopsWordFlow(char ch) {
        return ch == '.'
                || ch == ','
                || ch == '!'
                || ch == '?'
                || ch == ';';
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

        if (lastWord == null) {
            lastWord = currentWord;
            return;
        }

        // map contains key because last word gets set after putting it in the map
        wordInfoHashMap.get(lastWord).addNextWord(currentWord);

        // set new last word
        lastWord = currentWord;
    }

}
