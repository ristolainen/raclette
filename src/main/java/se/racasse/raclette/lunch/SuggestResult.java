package se.racasse.raclette.lunch;

import com.google.common.collect.ImmutableSet;
import se.racasse.raclette.place.PlaceScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SuggestResult {

    public List<PlaceScore> scores;

    public Optional<PlaceScore> top() {
        return scores.stream().findFirst();
    }

    static Collector<PlaceScore, ?, SuggestResult> collector() {
        return new SuggestResultCollector();
    }

    private static class SuggestResultCollector implements Collector<PlaceScore, SuggestResult, SuggestResult> {

        @Override
        public Supplier<SuggestResult> supplier() {
            return () -> {
                final SuggestResult suggestResult = new SuggestResult();
                suggestResult.scores = new ArrayList<>();
                return suggestResult;
            };
        }

        @Override
        public BiConsumer<SuggestResult, PlaceScore> accumulator() {
            return (suggestResult, placeScore) -> suggestResult.scores.add(placeScore);
        }

        @Override
        public BinaryOperator<SuggestResult> combiner() {
            return (suggestResult, suggestResult2) -> {
                suggestResult.scores.addAll(suggestResult2.scores);
                return suggestResult;
            };
        }

        @Override
        public Function<SuggestResult, SuggestResult> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
        }
    }

}
