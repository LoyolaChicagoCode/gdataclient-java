package edu.luc.cs.laufer.cs433.gdata;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.gbase.client.FeedURLFactory;
import com.google.api.gbase.client.GoogleBaseEntry;
import com.google.api.gbase.client.GoogleBaseFeed;
import com.google.api.gbase.client.GoogleBaseQuery;
import com.google.api.gbase.client.GoogleBaseService;
import com.google.gdata.data.TextConstruct;

import static org.junit.Assert.*;

public class Main {

	private static String KEY;
	private static String TOKEN;
	private static String APP_NAME;

	private GoogleBaseService service;

	@BeforeClass
	public static void beforeClass() throws Exception {
		// the right way to load properties: from classpath
		// (independent of specific location)
		final Properties props = new Properties();
		final URL url = ClassLoader.getSystemResource("main.properties");
		props.load(new FileInputStream(new File(url.getFile())));

		KEY = props.getProperty("main.gdata.apikey");
		TOKEN = props.getProperty("main.gdata.authtoken");
		APP_NAME = props.getProperty("main.gdata.appname");
	}

	@Before
	public void before() throws Exception {
		service = new GoogleBaseService(APP_NAME, KEY);
		service.setUserToken(TOKEN);
	}

	@Test
	public void testEmpty() throws Exception {
		// see http://code.google.com/apis/base/docs/2.0/developers_guide_java.html
		GoogleBaseQuery query = new GoogleBaseQuery(FeedURLFactory.getDefault()
				.getItemsFeedURL());
		GoogleBaseFeed feed = service.query(query);
		for (final GoogleBaseEntry entry : feed.getEntries()) {
			if (entry.getGoogleBaseAttributes().getItemType().equals("TestOnly")) {
				System.out.println(entry.getId() + " "
						+ entry.getTitle().getPlainText() + " "
						+ entry.getGoogleBaseAttributes().getItemType());
				fail("set of TestOnly items not empty");
			}
		}
	}

	@Test
	public void testListAll() throws Exception {
		GoogleBaseQuery query = new GoogleBaseQuery(FeedURLFactory.getDefault()
				.getItemsFeedURL());
		GoogleBaseFeed feed = service.query(query);
		for (final GoogleBaseEntry entry : feed.getEntries()) {
			System.out.println(entry.getId() + " "
					+ entry.getTitle().getPlainText() + " of type "
					+ entry.getGoogleBaseAttributes().getItemType());
		}
	}

	@Test
	public void testAddDelete() throws Exception {

		GoogleBaseEntry entry = new GoogleBaseEntry();
		entry.setTitle(TextConstruct.create(TextConstruct.Type.TEXT,
				"My House", null));
		entry.setContent(TextConstruct.create(TextConstruct.Type.TEXT,
				"The best house in the area.", null));
		entry.getGoogleBaseAttributes().setItemType("TestOnly");
		entry.getGoogleBaseAttributes().addTextAttribute("my attribute",
				"hello");
		entry.getGoogleBaseAttributes().addFloatAttribute("bathrooms", 2);
		entry.getGoogleBaseAttributes().addFloatAttribute("rooms", 6.5f);
		entry.getGoogleBaseAttributes().addFloatAttribute("bedrooms", 2);

		GoogleBaseEntry asInserted =
			service.insert(FeedURLFactory.getDefault().getItemsFeedURL(), entry);

		int count = 0;
		GoogleBaseQuery query = new GoogleBaseQuery(FeedURLFactory.getDefault()
				.getItemsFeedURL());
		GoogleBaseFeed feed = service.query(query);
		for (final GoogleBaseEntry e2 : feed.getEntries()) {
			if (e2.getGoogleBaseAttributes().getItemType().equals("TestOnly")) {
				++ count;
			}
		}
		assertEquals(1, count);

		service.delete(new URL(asInserted.getId()));

		count = 0;
		query = new GoogleBaseQuery(FeedURLFactory.getDefault()
				.getItemsFeedURL());
		feed = service.query(query);
		for (final GoogleBaseEntry e2 : feed.getEntries()) {
			if (e2.getGoogleBaseAttributes().getItemType().equals("TestOnly")) {
				++ count;
			}
		}
		assertEquals(0, count);
	}
}
