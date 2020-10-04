import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * @author ahmaadidrees
 *
 */
public class WebCrawler {

	/**
	 * work queue to use
	 */
	public WorkQueue workQ;
	/**
	 * limit of web pages to crawl
	 */
	public int limit;
	
	/**
	 * number of threads to use
	 */
	public int threads;
	
	/**
	 * set of links found within the seed url
	 */
	public HashSet<URL> linkSet;
	
	/**
	 * 
	 */
	public ThreadSafeInvertedIndex index;
	
	/**
	 * constructor for the web crawler
	 * @param workQ the work queue to use
	 * @param limit limit to use
	 * @param threads to use
	 * @param index to use
	 */
	public WebCrawler(WorkQueue workQ, int limit, int threads, ThreadSafeInvertedIndex index ) {
		this.workQ = workQ;
		this.linkSet = new HashSet<URL>();
		this.limit = limit;
		this.threads = threads;
		this.workQ = new WorkQueue(threads);
		this.index = index;
	}
	
	
	/**
	 * @param url seed url to build from
	 * @param index thread safe inverted index to use
	 * @throws IOException if IO error occurs
	 * @throws InterruptedException if interrupt error occurs
	 */
	public  void buildFromSeed(URL url, ThreadSafeInvertedIndex index) throws IOException, InterruptedException {
		if (threads < 1) {
			return;
		}
		ArrayList<URL> linkList = new ArrayList<URL>();
		String html = HtmlFetcher.fetch(url,3);
		html = HtmlCleaner.stripComments(html);
		html = HtmlCleaner.stripElement(html, "head");
		html = HtmlCleaner.stripElement(html, "style");
		html = HtmlCleaner.stripElement(html, "script");
		html = HtmlCleaner.stripElement(html, "noscript");
		html = HtmlCleaner.stripElement(html, "svg");
		linkList = LinkParser.listLinks(url, html);
		html = HtmlCleaner.stripTags(html);
		html = HtmlCleaner.stripEntities(html);
		for (URL link : linkList) {
			if(linkSet.size() < limit && !linkSet.contains(link)) {
				linkSet.add(link);
				workQ.execute(new Crawler(link, index));
			}
		}
		workQ.finish();
		workQ.shutdown();
	}

	/**
	 * @author ahmaadidrees
	 *
	 */
	private  class Crawler implements Runnable {

		/**
		 * 
		 */
		private  URL url;
		/**
		 * 
		 */
		private ThreadSafeInvertedIndex index;

		/**
		 * @param link to use
		 * @param index index to use
		 */
		public Crawler(URL link, ThreadSafeInvertedIndex index) {
			url = link;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				String html = HtmlFetcher.fetch(url, 3);
				html = HtmlCleaner.stripComments(html);
				html = HtmlCleaner.stripElement(html, "head");
				html = HtmlCleaner.stripElement(html, "style");
				html = HtmlCleaner.stripElement(html, "script");
				html = HtmlCleaner.stripElement(html, "noscript");
				html = HtmlCleaner.stripElement(html, "svg");
				ArrayList<URL> otherLinks = new ArrayList<URL>();
				otherLinks = LinkParser.listLinks(url, html);
				html = HtmlCleaner.stripTags(html);
				html = HtmlCleaner.stripEntities(html);
				InvertedIndex local = new InvertedIndex();
				IndexBuilder.build(url, local, html);
				index.addAll(local);
				synchronized(linkSet) {
					for (URL urls : otherLinks) {
						if(linkSet.size() < limit && !linkSet.contains(urls)) {
							linkSet.add(urls);
							workQ.execute(new Crawler(urls, index));
						}
					}
				}
			} catch (IOException e) {
				System.out.println("error occured while fetching the html from the url: " + url.toString());
			}
		}
	}
}
