package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;
import dev.latvian.apps.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapJSONAdapter implements JSONAdapter<Map<String, ?>> {
	public interface Factory {
		Factory MAP = LinkedHashMap::new;
		Factory IDENTITY_MAP = IdentityHashMap::new;

		Map<String, ?> create(int size);
	}

	private final Class<?> type;
	private final Factory factory;

	MapJSONAdapter(Class<?> type, Factory factory) {
		this.type = type;
		this.factory = factory;
	}

	@Override
	public Map<String, ?> adapt(JSON json, Object jsonValue, Type genericType) {
		if (jsonValue instanceof JSONObject jsonObject) {
			var map = factory.create(jsonObject.size());
			var valueType = genericType instanceof ParameterizedType pt ? pt.getActualTypeArguments()[1] : null;

			for (var entry : jsonObject.entrySet()) {
				map.put(entry.getKey(), json.adapt(entry.getValue(), valueType));
			}

			return map;
		}

		throw new IllegalArgumentException("Expected JSON object for type '" + type.getName() + "'");
	}
}
