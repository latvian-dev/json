package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;
import dev.latvian.apps.json.JSONArray;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class CollectionJSONAdapter implements JSONAdapter<Iterable<?>> {
	public interface Factory {
		Factory LIST = ArrayList::new;
		Factory LINKED_LIST = s -> new LinkedList<>();
		Factory LINKED_SET = LinkedHashSet::new;

		Collection<?> create(int size);
	}

	private final Class<?> type;
	private final Factory factory;

	CollectionJSONAdapter(Class<?> type, Factory factory) {
		this.type = type;
		this.factory = factory;
	}

	@Override
	public Iterable<?> adapt(JSON json, Object jsonValue, Type genericType) {
		if (jsonValue instanceof JSONArray jsonArray) {
			var collection = factory.create(jsonArray.size());
			var valueType = genericType instanceof ParameterizedType pt ? pt.getActualTypeArguments()[0] : null;

			for (var value : jsonArray) {
				collection.add(json.adapt(value, valueType));
			}

			return collection;
		}

		throw new IllegalArgumentException("Expected JSON array for type '" + type.getName() + "'");
	}
}
