package dev.latvian.apps.json;

import dev.latvian.apps.tinyserver.content.MimeType;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

import java.nio.charset.StandardCharsets;

public interface JSONResponse {
	HTTPResponse SUCCESS = HTTPStatus.OK.json(JSONObject.of("success", true).toString());

	static HTTPResponse of(HTTPStatus status, Object json) {
		return status.content(JSON.DEFAULT.write(json).getBytes(StandardCharsets.UTF_8), MimeType.JSON);
	}

	static HTTPResponse of(Object json) {
		return of(HTTPStatus.OK, json);
	}
}