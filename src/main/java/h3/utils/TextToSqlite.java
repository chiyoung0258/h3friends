package h3.utils;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Logger;

public class TextToSqlite {
	
	public static Logger logger = Logger.getLogger(TextToSqlite.class.getName());
	String sourcefilename;
	String targetfilename;
	String createsqlfile;
	String insertsqlfile;
	
	public TextToSqlite(String source, String target, String create, String insert)  {
		this.sourcefilename = source;
		this.targetfilename = target;
		this.createsqlfile = create;
		this.insertsqlfile = insert;
	}
	
	public void process() {
		Connection conn = null;
		BufferedReader br = null;
		try {
			conn = openSqlite();
			createTable(conn);
			br = new BufferedReader(new FileReader(sourcefilename));
			String line;
			while((line = br.readLine()) != null) {
				insertSqlite(conn, line);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) try { br.close(); } catch (Exception e) {e.printStackTrace();}
			closeSqlite(conn);
		}
	}
	
	public Connection openSqlite() {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + targetfilename);
			conn.setAutoCommit(false);
			logger.info("Opened database successfully");			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return conn;
	}
	
	public void closeSqlite(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createTable(Connection conn) {
		Statement stmt = null;
		File file = null;
		FileInputStream fis = null;
		try {
			file = new File(createsqlfile);
			fis = new FileInputStream(file);
			byte[] createSql = new byte[(int) file.length()];
			fis.read(createSql);
			String sql = new String(createSql, "UTF-8");

			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (fis != null) try {fis.close(); } catch(Exception e){e.printStackTrace();}
		}
		logger.info("Table STOCK Created.");
	}
	
	public void insertSqlite(Connection conn, String record) {
	    Statement stmt = null;
	    File file = null;
		FileInputStream fis = null;
	    try {
	    	stmt = conn.createStatement();
	      	file = new File(insertsqlfile);
			fis = new FileInputStream(file);
			byte[] insertSql = new byte[(int) file.length()];
			fis.read(insertSql);
			String sql = new String(insertSql, "UTF-8");
			
			//convert $1,$2,$3,$4 to column data.
			String[] tuple = record.split(",");
			for (int i = tuple.length-1; i >= 0; i--) {
				sql = sql.replace(String.format("$%d", i+1), String.format("%s", tuple[i]));
			}
			logger.info("query - " + sql);
			stmt.executeUpdate(sql);
			stmt.close();
			conn.commit();
	    } catch ( Exception e ) {
	    	logger.severe( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(1);
	    }
	}
	
	public static void main(String... args) {
		String source = args[0];
		String target = args[1];
		String create = args[2];
		String insert = args[3];
		
		TextToSqlite tts = new TextToSqlite(source, target, create, insert);
		tts.process();
	}
}
