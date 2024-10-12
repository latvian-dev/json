package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Locale;

public class EnumJSONAdapter implements JSONAdapter<Object> {
	public interface CustomName {
		String getJSONName();
	}

	private record EnumValue(int index, String name, Object value) {
	}

	private final Class<?> type;
	private EnumValue[] values;

	EnumJSONAdapter(Class<?> type) {
		this.type = type;
	}

	private EnumValue[] values() {
		if (values == null) {
			var c = type.getEnumConstants();
			values = new EnumValue[c.length];

			for (int i = 0; i < c.length; i++) {
				values[i] = new EnumValue(i, c[i] instanceof CustomName cn ? cn.getJSONName() : ((Enum<?>) c[i]).name().toLowerCase(Locale.ROOT), c[i]);
			}
		}

		return values;
	}

	@Override
	public Object adapt(JSON json, Object jsonValue, Type genericType) {
		if (jsonValue instanceof Number n) {
			int i = n.intValue();

			if (i >= 0 && i < values().length) {
				return values()[i].value;
			} else {
				throw new IndexOutOfBoundsException("Index out of bounds: " + i);
			}
		}

		var str = String.valueOf(jsonValue);

		for (var val : values()) {
			if (val.name().equalsIgnoreCase(str)) {
				return val.value;
			}
		}

		throw new NullPointerException("Unknown enum constant: " + str);
	}

	@Override
	public void write(JSON json, Writer writer, Object value, int depth, boolean pretty) throws IOException {
		json.write(writer, value instanceof CustomName cn ? cn.getJSONName() : ((Enum<?>) value).name().toLowerCase(Locale.ROOT), depth, pretty);
	}
}
