

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.io.FileReader;
import java.util.stream.Collectors;

public class Main {
    static final String OUTPUT_TXT = "output.txt";
    static int threadCount = 0;
    static String fileName = "";


    public static void main(String[] args) throws java.io.IOException {
        if (args.length < 2) {
            System.out.println("first argument: count of threads to use\n"
                            + "second argument: file to read");
        }

        threadCount = Integer.parseInt(args[0]);
        fileName = args[1];

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        LineHandler lineHandler = new LineHandler(threadCount);

        // debug:
        //System.setOut(new PrintStream("test.txt"));
        int i = 0;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String finalLine = line.trim();
            if (finalLine.length() > 0 && Character.isLetter(finalLine.charAt(0))) {
                //System.out.println(LineHandlerThread.replace(finalLine));
                lineHandler.handle(finalLine);

                if (i++ > 500) {
                    break;
                }
            }
        }
        lineHandler.handleNow();

        lineHandler.onReady(Main::printResult);
    }

    private static void printResult(HashMap<String, WordInfo> wordInfoHashMap) {
        List<WordInfo> words = wordInfoHashMap
                .values()
                .stream()
                .filter(wordInfo -> wordInfo.getCount() > 1)
                .sorted(Comparator.comparing(WordInfo::getCount).reversed())
                .collect(Collectors.toList());

        HashMap<String, String> wordIndex = new HashMap<>();

        for (int i = 0; i < words.size(); i++) {
            wordIndex.put(words.get(i).getWord(), Integer.toString(i, 10));
        }

        try {
            PrintWriter writer = new PrintWriter(OUTPUT_TXT, StandardCharsets.UTF_8);
            words.forEach(wordInfo -> {
                writer.println(wordInfo.toString(wordIndex));
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}