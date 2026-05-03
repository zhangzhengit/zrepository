package com.vo.conn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import com.vo.DBEnum;
import com.vo.DataSourceDTO;
import com.vo.cache.STU;
import com.vo.log.core.ZLog2;
import com.vo.transaction.ZIsolationEnum;

/**
 *
 * 数据库连接对象
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
// FIXME 2025年2月7日 下午11:46:04 zhangzhen : 去掉了lombok后，继续改，尽量不要让connection传递出去
public class ZConnection {

	private static final ZLog2 LOG = ZLog2.getInstance();

	private boolean busy;
	private Mode mode;
	private DBEnum dbEnum;

	private String driverClass;
	private String url;
	private String userName;
	private String pwd;
	private Connection connection;

	/**
	 * java.sql.Connnection对象创建时，此对象的默认隔离级别
	 */
	private final int transactionIsolation;

	/**
	 * java.sql.Connnection 的隔离级别是否由 transactionIsolation 改变了
	 */
	private boolean transactionIsolationChanged = false;

	/**
	 * 回滚事务
	 */
	public synchronized void rollback() {
		try {
			this.connection.rollback();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 提交当前事务
	 */
	public synchronized void commitIfAutoCommitFalse() {
		try {
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 如果 java.sql.Connnection 从默认的隔离级别修改过，则重置为它默认的隔离级别
	 *
	 * @return 返回是否重置了(是否重置为了默认的 transactionIsolation )
	 */
	public boolean resetToDefaultTransactionIsolationIfChanged() {
		if (this.transactionIsolationChanged) {
			try {
				this.connection.setTransactionIsolation(this.transactionIsolation);
				this.transactionIsolationChanged = false;
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			return true;
		}

		return false;
	}

	/**
	 * 设置 java.sql.Connection 自动提交为true
	 */
	public void setAutoCommitTrue() {
		try {
			this.connection.setAutoCommit(true);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置 java.sql.Connection 自动提交为false
	 */
	public void setAutoCommitFalse() {
		try {
			this.connection.setAutoCommit(false);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static synchronized ZConnection newConnection(final ZDatasourceProperties.P p) {

		try {
			final String c = p.getDatasourceDriverClass();
			Class.forName(c);
		} catch (final ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		final String url = p.getDatasourceUrl();
		final String userName = p.getDatasourceUsername();
		final String pwd = p.getDatasourcePassword();
		try {
			final Connection connection =
					STU.isEmpty(userName) ? DriverManager.getConnection(url)
							: DriverManager.getConnection(url, userName, pwd);

			final int transactionIsolation = connection.getTransactionIsolation();
			final ZConnection zConnection = new ZConnection(transactionIsolation);

			zConnection.setDriverClass(p.getDatasourceDriverClass());
			zConnection.setUrl(p.getDatasourceUrl());
			zConnection.setUserName(p.getDatasourceUsername());
			zConnection.setPwd(p.getDatasourcePassword());

			final DataSourceDTO dataSourceDTO = ZRepositoryMain.findCatalog(p.getDatasourceUrl());
			zConnection.setDbEnum(dataSourceDTO.getDbEnum());

			zConnection.setBusy(false);
			zConnection.setConnection(connection);

			return zConnection;

		} catch (final SQLException e) {
			final String exceptionMessage = gExceptionMessage(e);
			LOG.error("创建数据库连接失败,exceptionMessage={}", exceptionMessage);
			e.printStackTrace();
			LOG.error("创建数据库连接失败,已关闭程序.请看上面的异常信息");
			System.exit(0);
		}

		return null;
	}

	public static String gExceptionMessage(final Throwable e) {

		if (Objects.isNull(e)) {
			return "";
		}

		final StringWriter stringWriter = new StringWriter();
		final PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);

		return stringWriter.toString();
	}

	public boolean getBusy() {
		return this.busy;
	}

	public void setBusy(final boolean busy) {
		this.busy = busy;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setMode(final Mode mode) {
		this.mode = mode;
	}

	public DBEnum getDbEnum() {
		return this.dbEnum;
	}

	public void setDbEnum(final DBEnum dbEnum) {
		this.dbEnum = dbEnum;
	}

	public String getDriverClass() {
		return this.driverClass;
	}

	public void setDriverClass(final String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPwd() {
		return this.pwd;
	}

	public void setPwd(final String pwd) {
		this.pwd = pwd;
	}

	Connection getConnection() {
		return this.connection;
	}

	public void setTransactionIsolation(final ZIsolationEnum isolationEnum) {
		try {
			this.connection.setTransactionIsolation(isolationEnum.getIsolation());
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	void setConnection(final Connection connection) {
		this.connection = connection;
	}

	public boolean isTransactionIsolationChanged() {
		return this.transactionIsolationChanged;
	}

	public void setTransactionIsolationChanged(final boolean transactionIsolationChanged) {
		this.transactionIsolationChanged = transactionIsolationChanged;
	}

	public int getTransactionIsolation() {
		return this.transactionIsolation;
	}

	public ZConnection(final int transactionIsolation) {
		this.transactionIsolation = transactionIsolation;
	}

}
