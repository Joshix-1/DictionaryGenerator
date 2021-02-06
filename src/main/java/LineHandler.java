import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LineHandler {
    private  static final int LINES_IN_THREAD = 1000;
    private final HashMap<String, WordInfo> wordInfoHashMap = new HashMap<>();
    private final List<CompletableFuture<Void>> futures = new LinkedList<>();
    private final ExecutorService executorService;
    public LineHandler(int threadCount) {
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    private int linesIndex = 0;
    private final String[] lines = new String[LINES_IN_THREAD];
    public void handle(String str) {
        lines[linesIndex++] = str;
        if (linesIndex >= LINES_IN_THREAD) {
            futures.add(LineHandlerThread.handle(Arrays.copyOf(lines, linesIndex), executorService)
                    .thenAccept(this::mergeWordInfoMap));
            linesIndex = 0;
        }
    }

    public void handleNow() {
        futures.add(LineHandlerThread.handle(Arrays.copyOf(lines, linesIndex), executorService)
                .thenAccept(this::mergeWordInfoMap));
        linesIndex = 0;
    }

    synchronized private void mergeWordInfoMap(HashMap<String, WordInfo> wordInfoHashMap1) {

    }

    public void onReady(Consumer<HashMap<String, WordInfo>> consumer) {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept((V) -> {
                    consumer.accept(wordInfoHashMap);
        });
    }
}
