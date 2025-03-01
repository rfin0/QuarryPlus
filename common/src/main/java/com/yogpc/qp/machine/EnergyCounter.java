package com.yogpc.qp.machine;

import com.yogpc.qp.QuarryPlus;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

public abstract class EnergyCounter {
    private static final Logger LOGGER = QuarryPlus.LOGGER;
    private static final Marker MARKER_TICK = MarkerFactory.getMarker("TickLog");
    private static final Marker MARKER_FINAL = MarkerFactory.getMarker("Total");
    final String name;
    final long logInterval;

    public EnergyCounter(String name) {
        this.name = name;
        logInterval = 20 * 5;
    }

    public static EnergyCounter createInstance(boolean isDebug, String name) {
        if (isDebug) return new Debug(name);
        else return new Production();
    }

    public abstract void logOutput(long time);

    public abstract void logUsageMap();

    public abstract void useEnergy(LongSupplier time, long amount, String reason);

    public abstract void getEnergy(LongSupplier time, long amount);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", logInterval=" + logInterval +
            '}';
    }

    private static class Debug extends EnergyCounter {
        private long lastLogTick;
        private final Map<Long, Long> useCounter = new HashMap<>();
        private final Map<Long, Long> getCounter = new HashMap<>();
        private final Map<String, Long> usageMap = new HashMap<>();

        public Debug(String name) {
            super(name);
        }

        @Override
        public void logOutput(long time) {
            if (time - lastLogTick >= logInterval) {
                lastLogTick = time;
                var use = useCounter.values().stream().collect(Collectors.summarizingLong(Long::longValue));
                var get = getCounter.values().stream().collect(Collectors.summarizingLong(Long::longValue));
                if (use.getSum() != 0 && get.getSum() != 0)
                    LOGGER.info(MARKER_TICK, "{}: Used {} FE in {} ticks({} FE/t). Got {} FE in {} ticks({} FE/t).", name,
                        formatEnergyInFE(use.getSum()), use.getCount(), formatEnergyInFE(use.getAverage() / PowerEntity.ONE_FE),
                        formatEnergyInFE(get.getSum()), get.getCount(), formatEnergyInFE(get.getAverage() / PowerEntity.ONE_FE));
                useCounter.clear();
                getCounter.clear();
            }
        }

        @Override
        public void logUsageMap() {
            usageMap.entrySet().stream()
                .map(e -> "%s -> %s".formatted(e.getKey(), formatEnergyInFE(e.getValue())))
                .forEach(s -> LOGGER.info(MARKER_FINAL, s));
            usageMap.clear();
        }

        private void checkTime(long time, String name) {
            if (lastLogTick == 0) {
                lastLogTick = time;
            } else if (time - lastLogTick > logInterval) {
                LOGGER.warn(MARKER_TICK, "The last log time reset? Last: {}, Now({}): {}", lastLogTick, name, time);
            }
        }

        @Override
        public void useEnergy(LongSupplier timeGetter, long amount, String reason) {
            var time = timeGetter.getAsLong();
            checkTime(time, "USE");
            useCounter.merge(time, amount, Long::sum);
            usageMap.merge(reason, amount, Long::sum);
        }

        @Override
        public void getEnergy(LongSupplier timeGetter, long amount) {
            var time = timeGetter.getAsLong();
            checkTime(time, "GET");
            getCounter.merge(time, amount, Long::sum);
        }
    }

    private static class Production extends EnergyCounter {

        public Production() {
            super("Production");
        }

        @Override
        public void logOutput(long time) {
        }

        @Override
        public void logUsageMap() {
        }

        @Override
        public void useEnergy(LongSupplier timeGetter, long amount, String reason) {
        }

        @Override
        public void getEnergy(LongSupplier timeGetter, long amount) {
        }
    }

    public static String formatEnergyInFE(long energy) {
        return formatEnergyInFE((double) energy / PowerEntity.ONE_FE);
    }

    public static String formatEnergyInFE(double energy) {
        return String.format("%.3f", energy);
    }
}
