package org.ironrhino.core.sequence.cyclic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

public class MySQLCyclicSequence extends AbstractDatabaseCyclicSequence {

	private AtomicInteger nextId = new AtomicInteger(0);

	private AtomicInteger maxId = new AtomicInteger(0);

	public MySQLCyclicSequence() {
		setCacheSize(10);
	}

	@Override
	public void afterPropertiesSet() {
		Connection con = null;
		Statement stmt = null;
		try {
			con = getDataSource().getConnection();
			con.setAutoCommit(false);
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTables(null, null, "%", null);
			boolean tableExists = false;
			while (rs.next()) {
				if (getTableName().equalsIgnoreCase(rs.getString(3))) {
					tableExists = true;
					break;
				}
			}
			stmt = con.createStatement();
			String columnName = getSequenceName();
			if (tableExists) {
				rs = stmt.executeQuery("SELECT * FROM " + getTableName());
				boolean columnExists = false;
				ResultSetMetaData metadata = rs.getMetaData();
				for (int i = 0; i < metadata.getColumnCount(); i++) {
					if (columnName.equalsIgnoreCase(metadata
							.getColumnName(i + 1))) {
						columnExists = true;
						break;
					}
				}
				rs.close();
				if (!columnExists) {
					stmt.execute("ALTER TABLE `" + getTableName() + "` ADD "
							+ columnName + " INT NOT NULL DEFAULT 0,ADD "
							+ columnName + "_TIMESTAMP BIGINT DEFAULT 0");
					stmt.execute("update `" + getTableName() + "` set "
							+ columnName + "_TIMESTAMP=UNIX_TIMESTAMP()");
					con.commit();
				}
			} else {
				stmt.execute("CREATE TABLE `" + getTableName() + "` ("
						+ columnName + " INT NOT NULL DEFAULT 0," + columnName
						+ "_TIMESTAMP BIGINT) ");
				stmt.execute("INSERT INTO `" + getTableName()
						+ "` VALUES(0,UNIX_TIMESTAMP())");
				con.commit();
			}
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException(ex.getMessage(), ex);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		int next = 0;
		if (this.maxId.get() <= this.nextId.get()) {
			if (getLockService().tryLock(getLockName())) {
				try {
					Connection con = null;
					Statement stmt = null;
					try {
						con = getDataSource().getConnection();
						con.setAutoCommit(false);
						stmt = con.createStatement();
						String columnName = getSequenceName();
						if (isSameCycle(con, stmt)) {
							stmt.executeUpdate("UPDATE " + getTableName()
									+ " SET " + columnName
									+ " = LAST_INSERT_ID(" + columnName + " + "
									+ getCacheSize() + ")," + columnName
									+ "_TIMESTAMP = UNIX_TIMESTAMP()");
						} else {
							stmt.executeUpdate("UPDATE " + getTableName()
									+ " SET " + columnName
									+ " = LAST_INSERT_ID(" + getCacheSize()
									+ ")," + columnName
									+ "_TIMESTAMP = UNIX_TIMESTAMP()");
						}
						con.commit();
						ResultSet rs = null;
						try {
							rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
							if (!rs.next()) {
								throw new DataAccessResourceFailureException(
										"LAST_INSERT_ID() failed after executing an update");
							}
							int max = rs.getInt(1);
							next = max - getCacheSize() + 1;
							synchronized (this) {
								this.nextId.set(next);
								this.maxId.set(max);
							}
						} finally {
							if (rs != null)
								rs.close();
						}

					} catch (SQLException ex) {
						throw new DataAccessResourceFailureException(
								"Could not obtain last_insert_id()", ex);
					} finally {
						if (stmt != null)
							try {
								stmt.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						if (con != null)
							try {
								con.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
					}
				} finally {
					getLockService().unlock(getLockName());
				}
			} else {
				try {
					Thread.sleep(100);
					return nextStringValue();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			next = this.nextId.incrementAndGet();
		}
		return getStringValue(thisTimestamp, getPaddingLength(), next);
	}

	protected boolean isSameCycle(Connection con, Statement stmt)
			throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT  " + getSequenceName()
				+ "_TIMESTAMP,UNIX_TIMESTAMP() FROM " + getTableName());
		try {
			rs.next();
			Long last = rs.getLong(1);
			if (last < 10000000000L) // no mills
				last *= 1000;
			lastTimestamp = new Date(last);
			thisTimestamp = new Date(rs.getLong(2) * 1000);
		} finally {
			rs.close();
		}
		return getCycleType().isSameCycle(lastTimestamp, thisTimestamp);
	}

}
