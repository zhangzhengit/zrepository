package com.vo.conn;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.expression.spel.ast.RealLiteral;

import com.google.common.collect.Lists;
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

	public static final String DATASOURCE_PROPERTIES = "zdatasource.properties";

	public static final String DATASOURCE_PROPERTIES_1 = "config/zdatasource.properties";
	public static final String DATASOURCE_PROPERTIES_2 = "zdatasource.properties";
	public static final String DATASOURCE_PROPERTIES_3 = "src/main/resources/zdatasource.properties";
	public static final String DATASOURCE_PROPERTIES_4 = "src/main/resources/config/zdatasource.properties";

	private static ZDatasourceProperties INSTANCE;

	public static ZDatasourceProperties getInstance() {
		return INSTANCE;
	}

	static {

		final ZDatasourceProperties zDatasourceProperties = new ZDatasourceProperties();

		try {
			final P newWriteDP = newWriteDP();
			zDatasourceProperties.setWrite(newWriteDP);
		} catch (final Exception e) {
			throw new IllegalArgumentException("读取datasource.write配置出错，请检查配置文件");
		}

		final boolean existReadCount = gs().containsKey("datasource.read.count");
		if (!existReadCount) {
			LOG.error("datasource.read.count 不存在");
			System.exit(0);
		}

		final int readCount = existReadCount ? gs().getInt("datasource.read.count") : DEFAULT_READ_COUNT;
		zDatasourceProperties.setDatasourceReadUrlCount(readCount);

		final boolean showSql = gs().getBoolean("datasource.showsql");
		if (showSql) {
			zDatasourceProperties.setShowSql(true);
		} else {
			zDatasourceProperties.setShowSql(false);
		}

		final List<P> readList = Lists.newArrayList();
		try {
			final P read1 = newReadDP(0);
			readList.add(read1);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("读取datasource.read配置出错，请检查配置文件");
		}

		for (int i = 1; i < readCount; i++) {
			readList.add(newReadDP(i));
		}

		zDatasourceProperties.setReadList(readList);

		INSTANCE = zDatasourceProperties;

	}

	private static P newReadDP(final int i) {
		final ZDatasourceProperties.P read = new P();
		final String url = gs().getString("datasource.read.url[" + i + "]");
		if (StrUtil.isEmpty(url)) {
			LOG.error("datasource.read.url[" + i + "]不存在");
			System.exit(0);
		}
		final boolean notNeedUserNameAndPassword = url.contains("jdbc:sqlite");
		read.setDatasourceUrl(url);

		final String userName = gs().getString("datasource.read.username[" + i + "]");
		if (StrUtil.isEmpty(userName) && !notNeedUserNameAndPassword) {
			LOG.error("datasource.read.userName[" + i + "]不存在");
			System.exit(0);
		}

		read.setDatasourceUsername(userName);

		final String password = gs().getString("datasource.read.password[" + i + "]");
		if (StrUtil.isEmpty(password)) {
			// 允许password为空
//			LOG.error("datasource.read.password[" + i + "]不存在");
//			System.exit(0);
		}
		read.setDatasourcePassword(password);

		final String driverClass = gs().getString("datasource.read.driverClass[" + i + "]");
		if (StrUtil.isEmpty(driverClass)) {
			LOG.error("datasource.read.driverClass[" + i + "]不存在");
			System.exit(0);
		}
		read.setDatasourceDriverClass(driverClass);

		final int min = gs().getInt("datasource.read.minConnection[" + i + "]");
		if (!gs().containsKey("datasource.read.minConnection[" + i + "]")) {
			LOG.error("datasource.read.minConnection[" + i + "]不存在");
			System.exit(0);
		}
		if (min <= 0) {
			LOG.error("datasource.read.minConnection[" + i + "]必须大于0");
			System.exit(0);
		}

		read.setDatasourceMinConnection(min);

		if (!gs().containsKey("datasource.read.maxConnection[" + i + "]")) {
			LOG.error("datasource.read.maxConnection[" + i + "]不存在");
			System.exit(0);
		}
		final int max = gs().getInt("datasource.read.maxConnection[" + i + "]");
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

	private static P newWriteDP() {
		final ZDatasourceProperties.P write = new P();
		final String url = gs().getString("datasource.write.url");

		if (StrUtil.isEmpty(url)) {
			LOG.error("datasource.write.url 不存在");
			System.exit(0);
		}

		final boolean notNeedUserNameAndPassword = url.contains("jdbc:sqlite");
		write.setDatasourceUrl(url);

		final String userName = gs().getString("datasource.write.username");
		if(StrUtil.isEmpty(userName) && !notNeedUserNameAndPassword) {

			LOG.error("datasource.write.username 不存在");
			System.exit(0);
		}
		write.setDatasourceUsername(userName);

		final String password = gs().getString("datasource.write.password");
		if (StrUtil.isEmpty(password)) {
			// 允许password为空
//			LOG.error("datasource.write.password 不存在");
//			System.exit(0);
		}
		write.setDatasourcePassword(password);

		final String driverClass = gs().getString("datasource.write.driverClass");
		if (StrUtil.isEmpty(driverClass)) {
			LOG.error("datasource.write.driverClass 不存在");
			System.exit(0);
		}
		write.setDatasourceDriverClass(driverClass);

		final int min = gs().getInt("datasource.write.minConnection");
		final boolean containsKeyMinConnection = gs().containsKey("datasource.write.minConnection");
		if(!containsKeyMinConnection) {
			LOG.error("datasource.write.minConnection 不存在");
			System.exit(0);
		}
		if (min <= 0) {
			LOG.error("datasource.write.minConnection 必须大于0");
			System.exit(0);
		}

		write.setDatasourceMinConnection(min);

		final int max = gs().getInt("datasource.write.maxConnection");
		final boolean containsKeyMaxConnection = gs().containsKey("datasource.write.maxConnection");
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

	private static PropertiesConfiguration gs() {
		try {
			return new PropertiesConfiguration(DATASOURCE_PROPERTIES);
		} catch (final ConfigurationException e) {
			e.printStackTrace();
			try {
				return new PropertiesConfiguration(DATASOURCE_PROPERTIES_2);
			} catch (final ConfigurationException e1) {
				e1.printStackTrace();
				try {
					return new PropertiesConfiguration(DATASOURCE_PROPERTIES_3);
				} catch (final ConfigurationException e2) {
					e2.printStackTrace();
					try {
						return new PropertiesConfiguration(DATASOURCE_PROPERTIES_4);
					} catch (final ConfigurationException e3) {
						e3.printStackTrace();
					}
				}
			}
		}

		return null;

	}
}
