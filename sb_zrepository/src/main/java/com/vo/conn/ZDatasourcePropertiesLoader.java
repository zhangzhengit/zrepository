package com.vo.conn;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.collect.Lists;
import com.vo.ZRC;
import com.vo.conn.ZDatasourceProperties.P;
import com.vo.core.ZLog2;

import cn.hutool.core.util.StrUtil;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年6月17日
 *
 */
public class ZDatasourcePropertiesLoader {

	private static final ZLog2 LOG = ZLog2.getInstance();

	private static final int DEFAULT_READ_COUNT = 1;

	public static final int DEAULT_MAX_CONNECTION = 10;

	public static final int DEAULT_MIN_CONNECTION = 1;

	public static final String DEFAULT_DATSOURCE_NAME = "zdatasource.properties";

	public static final String DATASOURCE_PROPERTIES_PATH = "";

	public static final String DATASOURCE_PROPERTIES_PATH_1 = "config/";
	public static final String DATASOURCE_PROPERTIES_PATH_2 = "";
	public static final String DATASOURCE_PROPERTIES_PATH_3 = "src/main/resources/";
	public static final String DATASOURCE_PROPERTIES_PATH_4 = "src/main/resources/config/";

	private static ZDatasourceProperties INSTANCE;

	public static ZDatasourceProperties getInstance(final String dataSourceName) {
		return ZRC.computeIfAbsent(dataSourceName, () ->initialize(dataSourceName));
	}

	private static ZDatasourceProperties initialize(final String dataSourceName) {
		final ZDatasourceProperties zDatasourceProperties = new ZDatasourceProperties();

		try {
			final P newWriteDP = newWriteDP(dataSourceName);
			zDatasourceProperties.setWrite(newWriteDP);
		} catch (final Exception e) {
			throw new IllegalArgumentException("读取datasource.write配置出错，请检查配置文件:" + dataSourceName);
		}

		final boolean existReadCount = getProperties(dataSourceName).containsKey("datasource.read.count");
		if (!existReadCount) {
			LOG.error("datasource.read.count 不存在");
			System.exit(0);
		}

		final int readCount = existReadCount ? getProperties(dataSourceName).getInt("datasource.read.count") : DEFAULT_READ_COUNT;
		zDatasourceProperties.setDatasourceReadUrlCount(readCount);

		final boolean showSql = getProperties(dataSourceName).getBoolean("datasource.showsql");
		if (showSql) {
			zDatasourceProperties.setShowSql(true);
		} else {
			zDatasourceProperties.setShowSql(false);
		}

		final List<P> readList = Lists.newArrayList();
		try {
			final P read1 = newReadDP(0, dataSourceName);
			readList.add(read1);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("读取datasource.read配置出错，请检查配置文件");
		}

		for (int i = 1; i < readCount; i++) {
			readList.add(newReadDP(i, dataSourceName));
		}

		zDatasourceProperties.setReadList(readList);

		INSTANCE = zDatasourceProperties;

		return zDatasourceProperties;
	}

	private static P newReadDP(final int i, final String dataSourceName) {
		final ZDatasourceProperties.P read = new P();
		final PropertiesConfiguration px = getProperties(dataSourceName);
		final String url = px.getString("datasource.read.url[" + i + "]");
		if (StrUtil.isEmpty(url)) {
			LOG.error("datasource.read.url[" + i + "]不存在");
			System.exit(0);
		}
		final boolean notNeedUserNameAndPassword = url.contains("jdbc:sqlite");
		read.setDatasourceUrl(url);

		final String userName = px.getString("datasource.read.username[" + i + "]");
		if (StrUtil.isEmpty(userName) && !notNeedUserNameAndPassword) {
			LOG.error("datasource.read.userName[" + i + "]不存在");
			System.exit(0);
		}

		read.setDatasourceUsername(userName);

		final String password = px.getString("datasource.read.password[" + i + "]");
		if (StrUtil.isEmpty(password)) {
			// 允许password为空
			//			LOG.error("datasource.read.password[" + i + "]不存在");
			//			System.exit(0);
		}
		read.setDatasourcePassword(password);

		final String driverClass = px.getString("datasource.read.driverClass[" + i + "]");
		if (StrUtil.isEmpty(driverClass)) {
			LOG.error("datasource.read.driverClass[" + i + "]不存在");
			System.exit(0);
		}
		read.setDatasourceDriverClass(driverClass);

		final int min = px.getInt("datasource.read.minConnection[" + i + "]");
		if (!px.containsKey("datasource.read.minConnection[" + i + "]")) {
			LOG.error("datasource.read.minConnection[" + i + "]不存在");
			System.exit(0);
		}
		if (min <= 0) {
			LOG.error("datasource.read.minConnection[" + i + "]必须大于0");
			System.exit(0);
		}

		read.setDatasourceMinConnection(min);

		if (!px.containsKey("datasource.read.maxConnection[" + i + "]")) {
			LOG.error("datasource.read.maxConnection[" + i + "]不存在");
			System.exit(0);
		}
		final int max = px.getInt("datasource.read.maxConnection[" + i + "]");
		if (max <= 0) {
			LOG.error("datasource.read.maxConnection[" + i + "]必须大于0");
			System.exit(0);
		}
		if (max < min) {
			LOG.error("datasource.read.maxConnection[" + i + "] 必须大于" + "datasource.read.minConnection[" + i + "]");
			System.exit(0);
		}

		read.setDatasourceMaxConnection(max);

		return read;
	}

