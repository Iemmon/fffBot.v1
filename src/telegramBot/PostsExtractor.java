package telegramBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

public class PostsExtractor implements Runnable {

	private final ArrayList<Post> listOfPosts;
	private final Properties properties;

	public PostsExtractor(ArrayList<Post> listOfPosts, Properties properties) {
		this.listOfPosts = listOfPosts;
		this.properties = properties;
	}

	@Override
	public void run() {
		try {
			File file = new File("DateOfPosts");

			if (!file.exists()) {
				file.createNewFile();
			}
			LocalDateTime lastDateInFile = getDateFromFile(file);
			System.out.println("Load " + lastDateInFile.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

			URL url = new URL("https://www.fly4free.com/flights/flight-deals/europe/feed/");
			ArrayList<String> listOfItems = getDataFromAPI(url);

			ArrayList<Post> newListOfPosts = new ArrayList<Post>();
			LocalDateTime dateOfLastPost = getListOfPosts(newListOfPosts, listOfItems, lastDateInFile);
			for (Post post : newListOfPosts) {
				// sendRequest(post);
				if (post.getDate().isAfter(lastDateInFile)) {
					sendMessageToTestChannel(post);
				}
			}

			listOfPosts.addAll(newListOfPosts);
			deleteOldPosts(listOfPosts);
			System.out.println(dateOfLastPost.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

			FileWriter writer = new FileWriter(file);
			writer.write(dateOfLastPost.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getDataFromAPI(URL url) throws IOException {
		HttpURLConnection connection;
		ArrayList<String> listOfItems = new ArrayList<>();
		connection = (HttpURLConnection) url.openConnection();
		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder respounce = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			respounce.append(line + "\n");
		}
		int startItem = 0;
		int endItem = 0;
		while (true) {
			startItem = respounce.indexOf("<item>", endItem);
			endItem = respounce.indexOf("</item>", startItem);
			if (startItem < 0) {
				break;
			}
			String item = respounce.substring(startItem, endItem);
			listOfItems.add(item);
			// System.out.println(item+"\n");
		}

		return listOfItems;
	}

	public LocalDateTime getListOfPosts(ArrayList<Post> listOfPosts, ArrayList<String> listOfItems,
			LocalDateTime lastDateInFile) throws IOException {

		LocalDateTime initialDate = LocalDateTime.of(2000, 1, 10, 0, 0, 0, 0);
		for (String e : listOfItems) {

			int startTitle = e.indexOf("<title>");
			int endTitle = e.indexOf("</title>", startTitle);
			String title = e.substring(startTitle + 7, endTitle);

			int startLink = e.indexOf("<link>");
			int endLink = e.indexOf("</link>", startLink);
			String link = e.substring(startLink + 6, endLink);

			int startDate = e.indexOf("<pubDate>");
			int endDate = e.indexOf("</pubDate>", startDate);
			String date = e.substring(startDate + 9, endDate);
			LocalDateTime pubTime = LocalDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);

			if (pubTime.isAfter(lastDateInFile)) {
				listOfPosts.add(new Post(title, link, pubTime));
			}

			if (initialDate.isBefore(pubTime)) {
				initialDate = pubTime;
			}
		}
		return initialDate;

	}

	public LocalDateTime getDateFromFile(File file) throws IOException {
		FileReader openFile = new FileReader(file);
		BufferedReader read = new BufferedReader(openFile);

		StringBuilder sb = new StringBuilder();
		String tmp;

		while ((tmp = read.readLine()) != null) {
			sb.append(tmp);
		}
		String currentDate = sb.toString().trim();
		System.out.println(currentDate);

		LocalDateTime result;
		if (currentDate != null && currentDate.length() != 0) {
			result = LocalDateTime.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		} else {
			result = LocalDateTime.of(2000, 1, 10, 0, 0, 0, 0);
		}
		// read.close();
		return result;
	}

	public void sendMessageToTestChannel(Post post) throws Exception {
		HttpsURLConnection connection;

		String encodedTitle = URLEncoder.encode(post.getTitle(), "UTF-8");
		String key = URLEncoder.encode("{\"inline_keyboard\":[[{\"text\":\"post\", \"callback_data\":\""
				+ post.getUniqueID() + "ok" + "\"}]]}", "UTF-8");
		String botID = properties.getProperty("botProperties.botID");
		String chatID = properties.getProperty("botProperties.chatID");

		URL url = new URL("https://api.telegram.org/" + botID + "/sendMessage?text=" + encodedTitle + "%20"
				+ post.getLink() + "&reply_markup=" + key + "&chat_id=" + chatID);
		System.out.println(url.toString());
		connection = (HttpsURLConnection) url.openConnection();
		InputStream connectionStream = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(connectionStream));
		StringBuilder sbRequest = new StringBuilder();
		String request;
		while ((request = br.readLine()) != null) {
			sbRequest.append(request + "\n");
		}
		System.out.println(sbRequest.toString());
		connectionStream.close();
	}

	public void deleteOldPosts(ArrayList<Post> listOfPosts) {
		LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
		List<Post> postsToDelete = new ArrayList<>();
		for (Post post : listOfPosts) {
			if (post.getDate().isBefore(threeDaysAgo)) {
				postsToDelete.add(post);
			}
		}
		listOfPosts.removeAll(postsToDelete);
	}

}
