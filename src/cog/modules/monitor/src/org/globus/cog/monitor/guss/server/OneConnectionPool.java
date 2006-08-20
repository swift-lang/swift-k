package org.globus.cog.monitor.guss;

/*Singleton class to set up database connection pool that will be used by all other classes.*/
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class OneConnectionPool {

    private static final String poolShortName = "usagestats";
    private static final String poolFullName = "jdbc:apache:commons:dbcp:usagestats";
    private static int numberOfConnections;

    public static void create(String driverClass, String dburl,
			      String userName, String password) 
	throws ClassNotFoundException, SQLException {

	ConnectionFactory connectionFactory;
	GenericObjectPool connectionPool;
	PoolableConnectionFactory poolableConnectionFactory;
	PoolingDriver driver;

	Class.forName(driverClass);
	connectionPool = new GenericObjectPool(null);
	connectionFactory = new DriverManagerConnectionFactory(dburl, userName, password);
	poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
								  connectionPool,
								  null, null,
								  false, true);
	driver = new PoolingDriver();
	driver.registerPool(poolShortName, connectionPool);
	System.out.println("New connection pool created");
	numberOfConnections = 0;
    }

    public static Connection getConnection() throws SQLException {
	numberOfConnections++;
	System.out.println("Opened connection: "+numberOfConnections+" open now.");
	return DriverManager.getConnection(poolFullName);
    }

    public static void closeConnection(Connection con) {

	try {
	    con.close();
	    numberOfConnections --;
	    System.out.println("Closed connection: now "+numberOfConnections+" open.");
	} catch (Exception e) {}
    }

    public static void close() {
	PoolingDriver driver;
	try {
	    driver = (PoolingDriver)DriverManager.getDriver(poolFullName);
	    driver.closePool(poolShortName);
	}
	catch(Exception e) {}
	System.out.println("Connection pool closed.");
    }
    
}

