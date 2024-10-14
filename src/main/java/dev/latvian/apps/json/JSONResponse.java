package dev.latvian.apps.json;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;

public interface JSONResponse {
	HTTPResponse SUCCESS = of(JSONObject.of("success", true));

	static HTTPResponse of(HTTPStatus status, Object json) {
		return status.json(JSON.DEFAULT.write(json));
	}

	static HTTPResponse of(Object json) {
		return of(HTTPStatus.OK, json);
	}
}