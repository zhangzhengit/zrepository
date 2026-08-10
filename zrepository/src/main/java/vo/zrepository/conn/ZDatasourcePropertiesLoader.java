package vo.zrepository.conn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import vo.log.core.ZLog2;
import vo.vortex.cache.ZRC;
import vo.vortex.common.STU;
import vo.zrepository.conn.ZDatasourceProperties.P;


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
	public static final String DATASOURCE_PROPERTIES_PATH_1 = "config/";
	public static final String DATASOURCE_PROPERTIES_PATH_3 = "src/main/resources/";
	public static final String DATASOURCE_PROPERTIES_PATH_4 = "src/main/resources/config/";

	public static ZDatasourceProperties getInstance(final String dataSourceName) {
		return ZRC.singleton().computeIfAbsent(dataSourceName, () ->initialize(dataSourceName));
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

		final int readCount = existReadCount ? Integer.parseInt(getProperties(dataSourceName).getProperty("datasource.read.count"))
				: DEFAULT_READ_COUNT;
		zDatasourceProperties.setDatasourceReadUrlCount(readCount);

		final boolean showSql = Boolean.parseBoolean(getProperties(dataSourceName).getProperty("datasource.showsql"));
		if (showSql) {
			zDatasourceProperties.setShowSql(true);
		} else {
			zDatasourceProperties.setShowSql(false);
		}

		final List<P> readList = new ArrayList<>();
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

		return zDatasourceProperties;
	}

	private static P newReadDP(final int i, final String dataSourceName) {
		final ZDatasourceProperties.P read = new P();
		final Properties px = getProperties(dataSourceName);
		final String url = px.getProperty("datasource.read.url[" + i + "]");
		if (STU.isEmpty(url)) {
			LOG.error("datasource.read.url[" + i + "]不存在");
			System.exit(0);
		}
		final boolean notNeedUserNameAndPassword = url.contains("jdbc:sqlite");
		read.setDatasourceUrl(url);

		final String userName = px.getProperty("datasource.read.username[" + i + "]");
		if (STU.isEmpty(userName) && !notNeedUserNameAndPassword) {
			LOG.error("datasource.read.userName[" + i + "]不存在");
			System.exit(0);
		}

		read.setDatasourceUsername(userName);

		final String password = px.getProperty("datasource.read.password[" + i + "]");
		if (STU.isEmpty(password)) {
			// 允许password为空
			//			LOG.error("datasource.read.password[" + i + "]不存在");
			//			System.exit(0);
		}
		read.setDatasourcePassword(password);

		final String driverClass = px.getProperty("datasource.read.driverClass[" + i + "]");
		if (STU.isEmpty(driverClass)) {
			LOG.error("datasource.read.driverClass[" + i + "]不存在");
			System.exit(0);
		}
		read.setDatasourceDriverClass(driverClass);

		final int min = Integer.parseInt(px.getProperty("datasource.read.minConnection[" + i + "]"));
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
		final int max = Integer.parseInt(px.getProperty("datasource.read.maxConnection[" + i + "]"));
		if (max <= 0) {
			LOG.error("datasource.read.maxConnection[" + i + "]必须大于0");
			System.exit(0);
		}
		if (max < min) {
			LOG.error("datasource.read.maxConnection[" + i + "] 必须大于" + "datasource.read.minConnection[" + i + "]");
			System.exit(0);
		}

		if ((url.contains("jdbc:postgresql") || url.contains("jdbc:mysql")) && (max <= 1)) {
			LOG.error("datasource.read.maxConnection[" + i + "]必须大于1");
			System.exit(0);
		}

		if (url.contains("jdbc:sqlite") && (max != 1)) {
			LOG.error("datasource.read.maxConnection[" + i + "]必须等于1");
			System.exit(0);
		}

		read.setDatasourceMaxConnection(max);

		return read;
	}

	private static P newWriteDP(final String dataSourceName) {
		final ZDatasourceProperties.P write = new P();
		final Properties properties = getProperties(dataSourceName);
		final String url = properties.getProperty("datasource.write.url");

		if (STU.isEmpty(url)) {
			LOG.error("datasource.write.url 不存在");
			System.exit(0);
		}

		final boolean notNeedUserNameAndPassword = url.contains("jdbc:sqlite");
		write.setDatasourceUrl(url);

		final String userName = properties.getProperty("datasource.write.username");
		if (STU.isEmpty(userName) && !notNeedUserNameAndPassword) {
			LOG.error("datasource.write.username 不存在");
			System.exit(0);
		}
		write.setDatasourceUsername(userName);

		final String password = properties.getProperty("datasource.write.password");
		if (STU.isEmpty(password)) {
			// 允许password为空
			//			LOG.error("datasource.write.password 不存在");
			//			System.exit(0);
		}
		write.setDatasourcePassword(password);

		final String driverClass = properties.getProperty("datasource.write.driverClass");
		if (STU.isEmpty(driverClass)) {
			LOG.error("datasource.write.driverClass 不存在");
			System.exit(0);
		}
		write.setDatasourceDriverClass(driverClass);

		final boolean containsKeyMinConnection = properties.containsKey("datasource.write.minConnection");
		if(!containsKeyMinConnection) {
			LOG.error("datasource.write.minConnection 不存在");
			System.exit(0);
		}

		final int min = Integer.parseInt(properties.getProperty("datasource.write.minConnection"));
		if (min <= 0) {
			LOG.error("datasource.write.minConnection 必须大于0");
			System.exit(0);
		}

		write.setDatasourceMinConnection(min);

		final boolean containsKeyMaxConnection = properties.containsKey("datasource.write.maxConnection");
		if (!containsKeyMaxConnection) {
			LOG.error("datasource.write.maxConnection 不存在");
			System.exit(0);
		}

		final int max = Integer.parseInt(properties.getProperty("datasource.write.maxConnection"));
		if (max <= 0) {
			LOG.error("datasource.write.maxConnection 必须大于0");
			System.exit(0);
		}

		if (max < min) {
			LOG.error("datasource.write.maxConnection 必须大于 datasource.write.minConnection");
			System.exit(0);
		}

		if ((url.contains("jdbc:postgresql") || url.contains("jdbc:mysql")) && (max <= 1)) {
			LOG.error("datasource.write.maxConnection 必须大于 1");
			System.exit(0);
		}

		if (url.contains("jdbc:sqlite") && (max != 1)) {
			LOG.error("datasource.write.maxConnection 必须等于 1");
			System.exit(0);
		}

		write.setDatasourceMaxConnection(max);

		return write;
	}

	private static Properties getProperties(final String dataSourceName) {
		return ZRC.singleton().computeIfAbsent(dataSourceName, () -> getProperties0(dataSourceName));
	}

	private static Properties getProperties0(final String dataSourceName) {

		final Properties p1 = ZProperties.loadDirConfig(File.separator + "config" + File.separator);
		if (p1 != null) {
			return p1;
		}

		final Properties p2 = ZProperties.loadDirConfig("/" + dataSourceName);
		if (p2 != null) {
			return p2;
		}

		final Properties p3 = ZProperties.loadPResources("/config" + dataSourceName);
		if (p3 != null) {
			return p3;
		}

		final Properties p4 = ZProperties.loadPResources("/" + dataSourceName);
		if (p4 != null) {
			return p4;
		}

		// 到此为异常情况，直接退出程序
		LOG.error("配置文件[{}]不存在", dataSourceName);
		System.exit(0);
		return null;
	}
}
