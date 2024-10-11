package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReflectionJSONAdapter implements JSONAdapter<Object> {
	private final Class<?> type;
	private Constructor<?> constructor;
	private Map<String, Field> fields;

	ReflectionJSONAdapter(Class<?> type) {
		this.type = type;
		this.fields = null;
	}

	private Map<String, Field> fields() {
		if (fields == null) {
			fields = new LinkedHashMap<>();

			for (var f : type.getDeclaredFields()) {
				if (f.isSynthetic() || Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
					continue;
				}

				f.setAccessible(true);
				fields.put(f.getName(), f);
			}
		}

		return fields;
	}

	@Override
	public Object adapt(JSON json, Object jsonValue, Type genericType) {
		if (jsonValue instanceof Map<?, ?> object) {
			if (constructor == null) {
				try {
					constructor = type.getDeclaredConstructor();
					constructor.setAccessible(true);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("No default constructor for type '" + type.getName() + "'", e);
				}
			}

			try {
				var o = constructor.newInstance();

				for (var entry : object.entrySet()) {
					if (entry.getValue() != null && entry.getValue() != JSON.NULL) {
						var f = fields().get(String.valueOf(entry.getKey()));

						if (f != null) {
							f.set(o, json.adapt(entry.getValue(), f.getType()));
						}
					}
				}

				return o;
			} catch (Exception e) {
				throw new RuntimeException("Error reading '" + type.getName() + "' JSON", e);
			}
		} else {
			throw new IllegalArgumentException("Expected JSON object for type '" + type.getName() + "'");
		}
	}

	@Override
	public void write(JSON json, Writer writer, Object value, int depth, boolean pretty) throws IOException {
		throw new UnsupportedOperationException("Reflection JSON for type '" + type.getName() + "' not supported yet");
	}
}
