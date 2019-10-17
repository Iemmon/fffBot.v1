package telegramBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import com.sun.net.httpserver.*;

public class Server implements HttpHandler {

	private final ArrayList<Post> list;
	private final Properties properties;

	public Server(ArrayList<Post> list, Properties properties) {
		this.list = list;
		this.properties = properties;
	}

	public void sendRequest(Post post) throws Exception {
		HttpsURLConnection connection;
		String encodedTitle = URLEncoder.encode(post.getTitle(), "UTF-8");
		String botID = properties.getProperty("botProperties.botID");
		String chatID = properties.getProperty("botProperties.chatID");
		URL url = new URL("https://api.telegram.org/bot492068735:AAEtbf_8kz-tHRpEJt33ryRlb9NAfFfRsUQ/sendMessage?text="
				+ encodedTitle + " " + post.getLink() + "&chat_id=@flyforfree");
		connection = (HttpsURLConnection) url.openConnection();
		InputStream connectionStream = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(connectionStream));
		StringBuilder sbRequest = new StringBuilder();
		String request;
		while ((request = br.readLine()) != null) {
			sbRequest.append(request + "\n");
		}

		connectionStream.close();
	}

	public Post searchThroughList(ArrayList<Post> list, String item) {
		for (Post post : list) {
			if (post.getUniqueID().contains(item)) {
				return post;
			}
		}
		return null;
	}

	@Override
	public void handle(HttpExchange t) throws IOException {

		InputStream inputStream = t.getRequestBody();
		BufferedReader brForInputStream = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sbRequest = new StringBuilder();
		String request;
		while ((request = brForInputStream.readLine()) != null) {
			sbRequest.append(request + "\n");
		}
		System.out.println(sbRequest.toString());

		int startItem = 0;
		int endItem = 0;

		startItem = sbRequest.indexOf("\"data\"", endItem);
		endItem = sbRequest.indexOf(",", startItem);
		if (endItem == -1)
			endItem = sbRequest.indexOf("}", startItem);

		String item = sbRequest.substring(startItem + 9, endItem - 1);
		System.out.println(item);
		startItem = 0;
		endItem = 0;

		startItem = sbRequest.indexOf("\"message_id\"", endItem);
		endItem = sbRequest.indexOf(",", startItem);
		if (endItem == -1)
			endItem = sbRequest.indexOf("}", startItem);

		System.out.println(startItem + " " + endItem);

		String messageID = sbRequest.substring(startItem, endItem).replaceAll("[^\\d-]", "");

		System.out.println(messageID);

		String lastTwoLetters = item.substring(item.length() - 2, item.length());

		String postId;
		if (lastTwoLetters.contains("ok")) {
			System.out.println("Equal");
			postId = item.substring(0, item.length() - 2);
		} else {
			System.out.println("Not equal");
			postId = item;
		}

		System.out.println("Button pressed on button with id #" + postId);
		Post post = searchThroughList(list, postId);

		if (post != null) {
			post.setMessageID(messageID);
			try {
				sendRequest(post);
				editMessage(post);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		inputStream.close();

		String response = "OK";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public void editMessage(Post post) {
		try {
			HttpsURLConnection connection;
			String encodedTitle = URLEncoder.encode(post.getTitle(), "UTF-8");

			URL url = new URL(
					"https://api.telegram.org/bot492068735:AAEtbf_8kz-tHRpEJt33ryRlb9NAfFfRsUQ/editMessageText?text="
							+ encodedTitle + " " + post.getLink() + "&chat_id=-1001127959489&message_id="
							+ post.getMessageID());
			connection = (HttpsURLConnection) url.openConnection();
			InputStream connectionStream = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(connectionStream));
			StringBuilder sbRequest = new StringBuilder();
			String request;
			while ((request = br.readLine()) != null) {
				sbRequest.append(request + "\n");
			}

			connectionStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
