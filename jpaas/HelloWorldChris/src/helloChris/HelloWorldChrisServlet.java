package helloChris;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
import javax.sql.DataSource;

import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

/**
 * Chris' HelloWorld Servlet printing out info's using SAP HANA Cloud APIs
 */
public class HelloWorldChrisServlet extends HttpServlet {
	private static final String TEST_TABLE_NAME = "TEST";
	private static final String USER_PROVIDER_NAME = "java:comp/env/user/Provider";
	private static final String DATASOURCE_NAME = "java:comp/env/jdbc/DefaultDB";
	private static final DateFormat dfmt = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss");
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		writer.println("Hello World!");
		writer.println("current time: " + dfmt.format(new Date()));
		String path = request.getRequestURI().substring(request.getContextPath().length());
		writer.println("path without contextpath: " + path);

		try {
			if (path.equalsIgnoreCase("/req"))
				describeRequest(writer, request);				
			else if (path.equalsIgnoreCase("/db"))
				describeDataSource(writer, getDataSource(writer));
			else if (path.equalsIgnoreCase("/db-write")) {
				DataSource ds = getDataSource(writer);
				describeDataSource(writer, ds);
				createNameValueTable(writer, ds, TEST_TABLE_NAME);
				updateNameValueTable(writer, ds, TEST_TABLE_NAME, "LAST_ACCESS",
						dfmt.format(new Date()));
			} else if (path.equalsIgnoreCase("/db-read")) {
				DataSource ds = getDataSource(writer);
				describeDataSource(writer, getDataSource(writer));
				describeNameValueTable(writer, ds, TEST_TABLE_NAME);
			} else if (path.equalsIgnoreCase("/")) {
				describeRequest(writer, request);				
				describeDataSource(writer, getDataSource(writer));
				DataSource ds = getDataSource(writer);
				describeDataSource(writer, ds);
				createNameValueTable(writer, ds, TEST_TABLE_NAME);
				updateNameValueTable(writer, ds, TEST_TABLE_NAME, "LAST_ACCESS",
						dfmt.format(new Date()));
				describeNameValueTable(writer, ds, TEST_TABLE_NAME);
			}
		} catch (Exception e) {
			writer.println("ERROR: An exception occured: " + e.getMessage());
			e.printStackTrace(writer);
		}
	}

	private DataSource getDataSource(PrintWriter writer) throws NamingException, Exception {
		InitialContext ctx = new InitialContext();
		DataSource ds = (DataSource) ctx.lookup(DATASOURCE_NAME);
		if (ds == null)
			throw new Exception("Couldn't lookup a DataSource under \""+DATASOURCE_NAME+"\"");
		return ds;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private void describeRequest(PrintWriter writer, HttpServletRequest request)
			throws PersistenceException, NamingException,
			UnsupportedUserAttributeException {
		writer.println("request.getAuthType(): "+request.getAuthType());
		writer.println("request.getCharacterEncoding(): "+request.getCharacterEncoding());
		writer.println("request.getContentLength(): "+request.getContentLength());
		writer.println("request.getContentType(): "+request.getContentType());
		writer.println("request.getContextPath(): "+request.getContextPath());
		writer.println("request.getLocalAddr(): "+request.getLocalAddr());
		writer.println("request.getLocalName(): "+request.getLocalName());
		writer.println("request.getLocalPort(): "+request.getLocalPort());
		writer.println("request.getMethod(): "+request.getMethod());
		writer.println("request.getPathInfo(): "+request.getPathInfo());
		writer.println("request.getPathTranslated(): "+request.getPathTranslated());
		writer.println("request.getProtocol(): "+request.getProtocol());
		writer.println("request.getRemoteAddr(): "+request.getRemoteAddr());
		writer.println("request.getRemoteHost(): "+request.getRemoteHost());
		writer.println("request.getRemotePort(): "+request.getRemotePort());
		writer.println("request.getRemoteUser(): "+request.getRemoteUser());
		writer.println("request.getRequestedSessionId(): "+request.getRequestedSessionId());
		writer.println("request.getRequestURI(): "+request.getRequestURI());
		writer.println("request.getRequestURL(): "+request.getRequestURL());
		writer.println("request.getScheme(): "+request.getScheme());
		writer.println("request.getServerName(): "+request.getServerName());
		writer.println("request.getServerPort(): "+request.getServerPort());
		writer.println("request.getServletPath(): "+request.getServletPath());
		writer.println("request attributes:");
		Enumeration<String> attributeNames = request.getAttributeNames();
		while(attributeNames.hasMoreElements()) {
			String attName=attributeNames.nextElement();
			writer.println("  "+attName+": "+request.getAttribute(attName));
		}
		writer.println("request headers:");
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName=headerNames.nextElement();
			writer.println("  "+headerName+": "+request.getHeader(headerName));
		}
		writer.println("request parameters:");
		Enumeration<String> paramNames = request.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String paramName=paramNames.nextElement();
			writer.println("  "+paramName+": "+request.getParameter(paramName));
		}

		describePrincipal(writer, request.getUserPrincipal());
	}

	private void describePrincipal(PrintWriter writer, Principal principal)
			throws NamingException, PersistenceException,
			UnsupportedUserAttributeException {
		InitialContext ctx = new InitialContext();
		if (principal != null) {
			UserProvider userProvider = (UserProvider) ctx
					.lookup(USER_PROVIDER_NAME);
			User user = userProvider.getUser(principal.getName());
			if (user != null) {
				writer.println("ctx.lookup(\"" + USER_PROVIDER_NAME
						+ "\").getUser(request.getUserPrincipal()).getName(): "
						+ user.getName());
				writer.println("Attributes of user \"" + user.getName() + "\":");
				for (String attr : user.listAttributes())
					writer.println("  " + attr + ": " + user.getAttribute(attr));
			} else
				writer.println("ctx.lookup(\"" + USER_PROVIDER_NAME
						+ "\").getUser(request.getUserPrincipal()): (null)");
		}
	}

	private void describeDataSource(PrintWriter writer, DataSource ds)
			throws NamingException, SQLException {
		Connection conn = ds.getConnection();
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			writer.println("Database ProductName, Version: "
					+ metaData.getDatabaseProductName() + ", "
					+ metaData.getDatabaseProductVersion());
			ResultSet rs = metaData.getTables(null, null, "%",
					new String[] { "TABLE" });
			try {
				if (rs.next())
					do {
						writer.println("Found Table (Catalog, Schema, Name): ("
								+ rs.getString("TABLE_CAT") + ", "
								+ rs.getString("TABLE_SCHEM") + ", "
								+ rs.getString("TABLE_NAME") + ")");
					} while (rs.next());
				else
					writer.println("No tables found in database");
			} finally {
				rs.close();
			}
		} finally {
			conn.close();
		}
	}

	private void describeNameValueTable(PrintWriter writer, DataSource ds,
			String table) throws NamingException, SQLException {
		Connection conn = ds.getConnection();
		try {
			writer.println("Content of table \"" + table + "\"");
			PreparedStatement stmt = conn.prepareStatement("SELECT * from "
					+ table);
			ResultSet rs = stmt.executeQuery();
			try {
				while (rs.next()) {
					writer.println("  name, value: " + rs.getString("name")
							+ ", " + rs.getString("value"));
				}
			} finally {
				rs.close();
				stmt.close();
			}
			
		} finally {
			conn.close();
		}
	}

	private void createNameValueTable(PrintWriter writer, DataSource ds,
			String table) throws SQLException {
		Connection conn = ds.getConnection();
		try {
			// conn.prepareStatement("DROP TABLE " + table).executeUpdate();
			ResultSet rs = conn.getMetaData().getTables(null, null, table,
					new String[] { "TABLE" });
			try {
				if (rs.next()) {
					writer.println("Table " + table + " already existed.");
					return;
				}
			} finally {
				rs.close();
			}
			PreparedStatement stmt = conn.prepareStatement("CREATE TABLE "
					+ table + " (name VARCHAR(32), value VARCHAR(32), PRIMARY KEY(name))");
			stmt.executeUpdate();
			stmt.close();
			writer.println("create table \"" + table + "\"");
		} finally {
			conn.close();
		}
	}

	private void updateNameValueTable(PrintWriter writer, DataSource ds,
			String table, String name, String value) throws SQLException {
		Connection conn = ds.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE " + table
					+ " SET value=? where name=?");
			stmt.setString(1, value);
			stmt.setString(2, name);
			int rows = stmt.executeUpdate();
			stmt.close();
			if (rows >= 1)
				writer.println("Updated " + rows + " row(s) in table \""
						+ table);
			else {
				stmt = conn.prepareStatement("INSERT INTO " + table
						+ " VALUES (?, ?)");
				stmt.setString(1, name);
				stmt.setString(2, value);
				rows = stmt.executeUpdate();
				writer.println("Inserted " + rows + " row(s) into table \""
						+ table);
			}
		} finally {
			conn.close();
		}
	}
}
