package dev.latvian.apps.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONObject extends LinkedHashMap<String, Object> implements JSONSerializable {
	public static JSONObject of() {
		return new JSONObject(8);
	}

	public static JSONObject of(int initialCapacity) {
		return new JSONObject(initialCapacity);
	}

	public static JSONObject of(Map<String, ?> map) {
		var o = new JSONObject(map.size());
		o.putAll(map);
		return o;
	}

	public static JSONObject of(String key, Object value) {
		return new JSONObject(1).append(key, value);
	}

	public static JSONObject of(String key1, Object value1, String key2, Object value2) {
		return new JSONObject(2).append(key1, value1).append(key2, value2);
	}

	public static JSONObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
		return new JSONObject(3).append(key1, value1).append(key2, value2).append(key3, value3);
	}

	private JSONObject(int initialCapacity) {
		super(initialCapacity);
	}

	JSONObject(String key) {
	}

	@Override
	public final Object toJSON() {
		return this;
	}

	@Override
	public Object get(Object key) {
		var o = super.get(key);
		return o == JSON.NULL ? null : o;
	}

	public JSONObject append(String key, Object value) {
		put(key, value);
		return this;
	}

	public JSONObject asObject(String key) {
		return (JSONObject) get(key);
	}

	public JSONObject addObject(String key) {
		return (JSONObject) computeIfAbsent(key, JSONObject::new);
	}

	public JSONArray asArray(String key) {
		var o = get(key);
		return o instanceof JSONArray a ? a : JSONArray.of(o);
	}

	public JSONArray addArray(String key) {
		return (JSONArray) computeIfAbsent(key, JSONArray::new);
	}

	public String asString(String key, String def) {
		return String.valueOf(getOrDefault(key, def));
	}

	public String asString(String key) {
		return asString(key, "");
	}

	public Number asNumber(String key) {
		return (Number) get(key);
	}

	public Number asNumber(String key, Number def) {
		var o = get(key);

		if (o == null) {
			return def;
		} else if (o instanceof Number n) {
			return n;
		} else if (o instanceof CharSequence) {
			try {
				return Double.parseDouble(o.toString());
			} catch (NumberFormatException e) {
				return def;
			}
		} else {
			return def;
		}
	}

	public int asInt(String key, int def) {
		return asNumber(key, def).intValue();
	}

	public int asInt(String key) {
		return asInt(key, 0);
	}

	public long asLong(String key, long def) {
		return asNumber(key, def).longValue();
	}

	public long asLong(String key) {
		return asLong(key, 0L);
	}

	public double asDouble(String key, double def) {
		return asNumber(key, def).doubleValue();
	}

	public double asDouble(String key) {
		return asDouble(key, 0D);
	}

	public boolean asBoolean(String key, boolean def) {
		return (boolean) getOrDefault(key, def);
	}

	public boolean asBoolean(String key) {
		return asBoolean(key, false);
	}

	@Override
	public String toString() {
		return JSON.DEFAULT.write(this);
	}

	public String toPrettyString() {
		return JSON.DEFAULT.writePretty(this);
	}

	public boolean removeDeep(DeepRemovePredicate predicate, boolean removeEmpty) {
		boolean modified = false;
		var it = entrySet().iterator();

		while (it.hasNext()) {
			var e = it.next();
			var v = e.getValue();

			if (predicate.remove(e.getKey(), -1, v)) {
				modified = true;
				it.remove();
			} else if (v instanceof JSONObject o) {
				if (o.removeDeep(predicate, removeEmpty)) {
					modified = true;
				}

				if (removeEmpty && o.isEmpty()) {
					modified = true;
					it.remove();
				}
			} else if (v instanceof JSONArray a) {
				if (a.removeDeep(predicate, removeEmpty)) {
					modified = true;
				}

				if (removeEmpty && a.isEmpty()) {
					modified = true;
					it.remove();
				}
			}
		}

		return modified;
	}
}
