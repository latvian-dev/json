package dev.latvian.apps.json.test;

import java.net.URI;

public class TestConfig {
	public URI database = null;
	public WebConfig web = new WebConfig();
	public DiscordConfig[] discord = new DiscordConfig[0];

	public static class WebConfig {
		public int port = 12345;
		public String title = "Test";
		public boolean required = false;
		public long[] ids = new long[0];
	}

	public static class DiscordConfig {
		public String botToken = "1a2b3c";
		public String clientId = "12345";
		public String clientSecret = "shh";
	}
}
