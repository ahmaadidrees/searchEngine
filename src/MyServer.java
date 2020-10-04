import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * A simple example of using Jetty and servlets to create a dynamic web page. The web page will
 * display the current date/time when loaded.
 */
public class MyServer {

  /** The hard-coded port to run this server. */
  public static final int PORT = 8080;

  /**
 * 
 */
public static ThreadSafeInvertedIndex index;
  /**
 * @param index
 */
public MyServer(ThreadSafeInvertedIndex index) {
	  MyServer.index = index;
  }
  
  /**
   * Sets up a Jetty server with explicit connector and handler components.
   *
   * @param args unused
   * @throws Exception if unable to start web server
   */
  public static void main(String[] args) throws Exception {
    // Create the Jetty server
    Server server = new Server();

    // Setup the connector component
    ServerConnector connector = new ServerConnector(server);
    connector.setHost("localhost");
    connector.setPort(PORT);

    // Setup the handler component
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(SearchServlet.class, "/");

    // Configure server to use connector and handler
    server.addConnector(connector);
    server.setHandler(handler);

    // Start the server (it is a thread)
    server.start();
    System.out.println(" Server: " + server.getState());
    System.out.println("Threads: " + server.getThreadPool().getThreads());

    // Keeps main thread active as long as server is active
    // Until we implement shutdown, will never see the server enter terminated state
    server.join();
    System.out.println(" Server: " + server.getState());
  }

  /**
   * A simple servlet that will display the current date and time when loaded.
   */
  public static class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

      // Output request for debugging (better if use logger)
      System.out.println(Thread.currentThread().getName() + ": request: " + request.getRequestURI());

      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
      PrintWriter out = response.getWriter();
      out.printf("<html lang=\"en\">%n");
      out.printf("<head>");
      out.printf("<meta charset=\"utf-8\">");
      out.printf("<title>Search Engine</title>");
      out.printf("<body>");
      out.printf("<h1>Search Engine</h1>");
      out.printf("<form method=\"POST\" action=\"/\">");
      out.printf("<input type=\"text\" placeholder=\"Search..\" name=\"query\" id=\"query\" maxlength=\"100\" size=\"60\">");
      out.printf("</p>");
      out.printf("<p><input type=\"submit\" value=\"ENTER\"></p>");
      out.printf("</form>");
      out.printf("</body>");
      out.printf("</html>");
    
      
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	
    	response.setContentType("text/html");
    	PrintWriter out = response.getWriter();
    	String query = request.getParameter("query");
    	query = query == null || query.isBlank() ? "" : query;
    	out.printf("<html lang=\"en\">%n");
        out.printf("<head>");
        out.printf("<meta charset=\"utf-8\">");
        out.printf("<title>Results</title>");
        out.printf("<body>");
        out.printf("<h1>Results</h1>");
    	//out.println("query: "+query);
    	var queryLine = TextFileStemmer.uniqueStems(query);
    	//out.println(queryLine.toString());
    	var searchResults = index.search(queryLine, false);
    	int i = 1;
    	Collections.sort(searchResults);
    	for (InvertedIndex.Result result : searchResults) {
    		String link = result.getWhere();
    		//out.println(i++ + ": " + result.getWhere());
    		out.printf("<a href=\"%s\" >%s</a>%n ", link, link);
    	}
    	
    	
    }
  }
}