	private static P newWriteDP(final String dataSourceName) {
		final ZDatasourceProperties.P write = new P();
		final PropertiesConfiguration propertiesConfiguration = getProperties(dataSourceName);
		final String url = propertiesConfiguration.getString("datasource.write.url");

		if (StrUtil.isEmpty(url)) {
			LOG.error("datasource.write.url 不存在");
			System.exit(0);
		}

		final boolean notNeedUserNameAndPassword = url.contains("jdbc:sqlite");
		write.setDatasourceUrl(url);

		final String userName = propertiesConfiguration.getString("datasource.write.username");
		if (StrUtil.isEmpty(userName) && !notNeedUserNameAndPassword) {
			LOG.error("datasource.write.username 不存在");
			System.exit(0);
		}
		write.setDatasourceUsername(userName);

		final String password = propertiesConfiguration.getString("datasource.write.password");
		if (StrUtil.isEmpty(password)) {
			// 允许password为空
			//			LOG.error("datasource.write.password 不存在");
			//			System.exit(0);
		}
		write.setDatasourcePassword(password);

		final String driverClass = propertiesConfiguration.getString("datasource.write.driverClass");
		if (StrUtil.isEmpty(driverClass)) {
			LOG.error("datasource.write.driverClass 不存在");
			System.exit(0);
		}
		write.setDatasourceDriverClass(driverClass);

		final int min = propertiesConfiguration.getInt("datasource.write.minConnection");
		final boolean containsKeyMinConnection = propertiesConfiguration.containsKey("datasource.write.minConnection");
		if(!containsKeyMinConnection) {
			LOG.error("datasource.write.minConnection 不存在");
			System.exit(0);
		}
		if (min <= 0) {
			LOG.error("datasource.write.minConnection 必须大于0");
			System.exit(0);
		}

		write.setDatasourceMinConnection(min);

		final int max = propertiesConfiguration.getInt("datasource.write.maxConnection");
		final boolean containsKeyMaxConnection = propertiesConfiguration.containsKey("datasource.write.maxConnection");
		if (!containsKeyMaxConnection) {
			LOG.error("datasource.write.maxConnection 不存在");
			System.exit(0);
		}
		if (max <= 0) {
			LOG.error("datasource.write.maxConnection 必须大于0");
			System.exit(0);
		}
		if (max < min) {
			LOG.error("datasource.write.maxConnection 必须大于 datasource.write.minConnection");
			System.exit(0);
		}

		write.setDatasourceMaxConnection(max);

		return write;
	}

	private static PropertiesConfiguration getProperties(final String dataSourceName) {
		try {
			return new PropertiesConfiguration(DATASOURCE_PROPERTIES_PATH + dataSourceName);
		} catch (final ConfigurationException e) {
			e.printStackTrace();
			try {
				return new PropertiesConfiguration(DATASOURCE_PROPERTIES_PATH_2 + dataSourceName);
			} catch (final ConfigurationException e1) {
				e1.printStackTrace();
				try {
					return new PropertiesConfiguration(DATASOURCE_PROPERTIES_PATH_3 + dataSourceName);
				} catch (final ConfigurationException e2) {
					e2.printStackTrace();
					try {
						return new PropertiesConfiguration(DATASOURCE_PROPERTIES_PATH_4 + dataSourceName);
					} catch (final ConfigurationException e3) {
						e3.printStackTrace();
					}
				}
			}
		}

		return null;
	}
}
