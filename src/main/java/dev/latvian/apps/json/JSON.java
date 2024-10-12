package dev.latvian.apps.json;

import dev.latvian.apps.json.adapter.JSONAdapter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class JSON {
	public static final Object NULL = new Object();
	public static final JSON DEFAULT = new JSON(null);

	static {
		DEFAULT.registerStringAdapter(UUID.class, UUID::fromString);
		DEFAULT.registerStringAdapter(Date.class, value -> Date.from(Instant.parse(value)), date -> date.toInstant().toString());
		DEFAULT.registerStringAdapter(Instant.class, Instant::parse);
		DEFAULT.registerStringAdapter(URL.class, value -> {
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		});
		DEFAULT.registerStringAdapter(URI.class, URI::create);
		DEFAULT.registerStringAdapter(Path.class, Path::of);
		DEFAULT.registerStringAdapter(Class.class, value -> {
			try {
				return Class.forName(value);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}, Class::getName);
		DEFAULT.registerStringAdapter(StringBuilder.class, StringBuilder::new);
	}

	private static void escape(Writer writer, String string) throws IOException {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			switch (c) {
				case '"' -> writer.write("\\\"");
				case '\\' -> writer.write("\\\\");
				case '\b' -> writer.write("\\b");
				case '\f' -> writer.write("\\f");
				case '\n' -> writer.write("\\n");
				case '\r' -> writer.write("\\r");
				case '\t' -> writer.write("\\t");
				default -> {
					if (c < ' ') {
						writer.write("\\u");
						writer.write(String.format("%04x", (int) c));
					} else {
						writer.write(c);
					}
				}
			}
		}
	}

	private final JSON parent;
	private final Map<Class<?>, JSONAdapter<?>> adapters;

	private JSON(JSON parent) {
		this.parent = parent;
		this.adapters = new IdentityHashMap<>();
	}

	public <T> void registerAdapter(Class<T> type, JSONAdapter<T> adapter) {
		adapters.put(type, adapter);
	}

	public <T> void registerAdapter(Class<T> type, final Function<Object, T> adapt, final Function<T, Object> write) {
		registerAdapter(type, new JSONAdapter<>() {
			@Override
			public T adapt(JSON json, Object value, Type genericType) {
				return adapt.apply(value);
			}

			@Override
			public void write(JSON json, Writer writer, T value, int depth, boolean pretty) throws IOException {
				json.write(writer, write.apply(value), depth, pretty);
			}
		});
	}

	public <T> void registerStringAdapter(Class<T> type, final Function<String, T> adapt, final Function<T, String> write) {
		registerAdapter(type, new JSONAdapter<>() {
			@Override
			public T adapt(JSON json, Object value, Type genericType) {
				return adapt.apply(String.valueOf(value));
			}

			@Override
			public void write(JSON json, Writer writer, T value, int depth, boolean pretty) throws IOException {
				json.write(writer, write.apply(value), depth, pretty);
			}
		});
	}

	public <T> void registerStringAdapter(Class<T> type, final Function<String, T> adapt) {
		registerStringAdapter(type, adapt, String::valueOf);
	}

	@SuppressWarnings("unchecked")
	public <T> JSONAdapter<T> getAdapter(Class<T> type) {
		var a = adapters.get(type);

		if (a == null) {
			var p = parent;

			while (p != null) {
				a = p.adapters.get(type);

				if (a != null) {
					break;
				}

				p = p.parent;
			}
		}

		if (a == null) {
			a = JSONAdapter.create(type);
			adapters.put(type, a);
		}

		return (JSONAdapter<T>) a;
	}

	public JSONReader read(String string) {
		return new JSONCharArrayReader(this, string.toCharArray());
	}

	public JSONReader read(Path path) throws IOException {
		// use BufferedJSONReader in future
		return new JSONCharArrayReader(this, String.join("", Files.readAllLines(path)).toCharArray());
	}

	public String write(@Nullable Object value) {
		var builder = new StringWriter();

		try {
			write(builder, value, 0, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return builder.toString();
	}

	public String writePretty(@Nullable Object value) {
		var builder = new StringWriter();

		try {
			write(builder, value, 0, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return builder.toString();
	}

	public void write(Path path, Object value, boolean pretty) throws IOException {
		if (Files.notExists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}

		Files.writeString(path, pretty ? writePretty(value) : write(value));
	}

	public void write(Writer writer, @Nullable Object value, int depth, boolean pretty) throws IOException {
		if (depth > 200) {
			throw new IllegalStateException("JSON depth limit reached");
		}

		if (value instanceof JSONSerializable s) {
			value = s.toJSON();
		}

		if (value == null || value == NULL || value instanceof Optional<?> o && o.isEmpty()) {
			writer.write("null");
		} else if (value instanceof Optional<?> o) {
			write(writer, o.get(), depth, pretty);
		} else if (value instanceof String str) {
			writer.write('"');
			escape(writer, str);
			writer.write('"');
		} else if (value instanceof Number || value instanceof Boolean) {
			writer.write(String.valueOf(value));
		} else if (value instanceof Map<?, ?> map) {
			boolean first = true;
			writer.write('{');

			for (var entry : map.entrySet()) {
				if (entry.getValue() instanceof Optional<?> op && op.isEmpty()) {
					continue;
				}

				if (first) {
					first = false;
				} else {
					writer.write(',');
				}

				if (pretty) {
					writer.write('\n');

					for (int i = 0; i <= depth; i++) {
						writer.write('\t');
					}
				}

				writer.write('"');
				escape(writer, String.valueOf(entry.getKey()));
				writer.write('"');
				writer.write(':');

				if (pretty) {
					writer.write(' ');
				}

				write(writer, entry.getValue(), depth + 1, pretty);
			}

			if (pretty) {
				writer.write('\n');

				for (int i = 0; i < depth; i++) {
					writer.write('\t');
				}
			}

			writer.write('}');
		} else if (value instanceof Iterable<?> itr) {
			boolean first = true;
			writer.write('[');

			for (var o : itr) {
				if (o instanceof Optional<?> op && op.isEmpty()) {
					continue;
				}

				if (first) {
					first = false;
				} else {
					writer.write(',');
				}

				if (pretty) {
					writer.write('\n');

					for (int i = 0; i <= depth; i++) {
						writer.write('\t');
					}
				}

				write(writer, o, depth + 1, pretty);
			}

			if (pretty) {
				writer.write('\n');

				for (int i = 0; i < depth; i++) {
					writer.write('\t');
				}
			}

			writer.write(']');
		} else if (value instanceof Object[] arr) {
			write(writer, Arrays.asList(arr), depth, pretty);
		} else {
			((JSONAdapter) getAdapter(value.getClass())).write(this, writer, value, depth, pretty);
		}
	}

	public JSON child() {
		return new JSON(this);
	}

	@ApiStatus.Internal
	@SuppressWarnings("unchecked")
	public <T> T adapt(Object value, Type genericType) {
		var t = genericType == null ? null : JSONAdapter.getRawType(genericType);

		if (value == null || value == NULL) {
			return t == Optional.class ? (T) Optional.empty() : null;
		} else if (t == null || t == Object.class || t.isInstance(value)) {
			return (T) value;
		} else if (t == Optional.class && genericType instanceof ParameterizedType p) {
			var a = p.getActualTypeArguments();

			if (a != null && a.length == 1) {
				return (T) Optional.ofNullable(adapt(value, a[0]));
			} else {
				return (T) Optional.empty();
			}
		}

		if (t == String.class) {
			return (T) String.valueOf(value);
		} else if (t == Character.class || t == Character.TYPE) {
			return (T) Character.valueOf(String.valueOf(value).charAt(0));
		} else if (t == Number.class) {
			return (T) value;
		} else if (t == Byte.class || t == Byte.TYPE) {
			return (T) Byte.valueOf(((Number) value).byteValue());
		} else if (t == Short.class || t == Short.TYPE) {
			return (T) Short.valueOf(((Number) value).shortValue());
		} else if (t == Integer.class || t == Integer.TYPE) {
			return (T) Integer.valueOf(((Number) value).intValue());
		} else if (t == Long.class || t == Long.TYPE) {
			return (T) Long.valueOf(((Number) value).longValue());
		} else if (t == Float.class || t == Float.TYPE) {
			return (T) Float.valueOf(((Number) value).floatValue());
		} else if (t == Double.class || t == Double.TYPE) {
			return (T) Double.valueOf(((Number) value).doubleValue());
		} else if (t == JSONObject.class || t == JSONArray.class) {
			return (T) value;
		} else {
			return (T) getAdapter(t).adapt(this, value, genericType);
		}
	}
}
