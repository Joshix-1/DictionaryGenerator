import dumonts.hunspell.Hunspell;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    static String hunspellDic = "", hunspellAff = "";
    static int threadCount = 0;
    static String fileName = "";


    public static void main(String[] args) throws java.io.IOException {
        if (args.length < 2) {
            System.out.println("first argument: path to hunspell dic\n"
                            + "second argument: count of threads to use\n"
                            + "third argument: file to read");
        }

        hunspellDic = args[0];
        hunspellAff = hunspellDic.substring(0, hunspellDic.length() - 3) + "aff";

        threadCount = Integer.parseInt(args[1]);
        fileName = args[2];

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        LineHandler lineHandler = new LineHandler();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        System.setOut(new PrintStream("output.txt"));

        int i = 0;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.length() > 0 && Character.isLetter(line.charAt(0))) {
                String finalLine = line;
                executorService.submit(() -> {
                    lineHandler.handle(finalLine);
                });

                //only for debugging:
                if (i++ > 1000) {
                    break;
                }
            }
        }

        // Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.
        executorService.shutdown();

        ScheduledExecutorService waiter = Executors.newSingleThreadScheduledExecutor();
        waiter.scheduleAtFixedRate(() -> {
            // Returns true if all tasks have completed following shut down.
            if (executorService.isTerminated()) {
                printResult(lineHandler);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    private static void printResult(LineHandler lineHandler) {
        List<WordInfo> words = lineHandler
                .getWordInfoHashMap()
                .values()
                .stream()
                .sorted(Comparator.comparing(WordInfo::getCount))
                .collect(Collectors.toList());

        Hunspell hunspell = new Hunspell(Paths.get(hunspellDic), Paths.get(hunspellAff));
        words.stream()
                .filter(wordInfo -> hunspell.spell(wordInfo.getWord()))
                .forEach(System.out::println);

        System.exit(0);
    }
}