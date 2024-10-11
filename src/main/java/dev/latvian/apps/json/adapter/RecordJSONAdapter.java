package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;
import dev.latvian.apps.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class RecordJSONAdapter implements JSONAdapter<Object> {
	private record JSONRecordComponent(int index, String name, Class<?> type, Type adaptType, boolean optional, Method accessor) {
	}

	private final Class<?> type;
	private JSONRecordComponent[] components;
	private Constructor<?> constructor;

	RecordJSONAdapter(Class<?> type) {
		this.type = type;
	}

	private JSONRecordComponent[] components() {
		if (components == null) {
			var rc = type.getRecordComponents();
			components = new JSONRecordComponent[rc.length];

			for (int i = 0; i < rc.length; i++) {
				var c = rc[i];
				boolean optional = c.getType() == Optional.class;

				if (optional && c.getGenericType() instanceof ParameterizedType t && t.getActualTypeArguments().length == 1) {
					components[i] = new JSONRecordComponent(i, c.getName(), c.getType(), t.getActualTypeArguments()[0], true, c.getAccessor());
				} else {
					components[i] = new JSONRecordComponent(i, c.getName(), c.getType(), c.getGenericType(), false, c.getAccessor());
				}
			}
		}

		return components;
	}

	@Override
	public Object adapt(JSON json, Object jsonValue, Type genericType) {
		if (jsonValue instanceof Map<?, ?> object) {
			var components = components();

			if (constructor == null) {
				try {
					constructor = type.getConstructor(Arrays.stream(components).map(JSONRecordComponent::type).toArray(Class[]::new));
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("No record constructor for type '" + type.getName() + "'", e);
				}
			}

			try {
				var args = new Object[components.length];

				for (int i = 0; i < args.length; i++) {
					var j = object.get(components[i].name);

					if (j != null && j != JSON.NULL) {
						if (components[i].optional) {
							args[i] = Optional.ofNullable(json.adapt(j, components[i].adaptType));
						} else {
							args[i] = json.adapt(j, components[i].adaptType);
						}
					} else if (components[i].optional) {
						args[i] = Optional.empty();
					}
				}

				return constructor.newInstance(args);
			} catch (Exception e) {
				throw new RuntimeException("Error reading '" + type.getName() + "' JSON", e);
			}
		} else {
			throw new IllegalArgumentException("Expected JSON object for type '" + type.getName() + "'");
		}
	}

	@Override
	public void write(JSON json, Writer writer, Object value, int depth, boolean pretty) throws IOException {
		var components = components();
		var obj = JSONObject.of(components.length);

		for (var rc : components) {
			try {
				var o = rc.accessor.invoke(value);

				if (o instanceof Optional<?> op) {
					if (op.isPresent()) {
						obj.put(rc.name, op.get());
					}
				} else {
					obj.put(rc.name, o);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Failed to access record component '" + rc.name + "'", ex);
			}
		}

		json.write(writer, obj, depth, pretty);
	}
}
