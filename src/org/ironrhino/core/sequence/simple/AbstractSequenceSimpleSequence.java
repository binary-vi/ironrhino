package org.ironrhino.core.sequence.simple;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

public abstract class AbstractSequenceSimpleSequence extends AbstractDatabaseSimpleSequence {

	protected String getQuerySequenceStatement() {
		return new StringBuilder("SELECT NEXTVAL('").append(getActualSequenceName()).append("')").toString();
	}

	protected String getCreateSequenceStatement() {
		StringBuilder sb = new StringBuilder("CREATE SEQUENCE ").append(getActualSequenceName());
		if (getCacheSize() > 1)
			sb.append(" CACHE ").append(getCacheSize());
		return sb.toString();
	}

	protected String getRestartSequenceStatement() {
		return new StringBuilder("ALTER SEQUENCE ").append(getActualSequenceName()).append(" RESTART WITH 1")
				.toString();
	}

	@Override
	public void afterPropertiesSet() {
		try (Connection con = getDataSource().getConnection(); Statement stmt = con.createStatement()) {
			con.setAutoCommit(true);
			DatabaseMetaData dbmd = con.getMetaData();
			boolean sequenceExists = false;
			try (ResultSet rs = dbmd.getTables(null, null, "%", new String[] { "SEQUENCE" })) {
				while (rs.next()) {
					if (getActualSequenceName().equalsIgnoreCase(rs.getString(3))) {
						sequenceExists = true;
						break;
					}
				}
			}
			if (!sequenceExists)
				stmt.execute(getCreateSequenceStatement());
		} catch (SQLException ex) {
			logger.warn(ex.getMessage());
		}
	}

	@Override
	public int nextIntValue() throws DataAccessException {
		try (Connection con = getDataSource().getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(getQuerySequenceStatement())) {
			rs.next();
			return rs.getInt(1);
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain next value of sequence", ex);
		}
	}

	@Override
	public void restart() {
		try (Connection con = getDataSource().getConnection(); Statement stmt = con.createStatement()) {
			con.setAutoCommit(true);
			restartSequence(con, stmt);
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException(ex.getMessage(), ex);
		}
	}

	protected void restartSequence(Connection con, Statement stmt) throws SQLException {
		stmt.execute(getRestartSequenceStatement());
	}

}
