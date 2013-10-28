package helloChris;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.sap.security.auth.login.LoginContextFactory;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

/**
 * Chris' HelloWorld Servlet printing out info's using SAP HANA Cloud APIs
 */
public class HelloWorldChrisServlet extends HttpServlet {
	private static final String USER_PROVIDER_NAME = "java:comp/env/user/Provider";
	private static final String DATASOURCE_NAME = "java:comp/env/jdbc/DefaultDB";
	private static final DateFormat dfmt = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss z");
	private static final long serialVersionUID = 1L;
	private String indent = "";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		response.getWriter()
				.println(
						indent
								+ "HelloWorldChrisServlet.doGet() was called. current time: "
								+ dfmt.format(new Date()));

		try {
			indent = "  " + indent;
			printRequest(request, writer);
			if (request.getRemoteUser() == null
					&& request.getServletPath().contains("BASIC")) {
				writer.println(indent
						+ "Request was unauthenticated and servletPath contained BASIC. I'll trigger basic authentication.");
				LoginContextFactory.createLoginContext("BASIC").login();
				writer.println(indent
						+ "Login was successfull. I'll print out the modified request again\n");
				printRequest(request, writer);
			} else if (request.getRemoteUser() == null
					&& request.getServletPath().contains("FORM")) {
				writer.println(indent
						+ "Request was unauthenticated and servletPath contained FORM. I'll trigger form-based authentication.");
				LoginContextFactory.createLoginContext("FORM").login();
				writer.println(indent
						+ "Login was successfull. I'll print out the modified request again\n");
				printRequest(request, writer);
			}
		} catch (Exception e) {
			writer.println(indent + "ERROR: An exception occured: "
					+ e.getMessage());
		} finally {
			indent = indent.substring(2);
			writer.println(indent + "will leave HelloWorldChrisServlet.doGet()");
		}
	}

	private void printRequest(HttpServletRequest request, PrintWriter writer)
			throws NamingException, PersistenceException,
			UnsupportedUserAttributeException, Exception, SQLException {
		InitialContext ctx = new InitialContext();

		// describe the request
		writer.println();
		writer.println(indent
				+ "Information about the request (request.get...()):");
		writer.println(indent + "  request.getAuthType(): "
				+ request.getAuthType());
		writer.println(indent + "  request.getCharacterEncoding(): "
				+ request.getCharacterEncoding());
		writer.println(indent + "  request.getContentLength(): "
				+ request.getContentLength());
		writer.println(indent + "  request.getContentType(): "
				+ request.getContentType());
		writer.println(indent + "  request.getContextPath(): "
				+ request.getContextPath());
		writer.println(indent + "  request.getLocalAddr(): "
				+ request.getLocalAddr());
		writer.println(indent + "  request.getLocalName(): "
				+ request.getLocalName());
		writer.println(indent + "  request.getLocalPort(): "
				+ request.getLocalPort());
		writer.println(indent + "  request.getMethod(): " + request.getMethod());
		writer.println(indent + "  request.getPathInfo(): "
				+ request.getPathInfo());
		writer.println(indent + "  request.getPathTranslated(): "
				+ request.getPathTranslated());
		writer.println(indent + "  request.getProtocol(): "
				+ request.getProtocol());
		writer.println(indent + "  request.getRemoteAddr(): "
				+ request.getRemoteAddr());
		writer.println(indent + "  request.getRemoteHost(): "
				+ request.getRemoteHost());
		writer.println(indent + "  request.getRemotePort(): "
				+ request.getRemotePort());
		writer.println(indent + "  request.getRemoteUser(): "
				+ request.getRemoteUser());
		writer.println(indent + "  request.getRequestedSessionId(): "
				+ request.getRequestedSessionId());
		writer.println(indent + "  request.getRequestURI(): "
				+ request.getRequestURI());
		writer.println(indent + "  request.getRequestURL(): "
				+ request.getRequestURL());
		writer.println(indent + "  request.getScheme(): " + request.getScheme());
		writer.println(indent + "  request.getServerName(): "
				+ request.getServerName());
		writer.println(indent + "  request.getServerPort(): "
				+ request.getServerPort());
		writer.println(indent + "  request.getServletPath(): "
				+ request.getServletPath());
		writer.println(indent + "  request.getUserPrincipal(): "
				+ request.getUserPrincipal());
		writer.println();
		writer.println(indent + "request attributes:");
		Enumeration<String> attributeNames = request.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attName = attributeNames.nextElement();
			writer.println(indent + "  " + attName + ": "
					+ request.getAttribute(attName));
		}
		writer.println();
		writer.println(indent + "request headers (request.getHeader(...)):");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			writer.println(indent + "  " + headerName + ": "
					+ request.getHeader(headerName));
		}
		writer.println();
		writer.println(indent
				+ "request parameters (request.getParameter(...)):");
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			writer.println(indent + "  " + paramName + ": "
					+ request.getParameter(paramName));
		}

		// describe the session
		writer.println();
		writer.println(indent
				+ "Session attributes (request.getSession().getAttribute(...)):");
		HttpSession session = request.getSession();
		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			writer.println(indent + "  " + name + ": " + session.getAttribute(name));
		}

		// describe additional information about the user principal
		writer.println();
		writer.println(indent
				+ "Information from the userProvider for the current user principal:");
		Principal principal = request.getUserPrincipal();
		if (principal != null) {
			UserProvider userProvider = (UserProvider) ctx
					.lookup(USER_PROVIDER_NAME);
			writer.println(indent + "  UserProvider: ctx.lookup("
					+ USER_PROVIDER_NAME + "):" + userProvider);
			User user = userProvider.getUser(principal.getName());
			if (user != null) {
				writer.println(indent + "  ctx.lookup(\"" + USER_PROVIDER_NAME
						+ "\").getUser(request.getUserPrincipal()).getName(): "
						+ user.getName());
				writer.println(indent + "  Attributes of user \""
						+ user.getName() + "\":");
				for (String attr : user.listAttributes())
					writer.println(indent + "    " + attr + ": "
							+ user.getAttribute(attr));
			} else
				writer.println(indent + "  ctx.lookup(\"" + USER_PROVIDER_NAME
						+ "\").getUser(request.getUserPrincipal()): (null)");
		}

		// describe the datasource
		writer.println();
		writer.println(indent
				+ "Information about the default data source (ctx.lookup(\""
				+ DATASOURCE_NAME + "\"))");
		DataSource ds = (DataSource) ctx.lookup(DATASOURCE_NAME);
		if (ds == null)
			throw new Exception("Couldn't lookup a DataSource under \""
					+ DATASOURCE_NAME + "\"");
		writer.println(indent + "  DataSource: " + ds.toString());
		Connection conn = ds.getConnection();
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			writer.println(indent
					+ "  Connection metadata (dataSource.getConnection().getMetaData():");
			writer.println(indent + "    Database ProductName, Version: "
					+ metaData.getDatabaseProductName() + ", "
					+ metaData.getDatabaseProductVersion());
			ResultSet rs = metaData.getTables(null, null, "%",
					new String[] { "TABLE" });
			try {
				if (rs.next())
					do {
						writer.println(indent
								+ "    Found Table (Catalog, Schema, Name): ("
								+ rs.getString("TABLE_CAT") + ", "
								+ rs.getString("TABLE_SCHEM") + ", "
								+ rs.getString("TABLE_NAME") + ")");
					} while (rs.next());
				else
					writer.println(indent + "    No tables found in database");
			} finally {
				rs.close();
			}
		} finally {
			conn.close();
		}

		// describe the System environment
		writer.println();
		writer.println(indent + "System environment (System.getEnv(...)):");
		for (String key : System.getenv().keySet())
			writer.println(indent + "  " + key + ": " + System.getenv(key));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		writer.println(indent
				+ "HelloWorldChrisServlet.doPost() was called. current time: "
				+ dfmt.format(new Date()));
		try {
			indent = "  " + indent;
			doGet(request, response);
		} finally {
			indent = indent.substring(2);
			writer.println(indent
					+ "will leave HelloWorldChrisServlet.doPost().");
		}
	}
}
