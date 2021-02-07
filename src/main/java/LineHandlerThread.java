import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class LineHandlerThread {
    private final HashMap<String, Integer> words = new HashMap<>();
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

    /*
    Character escapes in xml:
        "   &quot;
        '   &apos;
        <   &lt;
        >   &gt;
        &   &amp;
     */
    private static final Pattern NBSP = Pattern.compile("&nbsp");
    private static final Pattern CHARS_TO_STRIP = Pattern.compile("[;']");

    private static final Pattern DOUBLE_QUOTE = Pattern.compile("&quot");
    private static final Pattern SINGLE_QUOTE = Pattern.compile("&apos");
    private static final Pattern LOWER_THAN = Pattern.compile("&lt");
    private static final Pattern GREATER_THAN = Pattern.compile("&gt");
    private static final Pattern AND = Pattern.compile("&amp");
    private static final Pattern HTML_REPLACE = Pattern.compile("<(\\w{2,4})[^>]*>(.*)</\\1[^>]*>");
    private static final Pattern HTML_REPLACE2 = Pattern.compile("</?(\\w{2,4})[^>]*>");
    private static final Pattern HTML_REF = Pattern.compile("<ref[^>]*>[^<]*</ref[^>]*>");
    private static final Pattern RENAMED_LINK = Pattern.compile("\\[\\[([^\\[\\]|]+)\\|[^\\[\\]|]+]]"); // with |
    private static final Pattern RENAMED_LINK2 = Pattern.compile("\\[\\[([^\\[\\]|]+),[^\\[\\]|]+]]"); // with ,
    private static final Pattern HIDE_NAMESPACE = Pattern.compile("\\[\\[[^\\[\\]|]+:([^\\[\\]|]+)\\|]]");
    private static final Pattern PARENTHESES = Pattern.compile("\\(.+\\)");
    private static final Pattern BRACKETS = Pattern.compile("[\\[\\]{}]");

    static String replace(String line) {
        // replace simple replacements:
        line = AND.matcher(line).replaceAll("&");
        line = NBSP.matcher(line).replaceAll(" ");
        line = CHARS_TO_STRIP.matcher(line).replaceAll("");
        line = PARENTHESES.matcher(line).replaceAll("");
        // replace wiki stuff: (https://en.wikipedia.org/wiki/Help:Wikitext#Layout)
        line = RENAMED_LINK.matcher(line).replaceAll(m -> m.group(1));
        line = RENAMED_LINK2.matcher(line).replaceAll(m -> m.group(1));
        line = HIDE_NAMESPACE.matcher(line).replaceAll(m -> m.group(1));
        // reescape chars
        line = DOUBLE_QUOTE.matcher(line).replaceAll("\"");
        line = SINGLE_QUOTE.matcher(line).replaceAll("'");
        line = LOWER_THAN.matcher(line).replaceAll("<");
        line = GREATER_THAN.matcher(line).replaceAll(">");
        // strip html-tags and content in it
        line = HTML_REF.matcher(line).replaceAll("");
        line = HTML_REPLACE.matcher(line).replaceAll(m -> m.group(2));
        line = HTML_REPLACE2.matcher(line).replaceAll("");
        // replace brackets:
        line = BRACKETS.matcher(line).replaceAll("");

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
