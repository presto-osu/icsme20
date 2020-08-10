package presto.privaid.firebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dictionary {
    protected String event_name; // each event corresponds to one dictionary
    protected Map<String, Set<String>> categoricalAttributeAndValues; // attr -> set of possible values

    public Dictionary(String event_name) {
        this.event_name = event_name;
        categoricalAttributeAndValues = new HashMap<>();
    }

    public Dictionary(String event_name, Set<String> attributes) {
        this.event_name = event_name;
        categoricalAttributeAndValues = new HashMap<>();
        for (String attr : attributes) categoricalAttributeAndValues.put(attr, new HashSet<String>());
    }

    public boolean addCategoricalAttribute(String attr) {
        if (categoricalAttributeAndValues.containsKey(attr)) return false;
        categoricalAttributeAndValues.put(attr, new HashSet<String>());
        return true;
    }

    public boolean addCategoricalAttributeValue(String attr, String value) {
        if (!categoricalAttributeAndValues.containsKey(attr)) return false;
        return categoricalAttributeAndValues.get(attr).add(value);
    }

    public Set<String> getCategoricalAttributeValues(String attr) {
        return categoricalAttributeAndValues.get(attr);
    }

    @Override
    public String toString() {
        return "[" + event_name + "=" + categoricalAttributeAndValues + "]";
    }
}
