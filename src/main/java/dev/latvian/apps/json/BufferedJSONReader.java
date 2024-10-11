package dev.latvian.apps.json;

import java.io.Reader;

public class BufferedJSONReader implements JSONReader {
	private final JSON JSON;
	private final Reader reader;
	private boolean move;
	private char peek;

	public BufferedJSONReader(JSON JSON, Reader reader) {
		this.JSON = JSON;
		this.reader = reader;
		this.move = true;
		this.peek = 0;
	}

	@Override
	public JSON json() {
		return JSON;
	}

	@Override
	public char read() {
		move = true;
		return peek();
	}

	@Override
	public char peek() {
		if (move) {
			move = false;

			try {
				peek = (char) reader.read();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		return peek;
	}
}
