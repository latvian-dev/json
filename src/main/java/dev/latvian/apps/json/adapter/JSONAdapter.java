package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public interface JSONAdapter<T> {
	T adapt(JSON json, Object jsonValue, Type genericType);

	void write(JSON json, Writer writer, T value, int depth, boolean pretty) throws IOException;

	static Class<?> getRawType(Type type) {
		return switch (type) {
			case Class<?> c -> c;
			case ParameterizedType p -> getRawType(p.getRawType());
			case GenericArrayType a -> getRawType(a.getGenericComponentType());
			case WildcardType t -> getRawType(t.getUpperBounds()[0]);
			case null, default -> throw new IllegalArgumentException("Generic type " + type + " is currently not supported");
		};
	}

	static JSONAdapter<?> create(Class<?> type) {
		if (type.isArray()) {
			return new ArrayJSONAdapter(type.getComponentType());
		} else if (type.isRecord()) {
			return new RecordJSONAdapter(type);
		} else if (type.isEnum()) {
			return new EnumJSONAdapter(type);
		} else {
			return new ReflectionJSONAdapter(type);
		}
	}
}
