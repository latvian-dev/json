package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;

public interface JSONAdapter<T> {
	T adapt(JSON json, Object jsonValue, Type genericType);

	default void write(JSON json, Writer writer, T value, int depth, boolean pretty) throws IOException {
		json.write(writer, value, depth, pretty);
	}

	static Class<?> getRawType(Type type) {
		return switch (type) {
			case Class<?> c -> c;
			case ParameterizedType p -> getRawType(p.getRawType());
			case GenericArrayType a -> getRawType(a.getGenericComponentType());
			case WildcardType t -> getRawType(t.getUpperBounds()[0]);
			case null, default -> throw new IllegalArgumentException("Generic type " + type + " is currently not supported");
		};
	}

	static JSONAdapter<?> create(Class<?> t) {
		if (t == IdentityHashMap.class) {
			return new MapJSONAdapter(t, MapJSONAdapter.Factory.IDENTITY_MAP);
		} else if (t == Map.class || t == HashMap.class || t == LinkedHashMap.class) {
			return new MapJSONAdapter(t, MapJSONAdapter.Factory.MAP);
		} else if (t == Set.class || t == SequencedSet.class || t == HashSet.class || t == LinkedHashSet.class) {
			return new CollectionJSONAdapter(t, CollectionJSONAdapter.Factory.LINKED_SET);
		} else if (t == LinkedList.class) {
			return new CollectionJSONAdapter(t, CollectionJSONAdapter.Factory.LINKED_LIST);
		} else if (t == List.class || t == ArrayList.class || t == Collection.class || t == Iterable.class || t == SequencedCollection.class) {
			return new CollectionJSONAdapter(t, CollectionJSONAdapter.Factory.LIST);
		} else if (t.isArray()) {
			return new ArrayJSONAdapter(t.getComponentType());
		} else if (t.isRecord()) {
			return new RecordJSONAdapter(t);
		} else if (t.isEnum()) {
			return new EnumJSONAdapter(t);
		} else {
			return new ReflectionJSONAdapter(t);
		}
	}
}
