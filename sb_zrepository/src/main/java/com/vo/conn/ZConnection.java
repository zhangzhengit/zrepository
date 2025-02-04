package com.vo.conn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import com.vo.DBEnum;
import com.vo.DataSourceDTO;
import com.vo.ZRepositoryMain;
import com.vo.core.ZLog2;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 *
 * 数据库连接对象
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
@Data
public class ZConnection {

	private static final ZLog2 LOG = ZLog2.getInstance();

	private Boolean busy;
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
	public synchronized void commit() {
		try {
			this.connection.commit();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 重置为 java.sql.Connnection 默认的隔离级别
	 */
	public void resetToDefaultTransactionIsolation() {
		try {
			this.connection.setTransactionIsolation(this.transactionIsolation);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}


	public void setAutoCommitTrue() {
		try {
			this.connection.setAutoCommit(true);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

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
					StrUtil.isEmpty(userName) ? DriverManager.getConnection(url)
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

}
