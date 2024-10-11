package dev.latvian.apps.json;

public class JSONCharArrayReader implements JSONReader {
	private final JSON JSON;
	private final char[] chars;
	private int pos;

	public JSONCharArrayReader(JSON JSON, char[] chars) {
		this.JSON = JSON;
		this.chars = chars;
		this.pos = 0;
		skipWhitespace();
	}

	@Override
	public JSON json() {
		return JSON;
	}

	@Override
	public char read() {
		if (pos == chars.length) {
			throw new IllegalStateException("EOL");
		}

		return chars[pos++];
	}

	@Override
	public char peek() {
		return pos == chars.length ? '\0' : chars[pos];
	}
}
