package ptemplin.nlp.tts.parse;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Provides utilities for extracting sample web content to read.
 */
public class HTMLExtractor {
	
	private static final String USER_AGENT_KEY = "User-Agent";
	private static final String USER_AGENT_VALUE = "Mozilla/5.0";

	private static final String wikiURL = "https://en.wikipedia.org";
	
	private static final int CHARACTER_LIMIT = 100000;
	
	/**
	 * Gets a synopsis of the content on the specified wikipedia page.
	 * @param page
	 * 			to retrieve
	 * @return the synopsis of the content on the page
	 */
	public static String getWikiText(String page) {
		String html = getHTML(wikiURL + page);
		char[] htmlArr = html.toCharArray();
		StringBuilder builder = new StringBuilder();
		boolean inP = false;
		boolean inTag = false;
		int numTags = 0;
		for (int i = 0; i < htmlArr.length; i++) {
			if (i > CHARACTER_LIMIT) {
				break;
			}
			if (htmlArr[i] == '<') {
				inTag = true;
			} else if (htmlArr[i] == '>') {
				inTag = false;
			} else if (inTag && htmlArr[i] == 'p' && htmlArr[i-1] == '<') {
				inP = true;
			} else if (inTag && htmlArr[i] == 'p' && htmlArr[i-1] == '/' && htmlArr[i-2] == '<') {
				inP = false;
			} else if (!inTag && inP) {
				builder.append(htmlArr[i]);
			}
		}
		return builder.toString();
	}
	
	/**
	 * Performs a GET request to the specified URL and returns the response.
	 * @param pageURL the page to retrieve
	 * @return the html string of the specified page url
	 */
	private static String getHTML(String pageURL) {
		HttpURLConnection connection = null;  
		try {
			URL url = new URL(pageURL);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT_VALUE);
			connection.getResponseCode();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		}
	}
	
}
