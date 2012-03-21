import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class HelloW extends AbstractHandler {
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.getSession().getSessionContext();
		
        X509Certificate[] certs1 = sslSessionContext.getSession(sessionId.getBytes()).getPeerCertificateChain();
        

		System.out.println("Handled a requst with authType:"+request.getAuthType());
		System.out.println("Handled a baseRequst with authType:"+baseRequest.getAuthType());
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.getWriter().println(
				"<h1>Hello World " + new SimpleDateFormat().format(new Date())
						+ "</h1>");
	}

	public static void main(String[] args) throws Exception {
		new Thread() {
			public void run() {
					startServer();
			}
		}.start();
	}

	private static void startServer() {
        Server server = new Server();
        
        SelectChannelConnector connector0 = new SelectChannelConnector();
        connector0.setPort(8080);
        connector0.setMaxIdleTime(30000);
        connector0.setRequestHeaderSize(8192);
 
        SelectChannelConnector connector1 = new SelectChannelConnector();
        connector1.setHost("127.0.0.1");
        connector1.setPort(8888);
        connector1.setThreadPool(new QueuedThreadPool(20));
        connector1.setName("admin");
 
        SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
        String jetty_home = 
          System.getProperty("jetty.home","c:/Users/d032780/java/jetty-distribution-7.0.1.v20091125/jetty-distribution-7.0.1.v20091125");
        System.setProperty("jetty.home",jetty_home);
        ssl_connector.setPort(8443);
        ssl_connector.setKeystore(jetty_home + "/etc/keystore");
        ssl_connector.setPassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        ssl_connector.setKeyPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        ssl_connector.setNeedClientAuth(true);
        server.addConnector(ssl_connector);
 
        server.setConnectors(new Connector[]{ connector0, connector1, ssl_connector });
 
        server.setHandler(new HelloW());
        try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}