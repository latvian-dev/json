package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;
import dev.latvian.apps.json.JSONArray;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

public class ArrayJSONAdapter implements JSONAdapter<Object> {
	private final Class<?> component;
	private Object emptyArray;

	ArrayJSONAdapter(Class<?> component) {
		this.component = component;
		this.emptyArray = null;
	}

	@Override
	public Object adapt(JSON json, Object jsonValue, Type genericType) {
		if (jsonValue instanceof Iterable<?> itr) {
			int size;

			if (itr instanceof Collection<?> c) {
				size = c.size();
			} else {
				size = 0;

				for (var ignored : itr) {
					size++;
				}
			}

			if (size == 0) {
				if (emptyArray == null) {
					emptyArray = Array.newInstance(component, 0);
				}

				return emptyArray;
			}

			var arr = Array.newInstance(component, size);

			int i = 0;

			for (var value : itr) {
				Array.set(arr, i, json.adapt(value, component));
				i++;
			}

			return arr;
		} else {
			throw new IllegalArgumentException("Expected collection, got " + jsonValue.getClass().getName());
		}
	}

	@Override
	public void write(JSON json, Writer writer, Object value, int depth, boolean pretty) throws IOException {
		var len = Array.getLength(value);

		if (len == 0) {
			writer.write("[]");
			return;
		}

		var arr = JSONArray.of(len);

		for (var i = 0; i < len; i++) {
			arr.add(Array.get(value, i));
		}

		json.write(writer, arr, depth, pretty);
	}
}
