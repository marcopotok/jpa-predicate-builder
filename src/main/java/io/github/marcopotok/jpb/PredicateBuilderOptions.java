package io.github.marcopotok.jpb;

public class PredicateBuilderOptions {

    private final boolean joinCacheIsEnabled;
    private final PrefetchEngine prefetchEngine;

    private PredicateBuilderOptions(boolean joinCacheIsEnabled, PrefetchEngine prefetchEngine) {
        this.joinCacheIsEnabled = joinCacheIsEnabled;
        this.prefetchEngine = prefetchEngine;
    }

    public static PredicateBuilderOptions createDefault() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isJoinCacheIsEnabled() {
        return joinCacheIsEnabled;
    }

    public PrefetchEngine getPrefetchEngine() {
        return prefetchEngine;
    }

    public static final class Builder {
        private boolean joinCacheIsEnabled = true;
        private PrefetchEngine prefetchEngine = new DefaultPrefetchEngine();

        private Builder() {
        }

        public Builder withoutJoinsCache() {
            this.joinCacheIsEnabled = false;
            return this;
        }

        public Builder withPrefetchEngine(PrefetchEngine prefetchEngine) {
            this.prefetchEngine = prefetchEngine;
            return this;
        }

        public PredicateBuilderOptions build() {
            return new PredicateBuilderOptions(joinCacheIsEnabled, prefetchEngine);
        }
    }
}
