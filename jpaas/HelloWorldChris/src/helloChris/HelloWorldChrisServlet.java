package helloChris;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
 * Servlet implementation class HelloWorldChrisServlet
 */
public class HelloWorldChrisServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public HelloWorldChrisServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		writer.println("from Chris: Hello World! Current time: "
				+ new Date().toGMTString());

		try {
			describeRequest(writer, request);
			describeDataSource(writer, "java:comp/env/jdbc/DefaultDB");
		} catch (Exception e) {
			writer.println("ERROR: An exception occured: "+e.getMessage());
			e.printStackTrace(writer);
		}
	}

	private void describePrincipal(PrintWriter writer, Principal principal)
			throws NamingException, PersistenceException, UnsupportedUserAttributeException {
		InitialContext ctx = new InitialContext();
		if (principal != null) {
			UserProvider userProvider = (UserProvider) ctx
					.lookup("java:comp/env/user/Provider");
			User user = userProvider.getUser(principal.getName());
			if (user != null) {
				writer.println("ctx.lookup(\"java:comp/env/user/Provider\").getUser(request.getUserPrincipal()).getName(): "
						+ user.getName());
				writer.println("Attributes:");
				for (String attr : user.listAttributes())
					writer.println("  " + attr + ": "
							+ user.getAttribute(attr));
			} else
				writer.println("ctx.lookup(\"java:comp/env/user/Provider\").getUser(request.getUserPrincipal()): (null)");
		}
	}

	private void describeDataSource(PrintWriter writer, String jndiName)
			throws NamingException, SQLException {
		InitialContext ctx = new InitialContext();
		DataSource ds = (DataSource) ctx.lookup(jndiName);
		writer.println("ctx.lookup(\"" + jndiName + "\"): "
				+ ((ds == null) ? "(null)" : ds));
		if (ds != null) {
			Connection conn = ds.getConnection();
			describeConnectionMetaData(writer, conn);
			try {
				if (!checkDBExists(conn, "TEST")) {
					writer.println("Table TEST didn't exist. Create it!");
					conn.prepareStatement(
							"CREATE TABLE TEST (name VARCHAR(32), value VARCHAR(32))")
							.executeUpdate();
				}
				conn.prepareStatement("INSERT INTO TEST VALUES ('ACCESS', '"+new Date().toGMTString()+"')")
						.executeUpdate();
				ResultSet rs = conn.prepareStatement(
						"SELECT COUNT(*) from TEST").executeQuery();
				try {
					rs.next();
					writer.println("Table TEST exists already and contains "
							+ rs.getInt(1) + " rows.");
				} finally {
					rs.close();
				}

			} finally {
				conn.close();
			}
		}
	}

	private void describeConnectionMetaData(PrintWriter writer, Connection conn)
			throws NamingException, SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		
//		ResultSet schemas = metaData.getSchemas();
//		try {
//			while(schemas.next())
//				writer.println("Found Catalog/Schema: "+schemas.getString("TABLE_CATALOG")+"/"+schemas.getString("TABLE_SCHEM"));
//		} finally {
//			schemas.close();
//		}
		ResultSet schemas = metaData.getTables(null,  null,  "%",  new String[] { "TABLE" });
		try {
			while(schemas.next())
				writer.println("Found Catalog, Schema, Name, Type: "+schemas.getString("TABLE_CAT")+", "+schemas.getString("TABLE_SCHEM")+", "+schemas.getString("TABLE_NAME")+", "+schemas.getString("TABLE_TYPE"));
		} finally {
			schemas.close();
		}
	}

	private boolean checkDBExists(Connection conn, String name)
			throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet rs = metaData.getTables(null, "APP", "%",
				new String[] { "TABLE" });
		try {
			while (rs.next())
				if (name.equals(rs.getString("TABLE_NAME")))
					return true;
			return false;
		} finally {
			rs.close();
		}
	}

	private void describeRequest(PrintWriter writer, HttpServletRequest request)
			throws PersistenceException, NamingException, UnsupportedUserAttributeException {
		String userName = request.getRemoteUser();
		writer.println("request.getRemoteUser(): "
				+ ((userName == null) ? "(null)" : userName));
		describePrincipal(writer, request.getUserPrincipal());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
