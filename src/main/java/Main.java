import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static String language = "";
    static int threadCount = 0;
    static String fileName = "";


    public static void main(String[] args) throws java.io.IOException {
        if (args.length < 2) {
            System.out.println("first argument: language for spell checking\n"
                            + "second argument: count of threads to use\n"
                            + "third argument: file to read");
        }

        language = args[0];
        threadCount = Integer.parseInt(args[1]);
        fileName = args[2];

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        LineHandler lineHandler = new LineHandler();
        ExecutorService executorService = Executors.newFixedThreadPool(7);

        System.setOut(new PrintStream("output.txt"));

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.length() > 0 && Character.isLetter(line.charAt(0))) {
                String finalLine = line;
                executorService.submit(() -> lineHandler.handle(finalLine));
            }
        }
    }
}


