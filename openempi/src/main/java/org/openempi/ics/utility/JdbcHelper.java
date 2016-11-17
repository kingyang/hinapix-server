/**
 * Title:        JdbcHelper
 * Description:  Provides the necessary support for establishing or retrieving database connections as defined in CDE.properties
 * Copyright:    (c) 2002
 * Company:      CareScience, Inc.
 *
 * @version 2.1
 */
package org.openempi.ics.utility;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Provides the necessary support for establishing or retrieving database connections as defined in CDE.properties
 */
public class JdbcHelper {
    private static JdbcHelper jdbcHelper;

    // Do not instantiate. Used as STATIC only.
    private JdbcHelper() {

    }

    private static BasicDataSource dataSource;
    static {
        // initializeJdbc();
        // jdbcHelper = new JdbcHelper();
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        dataSource = (BasicDataSource)applicationContext.getBean("dataSource");
    }

    /**
     * Will pass back either a new connection from the DataSource (if using Pool), or the existing connection from the DriverManager (if not using pool).
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection()
            throws SQLException {
        Connection conn = dataSource.getConnection();
        return conn;
    }
}
