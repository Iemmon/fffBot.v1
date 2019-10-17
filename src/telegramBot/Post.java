package telegramBot;

import java.time.LocalDateTime;
import java.util.UUID;

public class Post {
	private String title;
	private String link;
	private LocalDateTime date;
	private final String uniqueID;
	private String messageID;

	public Post(String title, String link, LocalDateTime date) {
		this.title = title;
		this.link = link;
		this.date = date;
		uniqueID = UUID.randomUUID().toString();
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	@Override
	public String toString() {

		return title + " " + link;

	}
}
