

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {
    static String outputFile = "output.txt";
    static String hunspellDic = "", hunspellAff = "";
    static int threadCount = 0;
    static String fileName = "";


    public static void main(String[] args) throws java.io.IOException {
        if (args.length < 2) {
            System.out.println("first argument: count of threads to use\n"
                            + "second argument: file to read");
        }

        hunspellDic = "args[0];";
        hunspellAff = hunspellDic.substring(0, hunspellDic.length() - 3) + "aff";

        threadCount = Integer.parseInt(args[0]);
        fileName = args[1];

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        LineHandler lineHandler = new LineHandler(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);


        int i = 0;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String finalLine = line.trim();
            if (finalLine.length() > 0 && Character.isLetter(finalLine.charAt(0))) {
                //System.out.println(finalLine);
                lineHandler.handle(finalLine);

                // only for debugging:
                if (i++ > 5_000) {
                    break;
                }
            }
        }
        lineHandler.handleNow();

        lineHandler.onReady(Main::printResult);
    }

    private static void printResult(HashMap<String, WordInfo> wordInfoHashMap) {
        //Hunspell hunspell = new Hunspell(Paths.get(hunspellDic), Paths.get(hunspellAff));

        List<WordInfo> words = wordInfoHashMap
                .values()
                .stream()
                .sorted(Comparator.comparing(WordInfo::getCount).reversed())
                ///.filter(wordInfo -> hunspell.spell(wordInfo.getWord()))
                .collect(Collectors.toList());

        HashMap<Integer, String> wordIndex = new HashMap<>();

        for (int i = 0; i < words.size(); i++) {
            wordIndex.put(words.get(i).getWordInt(), Integer.toString(i, 36));
        }

        try {
            PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
            words.forEach(wordInfo -> {
                writer.println(wordInfo.toString(wordIndex));
            });
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}