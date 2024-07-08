package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.BatchCollector;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class StreamUtils {
    public static <T> Collector<T, List<T>, List<T>> batchCollector(int batchSize, Consumer<List<T>> batchProcessor) {
        return new BatchCollector<T>(batchSize, batchProcessor);
    }
}
