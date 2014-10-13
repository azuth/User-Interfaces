package de.hska.iwii.picturecommunity.backend.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

/**
 * Legt beim Programmstart die Datenbank sowie das Schema an, wenn
 * die Konfiguration das erfordert.
 */
public class Initializer extends NamedParameterJdbcDaoSupport {

	private Resource ddlCreateDatabaseFile;
	private Resource ddlRecreateSchemaFile;
	private String ddlPolicy;
	private String schema;
	private String database;

	// Existiert die Datenbank schon?
	boolean existsDatabase = false;
	
	// Existiert das Schema schon in der Datenbank?
	boolean existsSchema = false;

	/**
	 * Legt die erforderlichen Datenbanktabellen an.
	 */
	@PostConstruct
	private void createDatabases() {
		
		checkForDatabaseAndSchema();
		
		// Datenbank existiert nicht, darf aber angelegt werden. 
		if (!existsDatabase && (ddlPolicy.equals("CREATE_DATABASE") || ddlPolicy.equals("RECREATE_SCHEMA"))) {
			exeuteDDL(ddlCreateDatabaseFile);
		}
		
		// Datenbank wurde angelegt, oder Schema muss geloescht und erneut angelegt werden.
		if ((!existsDatabase && ddlPolicy.equals("CREATE_DATABASE")) || ddlPolicy.equals("RECREATE_SCHEMA")) {
			exeuteDDL(ddlRecreateSchemaFile);
		}
	}

	/**
	 * Ein DDL-Skript ausfuehren.
	 * @param ddlFile Verweis auf dei Datei.
	 */
	private void exeuteDDL(Resource ddlFile) {
		try {
			InputStream str = ddlFile.getInputStream();
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(str));
			String line = lnr.readLine();
			while (line != null) {
				line = line.trim(); 
				if (line.length() > 0 && !line.startsWith("#")) {
					line = line.replaceAll("\\{0\\}", database);
					line = line.replaceAll("\\{1\\}", schema);
					getJdbcTemplate().execute(line);
				}
				line = lnr.readLine();
			}
			str.close();
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
		}
	}

	/**
	 * Existieren Dankbank und Schema schon?
	 */
	private void checkForDatabaseAndSchema() {
		try {
			// Gibt es die Datenbank schon?
			Connection connection = getJdbcTemplate().getDataSource().getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet result = metaData.getCatalogs();
			while (result.next()) {
				if (result.getString("TABLE_CAT").equals(database)) {
					existsDatabase = true;
				}
			}
			
			// Gibt es das Schema schon in der Datenbank?
			if (existsDatabase) {
				result = metaData.getSchemas(database, schema);
				while (result.next()) {
					if (result.getString("TABLE_SCHEM").equals(schema)) {
						existsSchema = true;
					}
				}
			}
			connection.close();
		} catch (SQLException e1) {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, e1.getMessage());
		}
	}

	// Die folgenden Setter werden von Spring verwendet.
	public void setDdlCreateDatabaseFile(Resource ddlCreateDatabaseFile) {
		this.ddlCreateDatabaseFile = ddlCreateDatabaseFile;
	}

	public void setDdlRecreateSchemaFile(Resource ddlRecreateSchemaFile) {
		this.ddlRecreateSchemaFile = ddlRecreateSchemaFile;
	}

	public void setDdlPolicy(String ddlPolicy) {
		this.ddlPolicy = ddlPolicy;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setDatabase(String database) {
		this.database = database;
	}
}
