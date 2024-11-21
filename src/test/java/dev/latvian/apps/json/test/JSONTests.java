package dev.latvian.apps.json.test;

import dev.latvian.apps.ansi.JavaANSI;
import dev.latvian.apps.ansi.log.Log;
import dev.latvian.apps.json.JSON;
import dev.latvian.apps.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JSONTests {
	@Test
	public void serialize() {
		var map = JSONObject.of("a", 10).append("b", "Hi").append("c", List.of(1, 2, 3));

		Assertions.assertEquals(JSON.DEFAULT.write(map), """
			{"a":10,"b":"Hi","c":[1,2,3]}""");
	}

	@Test
	public void deserialize() {
		var map = JSON.DEFAULT.read("""
			{ "a"   : 10    , "b":"Hi","c":[  1,  2,3  ] }""").readObject();

		Assertions.assertEquals(map.asArray("c").asInt(2), 3);
	}

	@Test
	public void adapt() {
		var config = JSON.DEFAULT.read("""
			{"web":{"required":true,"ids":[1, 2, 3]},"database":"https://lat:test@fakedb.com:1234/","discord":[{"clientId":"7"}]}""").adapt(TestConfig.class);

		Assertions.assertEquals(config.database.toString(), "https://lat:test@fakedb.com:1234/");
		Assertions.assertEquals(config.discord[0].clientId, "7");
		Assertions.assertEquals(config.discord[0].clientSecret, "shh");
		Assertions.assertTrue(config.web.required);
		Assertions.assertEquals(Arrays.toString(config.web.ids), Arrays.toString(new long[]{1L, 2L, 3L}));
	}

	@Test
	public void readRemoteObject() {
		// {"hours":672,"total_launches":7864423,"hourly":11700.028795489174,"ml":[{"ml":1,"fraction":0.9412095458242773,"launches":7402070},{"ml":2,"fraction":0.05879045417572275,"launches":462353}],"mc":[{"mc":2001,"fraction":0.6443314404629558,"launches":5067295},{"mc":1902,"fraction":0.3556624561013567,"launches":2797080},{"mc":2004,"fraction":6.103435687525964E-6,"launches":48}],"mlmc":[{"ml":1,"mc":2001,"fraction":0.6154869848684386,"launches":4840450},{"ml":1,"mc":1902,"fraction":0.3257164575201512,"launches":2561572},{"ml":2,"mc":1902,"fraction":0.029945998581205512,"launches":235508},{"ml":2,"mc":2001,"fraction":0.028844455594517232,"launches":226845},{"ml":1,"mc":2004,"fraction":6.103435687525964E-6,"launches":48}],"launches":[{"version":"2001.6.4-build.114","fraction":0.26559062247796184,"launches":2088717},{"version":"2001.6.4-build.120","fraction":0.21153312836809515,"launches":1663586},{"version":"1902.6.2-build.45","fraction":0.1828411315108559,"launches":1437940},{"version":"2001.6.4-build.127","fraction":0.0810527358459737,"launches":637433},{"version":"1902.6.2-build.3","fraction":0.038601560470488426,"launches":303579},{"version":"2001.6.4-build.95","fraction":0.03645264248883866,"launches":286679},{"version":"1902.6.2-build.15","fraction":0.02442060911525232,"launches":192054},{"version":"1902.6.2-build.50","fraction":0.018523800156731142,"launches":145679},{"version":"2001.6.3-build.83","fraction":0.018382658206457105,"launches":144569},{"version":"1902.6.2-build.27","fraction":0.01564005394928528,"launches":123000},{"version":"","fraction":1.3987040117246999E-6,"launches":11}]}
		var content = "{\"hours\":672,\"total_launches\":7864423,\"hourly\":11700.028795489174,\"ml\":[{\"ml\":1,\"fraction\":0.9412095458242773,\"launches\":7402070},{\"ml\":2,\"fraction\":0.05879045417572275,\"launches\":462353}],\"mc\":[{\"mc\":2001,\"fraction\":0.6443314404629558,\"launches\":5067295},{\"mc\":1902,\"fraction\":0.3556624561013567,\"launches\":2797080},{\"mc\":2004,\"fraction\":6.103435687525964E-6,\"launches\":48}],\"mlmc\":[{\"ml\":1,\"mc\":2001,\"fraction\":0.6154869848684386,\"launches\":4840450},{\"ml\":1,\"mc\":1902,\"fraction\":0.3257164575201512,\"launches\":2561572},{\"ml\":2,\"mc\":1902,\"fraction\":0.029945998581205512,\"launches\":235508},{\"ml\":2,\"mc\":2001,\"fraction\":0.028844455594517232,\"launches\":226845},{\"ml\":1,\"mc\":2004,\"fraction\":6.103435687525964E-6,\"launches\":48}],\"launches\":[{\"version\":\"2001.6.4-build.114\",\"fraction\":0.26559062247796184,\"launches\":2088717},{\"version\":\"2001.6.4-build.120\",\"fraction\":0.21153312836809515,\"launches\":1663586},{\"version\":\"1902.6.2-build.45\",\"fraction\":0.1828411315108559,\"launches\":1437940},{\"version\":\"2001.6.4-build.127\",\"fraction\":0.0810527358459737,\"launches\":637433},{\"version\":\"1902.6.2-build.3\",\"fraction\":0.038601560470488426,\"launches\":303579},{\"version\":\"2001.6.4-build.95\",\"fraction\":0.03645264248883866,\"launches\":286679},{\"version\":\"1902.6.2-build.15\",\"fraction\":0.02442060911525232,\"launches\":192054},{\"version\":\"1902.6.2-build.50\",\"fraction\":0.018523800156731142,\"launches\":145679},{\"version\":\"2001.6.3-build.83\",\"fraction\":0.018382658206457105,\"launches\":144569},{\"version\":\"1902.6.2-build.27\",\"fraction\":0.01564005394928528,\"launches\":123000},{\"version\":\"\",\"fraction\":1.3987040117246999E-6,\"launches\":11}]}";
		Log.info(content);
		var json = JSON.DEFAULT.read(content).readObject();
		json.removeDeep((key, index, value) -> key.equals("fraction") && ((Number) value).doubleValue() < 0.5D, true);

		Log.info(JavaANSI.of(json));
		Log.info(JSON.DEFAULT.writePretty(json));

		Log.info(json.asInt("total_launches"));
		Log.info(json.asDouble("hourly"));
	}

	@Test
	public void record() {
		var r = new RecordTest("root", Optional.of(new RecordTest("bro")), Optional.of(List.of(
			new RecordTest("a"),
			new RecordTest("b"),
			new RecordTest("c")
		)));

		var str = JSON.DEFAULT.write(r);
		Log.info(r);
		Log.info(str);

		var r2 = JSON.DEFAULT.read(str).adapt(RecordTest.class);
		var str2 = JSON.DEFAULT.write(r2);

		Log.info(r2);
		Log.info(str2);

		Assertions.assertEquals(str, str2);
	}
}
