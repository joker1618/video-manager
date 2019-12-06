package xxx.joker.libs.core.test;

import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.file.JkFiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JkDataTest {

    private static final String FN_NAMES = "/testData/names.csv";
    private static final String FN_COUNTRIES = "/testData/countries.csv";

    private Random random;
    private List<String> names;
    private List<String> countries;

    public JkDataTest() {
        this(System.currentTimeMillis());
    }
    public JkDataTest(long seed) {
        random = new Random(seed);
        names = JkFiles.readLines(getClass().getResourceAsStream(FN_NAMES));
        countries = JkFiles.readLines(getClass().getResourceAsStream(FN_COUNTRIES));
    }

    public String nextName() {
        int index = random.nextInt(names.size());
        return names.get(index);
    }
    public List<String> nextNames(int num) {
        return Stream.generate(this::nextName).limit(num).collect(Collectors.toList());
    }

    public String nextCountry() {
        int index = random.nextInt(countries.size());
        return countries.get(index);
    }
    public List<String> nextCountries(int num) {
        return Stream.generate(this::nextCountry).limit(num).collect(Collectors.toList());
    }

    public boolean nextBoolean() {
        return random.nextDouble() < 0.5d;
    }
    public List<Boolean> nextBooleans(int num) {
        return Stream.generate(this::nextBoolean).limit(num).collect(Collectors.toList());
    }

    public int nextInt() {
        return random.nextInt();
    }
    public List<Integer> nextInts(int num) {
        return Stream.generate(this::nextInt).limit(num).collect(Collectors.toList());
    }
    public int nextInt(int upperBound) {
        return random.nextInt(upperBound);
    }
    public List<Integer> nextInts(int num, int upperBound) {
        return Stream.generate(() -> nextInt(upperBound)).limit(num).collect(Collectors.toList());
    }

    public long nextLong() {
        return random.nextLong();
    }
    public List<Long> nextLongs(long num) {
        return Stream.generate(this::nextLong).limit(num).collect(Collectors.toList());
    }
    public long nextLong(int upperBound) {
        return random.nextLong();
    }
    public List<Long> nextLongs(long num, int upperBound) {
        return Stream.generate(() -> nextLong(upperBound)).limit(num).collect(Collectors.toList());
    }

    public float nextFloat() {
        return random.nextFloat();
    }
    public List<Float> nextFloats(long num) {
        return Stream.generate(this::nextFloat).limit(num).collect(Collectors.toList());
    }
    public float nextFloat(float mult) {
        return random.nextFloat() * mult;
    }
    public List<Float> nextFloats(long num, float mult) {
        return Stream.generate(() -> nextFloat(mult)).limit(num).collect(Collectors.toList());
    }

    public double nextDouble() {
        return random.nextDouble();
    }
    public List<Double> nextDoubles(long num) {
        return Stream.generate(this::nextDouble).limit(num).collect(Collectors.toList());
    }
    public double nextDouble(double mult) {
        return random.nextDouble() * mult;
    }
    public List<Double> nextDoubles(long num, double mult) {
        return Stream.generate(() -> nextDouble(mult)).limit(num).collect(Collectors.toList());
    }

//    public LocalDateTime nextLdt() {
//        long now = JkDateTime.now().totalMillis();
//        long max = now + 1000L * 60 * 60 * 24 * 365 * 30;
//        long numMs = Math.abs(random.nextLong()) % max;
//        return JkDateTime.of(numMs).getDateTime();
//    }

    public <T> List<T> nextElements(Supplier<T> supplier, int times) {
        List<T> toRet = new ArrayList<>();
        for(int i = 0; i < times; i++) {
            toRet.add(supplier.get());
        }
        return toRet;
    }
}
