package gov.epa.emissions.commons.db;

import java.sql.Connection;

/**
 * An acceptor which takes in data and puts it in a database
 */

public class MySqlDataAcceptor extends AbstractDataAcceptor {

    public MySqlDataAcceptor(Connection connection, boolean useTransactions, boolean usePrepStatement) {
        super(connection);
    }

    public String customizeCreateTableQuery(String origQueryString) {
        String queryString = origQueryString;
        // TODO: do we need InnoDB ? If so, when ?
        // if (useTransactions) {
        // queryString += " type=InnoDB";
        // }
        return queryString;
    }

}
