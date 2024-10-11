package dev.latvian.apps.json.adapter;

import dev.latvian.apps.json.JSON;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Locale;

public class EnumJSONAdapter implements JSONAdapter<Object> {
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
				values[i] = new EnumValue(i, ((Enum<?>) c[i]).name().toLowerCase(Locale.ROOT), c[i]);
			}
		}

		return values;
	}

	@Override
	public Object adapt(JSON json, Object jsonValue, Type genericType) {
		var str = String.valueOf(jsonValue);

		for (var val : values()) {
			if (val.name().equalsIgnoreCase(str)) {
				return val.value;
			}
		}

		throw new NullPointerException("Eunm value '" + str + "' not found");
	}

	@Override
	public void write(JSON json, Writer writer, Object value, int depth, boolean pretty) throws IOException {
		for (var val : values()) {
			if (val.value == value) {
				json.write(writer, val.name, depth, pretty);
				return;
			}
		}

		json.write(writer, "", depth, pretty);
	}
}
