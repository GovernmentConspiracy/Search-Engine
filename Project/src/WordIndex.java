import java.util.*;

/**
 * A special type of {@link Index} that indexes the locations words were found.
 */
public class WordIndex implements Index<String>{
    private final Map<String, Set<Integer>> map;

    public WordIndex() {
        map = new HashMap<>() {
            @Override
            public Set<String> keySet() {
                return Collections.unmodifiableSet(super.keySet());
            }
            @Override
            public Collection<Set<Integer>> values() {
                return Collections.unmodifiableCollection(super.values());
            }
            @Override
            public Set<Map.Entry<String, Set<Integer>>> entrySet() {
                return Collections.unmodifiableSet(super.entrySet());
            }
        };
    }

    @Override
    public boolean add(String element, int position) {
        map.putIfAbsent(element, new TreeSet<>());
        return map.get(element).add(position);
    }

    @Override
    public int numPositions(String element) {
        Set<Integer> set = map.get(element);
        if (set != null)
            return set.size();
        return 0;
    }

    @Override
    public int numElements() {
        return map.size();
    }

    @Override
    public boolean contains(String element) {
        return map.containsKey(element);
    }

    @Override
    public boolean contains(String element, int position) {
        Set<Integer> set = map.get(element);
        if (set != null)
            return set.contains(position);
        return false;
    }

    @Override
    public Collection<String> getElements() {
        return map.keySet();
    }

    @Override
    public Collection<Integer> getPositions(String element) {
        Set<Integer> set = map.get(element);
        if (set != null)
            return Collections.unmodifiableSet(set);
        return Collections.emptySet();
    }
}
