package dev.latvian.apps.json.test;

import java.util.List;
import java.util.Optional;

public record RecordTest(String name, Optional<RecordTest> sibling, Optional<List<RecordTest>> children) {
	public RecordTest(String name) {
		this(name, Optional.empty(), Optional.empty());
	}
}
