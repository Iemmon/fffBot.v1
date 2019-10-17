package telegramBot;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

public class NewMain {

	@SuppressWarnings("restriction")
	public static void main(String[] args) throws Exception {
		ArrayList<Post> listOfPosts = new ArrayList<>();

		Properties appProps = new Properties();
		appProps.load(new FileInputStream("app.properties"));
		
		ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
		service.scheduleWithFixedDelay(new PostsExtractor(listOfPosts, appProps), 0, 2, TimeUnit.MINUTES);

		HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
		server.createContext("/", new Server(listOfPosts, appProps));
		server.setExecutor(service);
		server.start();

	}	
}
