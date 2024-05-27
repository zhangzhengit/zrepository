package com.vo.conn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import com.vo.core.ZLog2;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 *
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

	private String driverClass;
	private String url;
	private String userName;
	private String pwd;
	private Connection connection;


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
			final ZConnection zc = new ZConnection();

			zc.setDriverClass(p.getDatasourceDriverClass());
			zc.setUrl(p.getDatasourceUrl());
			zc.setUserName(p.getDatasourceUsername());
			zc.setPwd(p.getDatasourcePassword());

			zc.setBusy(false);
			zc.setConnection(connection);

			return zc;

		} catch (final SQLException e) {
			final String exceptionMessage = gExceptionMessage(e);
			LOG.error("获取新连接失败,exceptionMessage={}", exceptionMessage);
			e.printStackTrace();
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

		final String eMessage = stringWriter.toString();

		return eMessage;
	}

}
