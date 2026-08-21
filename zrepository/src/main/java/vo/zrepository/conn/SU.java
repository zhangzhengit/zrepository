package vo.zrepository.conn;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import vo.log.core.ZLog2;
import vo.vortex.cache.ZRC;
import vo.vortex.common.AU;
import vo.vortex.common.CU;
import vo.vortex.common.RU;
import vo.vortex.common.STU;
import vo.vortex.core.ZContext;
import vo.zrepository.actuator.SqlInvocationLogsConfigurationProperties;
import vo.zrepository.actuator.SqlInvocationLogsEntity;
import vo.zrepository.actuator.SqlInvocationLogsService;
import vo.zrepository.anno.ZCSourceEnum;
import vo.zrepository.anno.ZEHEnum;
import vo.zrepository.anno.ZEntity;
import vo.zrepository.anno.ZID;
import vo.zrepository.anno.ZQuery;
import vo.zrepository.anno.ZTransient;
import vo.zrepository.core.MethodRegex;
import vo.zrepository.core.Page;
import vo.zrepository.core.SUA;
import vo.zrepository.core.Sort;
import vo.zrepository.core.ZAllHandler;
import vo.zrepository.core.ZC2;
import vo.zrepository.core.ZDeleteByIdHandler;
import vo.zrepository.core.ZEntityHandler;
import vo.zrepository.core.ZEntityHandlerScanner;
import vo.zrepository.core.ZFieldConverter;
import vo.zrepository.core.ZRWrapper;
import vo.zrepository.core.ZRepository;
import vo.zrepository.enums.DBEnum;
import vo.zrepository.enums.SQLEMode;
import vo.zrepository.enums.SUEnum;
import vo.zrepository.enums.SUMode;
import vo.zrepository.enums.ZGenerationType;
import vo.zrepository.exception.ZQuerySQLException;
import vo.zrepository.exception.ZRepositoryException;
import vo.zrepository.transaction.ZIsolationEnum;
import vo.zrepository.transaction.ZTransactionAOP;

/**
 * @see ZRepository 接口和其子接口里的方法的具体实现
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
// FIXME 2023年9月16日 下午7:57:12 zhanghen: 考虑清楚每个方法 @ZID 字段为空怎么处理
// FIXME 2024年5月18日 下午12:29:49 zhangzhen: LOG 要不要使用 PreparedStatement.toString 代替？

// FIXME 2024年6月5日 下午10:46:38 zhangzhen : 所有log.xx信息，要仔细考虑参数类型，
// 如：
//	1、Date类型，要统一格式输出yyyy-MM-dd HH:mm:ss
//	2、数组类型，要Arrays.toString输出 等等

// FIXME 2024年11月26日 下午7:16:14 zhangzhen : TODO : 仔细考虑，每个方法对于byte[]怎么处理，

public class SU {

	private static final ZLog2 LOG = ZLog2.getInstance();

	private static final int NO_DELETE_OR_DELETE = -1;


	// FIXME 2024年6月2日 上午12:09:26 zhangzhen : 本类所有方法都计入了className和callerMethodName,用来做sql执行统计功能用，待做

	public static <T> Page<T> page(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final ZRWrapper<T> wrapper, final Integer size, final Integer page) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		if (wrapper == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".page方法: wrapper 参数不能为null");
		}

		wrapper.fetchPage(page, size);

		if (size == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".page方法: size 参数不能为null");
		}

		if (size <= 0) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".page方法: size 参数必须大于0！size = " + size);
		}

		if (page == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".page方法: page 参数不能为null");
		}

		if (page <= 0) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".page方法: page 参数必须大于0！page = " + page);
		}

		PreparedStatement ps=null;
		ResultSet rs=null;
		PreparedStatement psc = null;
		ResultSet pscRS = null;
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();
		try {

			final String pageSql =
					wrapper.done().getSql();

			final SUA suapageSql = excludedDeletedHandler(entityClass, null, returnType, pageSql, null, zc);
			final String pageSqlFinal2 = suapageSql.getSql();

			final String pageCountSqlT =
					MethodRegex.SELECT + Sort.SPACE
					+ "COUNT(*)"
					+ Sort.SPACE + MethodRegex.FROM
					+ Sort.SPACE
					+ entityClass.getAnnotation(ZEntity.class).tableName()
					+ Sort.SPACE + MethodRegex.WHERE
					+ Sort.SPACE + wrapper.done().getWhere();

			final SUA suapageCountSql = excludedDeletedHandler(entityClass, null, returnType, pageCountSqlT, null, zc);

			final String pageCountSql2 = suapageCountSql.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}分页查询：{}]", zrSubClassName, callerMethodName, pageSqlFinal2);
				LOG.info("[{}.{}总条数查询：{}]",zrSubClassName, callerMethodName, pageCountSql2);
			}

			ps = connection.prepareStatement(pageSqlFinal2);

			rs = ps.executeQuery();
			final ResultSetMetaData metaData = rs.getMetaData();

			final int count = metaData.getColumnCount();
			final List<Object> rL = new ArrayList<>();
			while (rs.next()) {
				final Object tR = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				rL.add(tR);
			}

			psc = connection.prepareStatement(pageCountSql2);
			pscRS = psc.executeQuery();
			pscRS.next();

			final Long countR = pscRS.getLong(1);
			final long pages = (countR.longValue() % size) == 0 ? countR.longValue() / size
					: (countR.longValue() / size) + 1;
			return new Page(size, Long.parseLong(String.valueOf(page)), pages, countR, rL);

		} catch (final SQLException | IllegalArgumentException  e1) {
			e1.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		} finally {
			close(ps, rs, psc, pscRS);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return new Page(size, Long.parseLong(String.valueOf(page)), 0L, 0L, new ArrayList<>());
	}

	private static Boolean isShowSQL(final String dataSourceName) {
		return ZDatasourcePropertiesLoader.getInstance(dataSourceName).getShowSql();
	}

	/**
	 * 获取T对象里非空的字段，返回<字段名称,字段值>
	 *
	 * @param <T>
	 * @param t
	 * @return
	 *
	 */
	private static <T> Map<String, Object> getNotNullFieldMap(final Object t) {
		final Map<String, Object> fMap = new LinkedHashMap<>();
		final Field[] fs = t.getClass().getDeclaredFields();
		for (final Field f : fs) {
			f.setAccessible(true);
			try {
				final Object v = f.get(t);
				if (v == null) {
					continue;
				}

				fMap.put(f.getName(), v);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return fMap;
	}

	public static <T> Boolean update(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class entityClass, final Object t, final String sql) {

		if (t == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".update方法: t 参数不能为null");
		}

		final Field[] fs = t.getClass().getDeclaredFields();
		final List<Field> aa = new ArrayList<>();
		Collections.addAll(aa, fs);
		final Optional<Field> zidO = aa.stream().filter(f -> f.isAnnotationPresent(ZID.class)).findAny();
		if (!zidO.isPresent()) {
			throw new IllegalArgumentException(
					"无 " + ZID.class.getSimpleName() + " 标记的属性，t = " + t.getClass().getName());
		}

		final Object idValue = getUpdateIdValue(t, zidO);
		if (Objects.isNull(idValue)) {
			throw new IllegalArgumentException("update方法参数 t 的 " + ZID.class.getSimpleName() + " 字段不能为空！t = " + t);
		}


		// update blobt set COLUMN where id = ?;
		final String gUpdateColumn = gUpdateColumn(t, fs);

		final String sqlF = sql.replace(MethodRegex.COLUMN, gUpdateColumn);

		final AtomicReference<String> sqlFAR = new AtomicReference<>(sqlF);

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final SUA updateHandler = updateHandler(entityClass, t, sqlF, zc);

		sqlFAR.set(updateHandler.getSql());

		final Connection connection = zc.getZConnection().getConnection();
		PreparedStatement ps  = null;
		try {
			ps = connection.prepareStatement(sqlFAR.get());
			int zTransientCount = 0;
			int index = 0;
			for (int i = 0; i < (fs.length); i++) {
				final Field f = fs[i];
				if (f.isAnnotationPresent(ZTransient.class)) {
					zTransientCount++;
					continue;
				}
				// 兼顾pgsql，@ZID字段不可以update
				if (f.isAnnotationPresent(ZID.class)) {
					continue;
				}

				f.setAccessible(true);
				index++;
				addPS(zc.getZConnection().getDbEnum(), t, ps, index, f, SUMode.UPDATE);
			}

			// 最后面的where id = ？ 赋值
			ps.setObject((fs.length) - zTransientCount, idValue);

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}],[{}]", zrSubClassName, callerMethodName, sqlFAR.get(), t,idValue);
			}

			final int executeUpdate = ps.executeUpdate();
			return executeUpdate > 0;

		} catch (final Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	private static <T> SUA updateHandler(final Class<T> entityClass, final Object t, final String sqlF, final ZC2 zc2) {
		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.UPDATE);
		final SUA sua = new SUA(entityClass, t, entityClass, sqlF, null);
		sua.setZc2(zc2);
		sh.forEach(h -> h.handle(sua));

		return sua;
	}

	private static <T> Object getUpdateIdValue(final Object t, final Optional<Field> zidO) {
		final Field idField = zidO.get();

		idField.setAccessible(true);
		Object idValue = null;
		try {
			idValue = idField.get(t);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		return idValue;
	}

	public static <T> boolean deleteAll(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final String sql) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();


		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.DELETE_ALL);
		final SUA sua = new SUA(entityClass, null, entityClass, sql, null);
		sua.setZc2(zc);
		sua.setZrSubClassName(zrSubClassName);
		sua.setCallerMethodName(callerMethodName);
		sh.forEach(h -> h.handle(sua));

		PreparedStatement ps =null;
		try {

			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}]", zrSubClassName, callerMethodName, s);
			}

			ps = connection.prepareStatement(s);

			final int executeUpdate = ps.executeUpdate();

			return executeUpdate >= 1;

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> boolean deleteByIdIn(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final List<Object> idList, final Class<T> entityClass, final String sql) {

		if ((idList == null) || idList.isEmpty()) {
			return false;
		}

		final Set<Object> idSet = new HashSet<>(idList);

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;

		try {

			final String params = String.join(",", Collections.nCopies(idSet.size(), "?"));

			final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.DELETE_Logical);
			final SUA sua = new SUA(entityClass, null, null, sql, null);
			sua.setZc2(zc);
			sh.forEach(h -> ((ZDeleteByIdHandler) h).handle(sua));

			final String s2 = sua.getSql();
			final String sqlT = s2.replace("?", params);

			ps = connection.prepareStatement(sqlT);
			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{},[共{}个条件]]", zrSubClassName, callerMethodName, s2,idSet.size());
			}

			int index = 1;
			for (final Object id : idSet) {
				ps.setObject(index, id);
				index++;
			}

			final int executeUpdate = ps.executeUpdate();

			return executeUpdate == idSet.size();

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> boolean deleteById(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Object id, final Class<T> entityClass, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();


		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.DELETE_Logical);
		final SUA sua = new SUA(entityClass, null, null, sql,new Object[] { id});
		sua.setZc2(zc);
		sh.forEach(h -> ((ZDeleteByIdHandler)h).handle(sua));

		final String s = sua.getSql();

		PreparedStatement ps = null;
		try {

			ps= connection.prepareStatement(s);
			ps.setObject(1, id);

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{},[{}]]",zrSubClassName,callerMethodName, s, id);
			}

			final int executeUpdate = ps.executeUpdate();

			return executeUpdate >= 1;

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> Map<Object, Boolean> existByIdIn(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Object idList, final Class<T> entityClass, final String sql) {

		final Map<Object, Boolean> v = new HashMap<>();
		if (idList == null) {
			v.put(null, false);
			return v;
		}

		final List idX = (List) ((List) idList).stream()
				.distinct().collect(Collectors.toList());
		if (CU.isEmpty(idX)) {
			return v;
		}

		final List idNullList = (List) idX.stream().filter(x -> x == null).collect(Collectors.toList());
		if (CU.isNotEmpty(idNullList)) {
			v.put(null, false);
		}
		final List idNotNullList = (List) idX.stream().filter(x -> x != null).collect(Collectors.toList());

		// select ZID,count(*) from TABLE_NAME where @ in (?) group by ZID;

		final Field zidF = getZID(entityClass);
		final String dbColumnName = ZFieldConverter.toDbField(zidF.getName());
		final String sqlF = sql.replace(MethodRegex.ZID, dbColumnName);

		final StringJoiner idJoiner = new StringJoiner(",");
		for (final Object id : idNotNullList) {
			idJoiner.add(String.valueOf(id));
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);

		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sqlF, null, zc);
		final String sqlF1 = sua.getSql();
		final String sqlF2 = sqlF1.replaceFirst("\\?", idJoiner.toString());


		// 开始查询
		if (isShowSQL(dataSourceName)) {
			LOG.info("[{}.{}：{},[共{}个条件]]",zrSubClassName,callerMethodName, sqlF1, idNotNullList.size());
		}

		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sqlF2);

			rs = ps.executeQuery();
			while (rs.next()) {
				final Object id = rs.getObject(1);
				final Object count = rs.getObject(2);
				v.put(id, (count != null) && (Long.parseLong(String.valueOf(count)) >= 1));
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		for (final Object id : idNotNullList) {
			if (!v.containsKey(id)) {
				v.put(id, false);
			}
		}

		return v;
	}

	public static <T> boolean existById(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Object id, final Class<T> entityClass, final String sql) {

		if (Objects.isNull(id)) {
			return false;
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sql,new Object[] { id}, zc );
		sua.setZc2(zc);

		final String s = sua.getSql();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{},[{}]]",zrSubClassName,callerMethodName, s,id);
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				final int count = rs.getInt(1);
				return count >= 1;
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> List<Object> saveAll(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> cls, final String sql,
			final List<T> tList) {

		if (CU.isEmpty(tList)) {
			return Collections.emptyList();
		}

		final List<T> tl2 = tList.stream().filter(x -> x!=null).collect(Collectors.toList());

		final String dataSourceName = getDataSourceNameFromClassType(cls);

		final DBEnum db = getDBFromDataSourceName(dataSourceName);
		switch (db) {

		case SQLITE:
			// FIXME 2024年5月27日 下午5:54:36 zhangzhen: 对于sqlite而专门特别处理，从批量插入改为在一个事务里执行多次insert.
			// save0里的日志还要改，区分是从save还是saveAll方法来的

			final ZC2 zc2 = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
			final ZConnection zc = zc2.getZConnection();
			final Connection connection = zc.getConnection();

			final List<Object> idl = new ArrayList<>(tl2.size());
			for (final Object t : tl2) {
				try {
					final Object[] a = save0(zrSubClassName, callerMethodName, zc.getDbEnum(), cls, t, sql, connection, null);
					final ResultSet rs = (ResultSet) a[0];
					if (rs.next()) {
						final Object id = rs.getObject(1);
						idl.add(id);
					}
					close((AutoCloseable)a[0],(AutoCloseable)a[1]);
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}

			returnZConnectionAndCommitIfZCPool(dataSourceName, zc2);
			return idl;

		case MYSQL:
		case POSTGRESQL:
			return saveAllMysqlAndPGSQL(zrSubClassName, callerMethodName, mode, cls, sql, tl2);

		default:
			break;
		}

		return Collections.emptyList();
	}

	private static DBEnum getDBFromDataSourceName(final String dataSourceName) {
		return ZRepositoryMain.getDB(dataSourceName);
	}

	private static <T> List<Object> saveAllMysqlAndPGSQL(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final String sqlParam, final List<T> tList) {

		final Field[] declaredFields = entityClass.getDeclaredFields();
		final ArrayList<Field> aa = new ArrayList<>();
		Collections.addAll(aa, declaredFields);
		final Optional<Field> zid = aa.stream()
				.filter(f -> f.isAnnotationPresent(ZID.class)).findAny();
		if (!zid.isPresent()) {
			throw new IllegalArgumentException(
					"类中无 " + ZID.class.getSimpleName() + " 字段，cls = " + entityClass.getName());
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final ZConnection zConnection = zc.getZConnection();
		final Connection connection = zConnection.getConnection();

		final String sql = generateSaveAllSQL(entityClass, sqlParam);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			tList.parallelStream().forEach(t -> {
				final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.SAVE);
				final SUA sua = new SUA(entityClass, t, entityClass, sql, null);
				sh.forEach(h -> h.handle(sua));
			});

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{},[共插入{}条数据]]",zrSubClassName,callerMethodName, sql,tList.size());
			}

			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			final PreparedStatement ps2 = ps;
			tList.forEach(t -> {
				int index = 1;
				for (final Field f : declaredFields) {
					if (f.isAnnotationPresent(ZID.class) || f.isAnnotationPresent(ZTransient.class)) {
						continue;
					}
					try {
						addPS(zConnection.getDbEnum(), t, ps2, index, f, SUMode.SAVE);
					} catch (final SQLException e) {
						e.printStackTrace();
					}
					index++;
				}
				try {
					ps2.addBatch();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			});



			ps.executeBatch();

			rs = ps.getGeneratedKeys();

			final Field idField = zid.get();
			final Class<?> type = idField.getType();

			final List<Object> r = new ArrayList<>();
			while (rs.next()) {
				// 类型转换
				final Object id = rs.getObject(1);
				if (type.getName().equals(String.class.getName())) {
					r.add(String.valueOf(id));
				} else if (type.getName().equals(Integer.class.getName())) {
					r.add(Integer.valueOf(String.valueOf(id)));
				} else if (type.getName().equals(Byte.class.getName())) {
					r.add(Byte.valueOf(String.valueOf(id)));
				} else if (type.getName().equals(Short.class.getName())) {
					r.add(Short.valueOf(String.valueOf(id)));
				} else if (type.getName().equals(Long.class.getName())) {
					r.add(Long.valueOf(String.valueOf(id)));
				} else if (type.getName().equals(Double.class.getName())) {
					r.add(Double.valueOf(String.valueOf(id)));
				} else if (type.getName().equals(BigInteger.class.getName())) {
					r.add(new BigInteger(String.valueOf(id)));
				} else if (type.getName().equals(BigDecimal.class.getName())) {
					r.add(new BigDecimal(String.valueOf(id)));
				} else if (type.getName().equals(Character.class.getName())) {
					r.add(Character.valueOf(String.valueOf(id).charAt(0)));
				}
			}
			return r;

		} catch (final Exception e) {
			e.printStackTrace();
			try {
				// XXX sqlite 在16GB的傲腾上面测试连续批量insert，硬盘满了会报错：[SQLITE_FULL] Insertion failed because database is full (database or disk is full)
				// mysql和pgsql还没测
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}

		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	private static <T> String generateSaveAllSQL(final Class<T> cls, final String sql) {

		final Supplier<String> supplier = () -> {
			final StringJoiner arg = new StringJoiner(",");
			final StringJoiner v = new StringJoiner(",");
			for (final Field field : cls.getDeclaredFields()) {
				if (field.isAnnotationPresent(ZID.class) || field.isAnnotationPresent(ZTransient.class)) {
					continue;
				}

				final String dbFieldname = ZFieldConverter.toDbField(field.getName());
				arg.add(dbFieldname);
				v.add("?");
			}

			return sql.replace(MethodRegex.COLUMNS, arg.toString()).replace(MethodRegex.COLUMN_VALUES, v.toString());
		};

		final String key = cls.getName() + "@" + sql;

		return ZRC.singleton().computeIfAbsent(key, supplier);
	}

	public static <T> T save(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass,
			final Class<?> entityTName, final Object t, final String sql) {

		if (t == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".save方法: t 参数不能为null");
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();
		try {

			final Object[] a = save0(zrSubClassName, callerMethodName, zc.getZConnection().getDbEnum(), entityClass, t, sql, connection, null);
			final ResultSet rs = (ResultSet) a[0];
			try {

				if (rs.next()) {
					final Object id = rs.getObject(1);
					final ZEntity zEntity = t.getClass().getAnnotation(ZEntity.class);

					// XXX 这个sql就这样写了，因为在 findById0 里面已经把*替换为具体column了
					// FIXME 2024年5月27日 下午2:58:04 zhangzhen: 这个sql写死了，我想改@ZID字段测试，结果ZR中的方法模板都提前规定了必须包含Id，
					// 改起来改动太多了，那就这样：@ZID字段名称必须是id，不允许为其他？表中必须有id字段并且必须是主键？
					final String selectById = MethodRegex.SELECT + " * " + MethodRegex.FROM + " " + zEntity.tableName()
					+ " " + MethodRegex.WHERE + " id = ?";
					return findById0(zrSubClassName, callerMethodName, zc.getZConnection().getDbEnum(), mode, id, entityTName, selectById, zc.getZConnection());
				}
			} finally {
				close((AutoCloseable)a[0],(AutoCloseable)a[1]);
			}

		} catch (final SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return null;
	}

	private static <T> Object[] save0(final String zrSubClassName, final String callerMethodName, final DBEnum dbEunum, final Class<T> entityClass,
			final Object t, final String sql, final Connection connection, final ZC2 zc2) throws SQLException {

		final StringJoiner arg = new StringJoiner(",");
		final Field[] fs = entityClass.getDeclaredFields();
		int fieldCount = 0;
		for (final Field field : fs) {
			if (field.isAnnotationPresent(ZTransient.class) || (field.isAnnotationPresent(ZID.class)
					&& (field.getAnnotation(ZID.class).strategy() == ZGenerationType.IDENTITY))) {
				continue;
			}
			fieldCount++;
			final String dbFieldname = ZFieldConverter.toDbField(field.getName());
			arg.add(dbFieldname);
		}

		final StringJoiner joiner = new StringJoiner(",");
		for (int i = 1; i <= fieldCount; i++) {
			joiner.add("?");
		}

		final String sql2 = sql.replace(MethodRegex.COLUMNS, arg.toString()).replace(MethodRegex.COLUMN_VALUES,
				joiner.toString());

		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.SAVE);
		final SUA sua = new SUA(entityClass, t, entityClass, sql2, null);
		sua.setZc2(zc2);
		sh.forEach(h -> h.handle(sua));

		PreparedStatement ps;
		if (isShowSQL(getDataSourceNameFromClassType(entityClass))) {
			// FIXME 2024年11月26日 下午7:07:04 zhangzhen : SU.save 已处理了byte[]的log了，其他的也记得改掉
			final Object hTBlob = hTBlob(entityClass, t);
			LOG.info("[{}.{}：{},[{}]]",zrSubClassName,callerMethodName, sql2,hTBlob);
		}

		ps = connection.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
		int i = 0;
		for (final Field field : fs) {
			if (field.isAnnotationPresent(ZTransient.class) || (field.isAnnotationPresent(ZID.class)
					&& (field.getAnnotation(ZID.class).strategy() == ZGenerationType.IDENTITY))) {
				continue;
			}

			i++;
			addPS(dbEunum, t, ps, i, field, SUMode.SAVE);
		}
		final int executeUpdate = ps.executeUpdate();
		final ResultSet rs = ps.getGeneratedKeys();
		return new Object[] {rs,ps};
	}


	private static <T> boolean addPS(final DBEnum dbEnum, final Object t, final PreparedStatement ps, final int i, final Field field, final SUMode mode)
			throws SQLException {

		try {

			field.setAccessible(true);
			final Object v2 = field.get(t);
			if (v2 == null) {
				ps.setObject(i, null);
				return false;
			}

			final String fn = field.getType().getName();

			// FIXME 2024年5月3日 下午9:51:23 zhangzhen: 各种类型，考虑好要不要特殊处理，继续测试
			if (fn.equals(Boolean.class.getName())) {
				// XXX sqlite也暂时Boolean和tinyint 对应，和mysql一样
				if ((dbEnum == DBEnum.MYSQL) || (dbEnum == DBEnum.SQLITE)) {
					final boolean equals = Boolean.TRUE.equals(v2);
					final byte vb = (byte) (equals ? 1 : 0);
					ps.setByte(i, vb);
				} else if (dbEnum == DBEnum.POSTGRESQL) {
					ps.setBoolean(i, Boolean.parseBoolean(String.valueOf(v2)));
				}
			} else if (fn.equals(Character.class.getName())) {
				// char 类型直接用String
				ps.setString(i, String.valueOf(v2));
			} else if (fn.equals(Byte.class.getName())) {
				ps.setByte(i, (Byte) v2);
			} else if (fn.equals(Short.class.getName())) {
				ps.setShort(i, (Short) v2);
			} else if (fn.equals(Integer.class.getName())) {
				ps.setInt(i, (Integer) v2);
			} else if (fn.equals(Long.class.getName())) {
				ps.setLong(i, (Long) v2);
			} else if (fn.equals(Double.class.getName())) {
				ps.setDouble(i, (Double) v2);
			} else if (fn.equals(String.class.getName())) {
				ps.setString(i, String.valueOf(v2));
			} else if (fn.equals(BigDecimal.class.getName())) {
				ps.setBigDecimal(i, (BigDecimal) v2);
			} else if (v2.getClass().isArray()) {
				// blob类型
				// FIXME 2024年5月5日 下午9:14:57 zhangzhen: saveAll
				// 时，setBlob和setBinaryStream都会导致ps.excuteBatch NPE,所有在此用setObject
				ps.setObject(i, v2);
				// final ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[])
				// v2);
				// ps.setBlob(i, inputStream);
				// ps.setBinaryStream(i, inputStream);
			} else if (fn.equals(java.util.Date.class.getName())) {
				// FIXME 2023年8月1日 下午8:50:26 zhanghen: TODO
				// 日期时间的字段，新增注解：表示插入的格式
				// ps.setDate(i, new java.sql.Date(((Date) v2).getTime()));
				// FIXME 2024年5月19日 下午9:23:37 zhangzhen: 考虑好sql.date 要不要对应DATE
				ps.setTimestamp(i, new java.sql.Timestamp(((Date) v2).getTime()));
			} else if (fn.equals(java.sql.Date.class.getName())) {
				ps.setDate(i, (java.sql.Date) v2);
			} else if (fn.equals(java.sql.Time.class.getName())) {
				// FIXME 2025年1月8日 下午8:46:34 zhangzhen : 使用(java.sql.Time) v 会导致取出来的结果不一致？以后再查什么原因
				ps.setTime(i, Time.valueOf(v2.toString()));
			} else if (fn.equals(LocalTime.class.getName())) {
				ps.setObject(i, v2);
			} else if (fn.equals(LocalDate.class.getName())) {
				ps.setDate(i, java.sql.Date.valueOf((LocalDate) v2));
			} else if (fn.equals(LocalDateTime.class.getName())) {
				final LocalDateTime localDateTime = (LocalDateTime) v2;
				final ZonedDateTime atZone = localDateTime.atZone(ZoneId.systemDefault());
				final Timestamp timestamp = Timestamp.from(atZone.toInstant());
				ps.setTimestamp(i, timestamp);
			} else if (fn.equals(Timestamp.class.getName())) {
				ps.setTimestamp(i, (Timestamp) v2);
			} else {
				// FIXME 2024年5月4日 下午2:41:05 zhangzhen: TODO
				// 暂时只支持上面这些类型，在程序启动时就校验字段类型是否支持，而不是在此提示，在此提示太晚了（程序已经开始运行了）
				// throw new IllegalArgumentException("size 必须大于0！size = " + size);
			}

			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return false;
	}

	private static ZC2 getZCAndSetAutoCommitFALSEIfPG(final Mode mode, final String dataSourceName) {
		final ZC2 zcT = ZTransactionAOP.getCurrentZConnection();
		if (zcT != null) {
			return zcT;
		}

		// XXX 注意：这里为了兼容pg，处理为先setAC true再false
		// 否则会在一个并发deleteAll的单元测试中会出现pg死锁的情况，其他还会有什么问题暂未发现
		// 用jprofiler监测发现此处的setAC 非常耗时，如 5600G 64G的机器上的mysql findById 去除这两个setAC前后qps从8千变为2万
		// mysql和sqlite不用这两个setAC暂没发现问题，所以就先这么处理了：pg时先setAC true再false
		// 并且截止现在  2025年2月4日 下午9:49:59 sb_zrepository_test 里的265个单元测试，处理pg处理 java.util.Date
		// new 一个存入pg再取出和new的equals是false以外，其他的都通过测试了.
		final ZConnection zConnection = ZCPool.getInstance(dataSourceName).getZConnection(mode);
		if (zConnection.getDbEnum() == DBEnum.POSTGRESQL) {
			zConnection.setAutoCommitTrue();
			zConnection.setAutoCommitFalse();
		}

		final ZC2 zc2 = new ZC2(zConnection, ZCSourceEnum.ZCPOOL);
		ZTransactionAOP.resetToDefaultTransactionIsolation(zc2);

		return zc2;
	}

	public static List find(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class entityClass,final Class returnType, final String sql, final Object wrapper) {


		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, entityClass);
			final String where = wrapper == null ? ZRWrapper.ALWAYS_TRUE : ((ZRWrapper) wrapper).done().getFullWhere();
			final String x = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select) + " "
					+ MethodRegex.WHERE + " " + where;

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, x, null, zc);

			final String x2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}]",zrSubClassName,callerMethodName, x2);
			}

			ps = connection.prepareStatement(x2);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), entityClass, rs, metaData, count);
				r.add(t);
			}
			return r;

		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findAll(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final String sql) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);
		final String s = sua.getSql();

		final String select = gSelectFromReturnType(entityClass, entityClass);
		final String sF = s.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

		PreparedStatement ps = null;
		ResultSet rs = null;

		if (isShowSQL(dataSourceName)) {
			LOG.info("[{}.{}：{}]",zrSubClassName,callerMethodName, sF);
		}

		try {

			ps = connection.prepareStatement(sF);
			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), entityClass, rs, metaData, count);
				r.add(t);
			}
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
			return r;

		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	private static <T> String getDataSourceNameFromClassType(final Class<T> cls) {
		final ZEntity en = cls.getAnnotation(ZEntity.class);
		return en.dataSourceName();
	}


	public static <T> List<T> findByIdIn(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final List<Object> idList, final Class<T> entityClass, final String sql) {

		if ((idList == null) || idList.isEmpty()) {
			return Collections.emptyList();
		}

		return findByIdIn1(zrSubClassName, callerMethodName, mode, idList, entityClass, sql);
	}

	private static <T> List<T> findByIdIn1(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final List<Object> idList, final Class<T> entityClass, final String sql) {
		final String cachekey =
				zrSubClassName + "@"
						+ callerMethodName + "@"
						+ mode + "@"
						+ entityClass.getName() + "@"
						+ sql + "@"
						+ idList.get(0).getClass().getName() + "@"
						+ idList;

		final ZC2 zc2 = getZCAndSetAutoCommitFALSEIfPG(mode, getDataSourceNameFromClassType(entityClass));
		//				final ZC2 zc2 = getZC(mode, getDataSourceNameFromClassType(entityClass));

		final Supplier<List<T>> supplier = (Supplier<List<T>>) () -> findByIdIn0(zrSubClassName, callerMethodName, mode, idList, entityClass, sql, zc2);

		return (List<T>) selectFromCacheIfZT(cachekey, zc2, supplier);
	}

	private static <T> List<T> findByIdIn0(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final List<Object> idList, final Class<T> entityClass, final String sql, final ZC2 zc2) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final Set<Object> idSet = new HashSet<>(idList);

		final Date invokeTime = new Date();

		final Connection connection = zc2.getZConnection().getConnection();

		final long start = System.currentTimeMillis();

		final String select = gSelectFromReturnType(entityClass, entityClass);
		final String sql2 = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sql2, null, zc2);
		final String s = sua.getSql();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final StringJoiner joiner = new StringJoiner(",");
			for (final Object id : idSet) {
				joiner.add(String.valueOf(id));
			}
			final String param = joiner.toString();
			final String s2 = s.replace("?", param);
			ps = connection.prepareStatement(s2);


			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[共{}个条件]",zrSubClassName,callerMethodName, s,idSet.size());
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List rList = new ArrayList<>(idSet.size());
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				rList.add(newT(zc2.getZConnection().getDbEnum(), entityClass, rs, metaData, count));
			}

			saveSQLInvokeTime(zrSubClassName, callerMethodName, invokeTime, start, s2,
					entityClass.getAnnotation(ZEntity.class).tableName(), param);

			return rList;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();

			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc2);
		}

		return Collections.emptyList();
	}

	// FIXME 2024年6月8日 下午9:15:32 zhangzhen : 各个方法都加入
	private static void saveSQLInvokeTime(final String zrSubClassName, final String callerMethodName,
			final Date invokeTime, final long start, final String sql, final String tableName, final Object... value) {
		final SqlInvocationLogsConfigurationProperties cp = ZContext
				.getBean(SqlInvocationLogsConfigurationProperties.class);
		if ((cp != null) && cp.getEnable()) {
			final SqlInvocationLogsEntity entity = new SqlInvocationLogsEntity();
			entity.setTimeConsuming((int) (System.currentTimeMillis() - start));
			entity.setZrSubClassName(zrSubClassName);
			entity.setMethodName(callerMethodName);
			entity.setInvokeTime(invokeTime);
			entity.setSql(sql);
			entity.setTableName(tableName);
			if (value != null) {
				if (value.length == 1) {
					entity.setValue(String.valueOf(value[0]));
				} else {
					final StringJoiner vj = new StringJoiner(",");
					for (final Object v : value) {
						vj.add(String.valueOf(v));
					}
					entity.setValue(vj.toString());
				}
			}

			final String beanName = SqlInvocationLogsService.class.getName() + "@" + "service";
			// FIXME 2024年6月8日 下午8:14:06 zhangzhen : 这行改为异步的
			final SqlInvocationLogsService service = (SqlInvocationLogsService) ZContext.getBean(beanName);
			service.add(entity);
		}
	}

	private static <T> T findById0(final String zrSubClassName, final String callerMethodName, final DBEnum dbEnum, final Mode mode,
			final Object id, final Class entityClass, final String sql, final ZConnection zc) {

		final String select = gSelectFromReturnType(entityClass, entityClass);
		final String sF = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			if (isShowSQL(getDataSourceNameFromClassType(entityClass))) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, sF, id);
			}

			ps = zc.getConnection().prepareStatement(sF);
			final int index = 1;
			ps.setObject(index, id);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			if (rs.next()) {
				final int count = metaData.getColumnCount();
				return (T) newT(dbEnum, entityClass, rs, metaData, count);
			}

		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				zc.getConnection().rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
		}

		return null;
	}

	/**
	 * 如果本[SELECT]操作的连接对象是由 @ZTransaction 控制的，
	 * 则优先根据事务ID从缓存取，取不到再执行目标方法并且把结果放入缓存。
	 * 即：实现事务内[SELECT]操作的缓存
	 * @param cachekey
	 * @param zc2
	 * @param supplier
	 *
	 * @return
	 */
	private static Object selectFromCacheIfZT(final String cachekey, final ZC2 zc2, final Supplier supplier) {
		if(zc2.getSourceEnum() != ZCSourceEnum.ZTRANSACTION) {
			return supplier.get();
		}

		final DBEnum dbEnum = zc2.getZConnection().getDbEnum();

		// SQLITE 不管什么隔离级别都不使用事务内缓存
		if (dbEnum == DBEnum.SQLITE) {
			return supplier.get();
		}

		final ZIsolationEnum isolationEnum = zc2.getIsolationEnum();

		// MYSQL和PGSQL在 读未提交/读已提交 的隔离级别下，不使用事务内缓存
		// 只有在 可重复读/串行化 的级别时才使用
		if (((dbEnum == DBEnum.MYSQL) || (dbEnum == DBEnum.POSTGRESQL))
				&& ((isolationEnum != ZIsolationEnum.SERIALIZABLE)
						&& (isolationEnum != ZIsolationEnum.REPEATABLE_READ))) {

			return supplier.get();
		}

		final String transactionId = zc2.getTransactionId();
		final String key = zc2.getSourceEnum() + "@" + zc2.getZConnection().getConnection().hashCode() + "@"
				+ transactionId + "@" + cachekey;

		zc2.addKey(key);

		synchronized (key) {
			return ZRC.singleton().computeIfAbsent(key, supplier, true);
		}
	}

	public static Optional<Object> findOptionalById(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Object id, final Class entityClass, final String sql) {

		if (id == null) {
			return Optional.empty();
		}

		final Object v = findById1(zrSubClassName, callerMethodName, mode, id, entityClass, sql);
		return Optional.ofNullable(v);
	}

	public static Object findById(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Object id, final Class entityClass, final String sql) {

		if (id == null) {
			return null;
		}

		return findById1(zrSubClassName, callerMethodName, mode, id, entityClass, sql);
	}

	private static Object findById1(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Object id, final Class entityClass, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final Date invokeTime = new Date();
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);

		final Supplier<Object> supplier = () -> {
			final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);
			final String sql2 = sua.getSql();

			try {
				final Object t = findById0(zrSubClassName, callerMethodName, zc.getZConnection().getDbEnum(), mode, id,
						entityClass, sql2, zc.getZConnection());
				saveSQLInvokeTime(zrSubClassName, callerMethodName, invokeTime, invokeTime.getTime(), sql,
						((ZEntity) entityClass.getAnnotation(ZEntity.class)).tableName(), id);
				return t;
			} finally {
				returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
			}
		};

		final String cachekey =
				zrSubClassName + "@"
						+ callerMethodName + "@"
						+ mode + "@"
						+ entityClass.getName() + "@"
						+ sql + "@"
						+ id.getClass().getName() + "@"
						+ id;

		return selectFromCacheIfZT(cachekey, zc, supplier);
	}

	private static <T> SUA excludedDeletedHandler(final Class<T> entityClass, final Object entityObject, final Class returnClass,
			final String sql, final Object[] arg, final ZC2 zc2) {
		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.SELECT_EXCLUDED_DELETED);
		final SUA sua = new SUA(entityClass, entityObject, returnClass, sql, arg);
		sua.setZc2(zc2);

		sh.forEach(h -> ((ZAllHandler)h).handle(sua));
		return sua;
	}

	private static FI getTCInfo(final Class<?> returnType, final ResultSetMetaData metaData, final int fieldCount) {

		final Field[] fieldArray = new Field[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			try {
				final String columnName = metaData.getColumnLabel(i + 1).toLowerCase();
				final String javaFieldName = ZFieldConverter.toJavaField(columnName);
				Field field = null;
				try {
					field = returnType.getDeclaredField(javaFieldName);
					field.setAccessible(true);
				} catch (final SecurityException | NoSuchFieldException e1) {
					e1.printStackTrace();
				}

				fieldArray[i] = field;

			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		return new FI(fieldCount, null, fieldArray);
	}

	static class FI {
		private final int fieldCount;
		private final String[] columnNameArray;
		private final Field[] fieldArray;

		public int getFieldCount() {
			return this.fieldCount;
		}

		public String[] getColumnNameArray() {
			return this.columnNameArray;
		}

		public Field[] getFieldArray() {
			return this.fieldArray;
		}

		public FI(final int fieldCount, final String[] columnNameArray, final Field[] fieldArray) {
			this.fieldCount = fieldCount;
			this.columnNameArray = columnNameArray;
			this.fieldArray = fieldArray;
		}

	}

	private static Object newT2(final DBEnum dbEnum, final Class returnType, final ResultSet rs,
			final ResultSetMetaData metaData, final int fieldCount,final FI fi) {

		final Object object = RU.newInstance(returnType);

		for (int i = 0; i < fi.getFieldCount(); i++) {
			final Object columValue = getColumnValue(rs, i, fi.getFieldArray()[i]);
			if (columValue == null) {
				continue;
			}

			setFieldValue(dbEnum, object, fi.getFieldArray()[i], columValue);
		}

		return object;
	}

	/**
	 * @param dbEnum
	 * @param returnType
	 * @param rs
	 * @param resultSetMetaData
	 * @param fieldCount
	 * @return
	 */
	private static Object newT(
			final DBEnum dbEnum,
			final Class returnType,
			final ResultSet rs,
			final ResultSetMetaData resultSetMetaData,
			final int fieldCount) {

		final Object object = RU.newInstance(returnType);

		for (int i = 0; i < fieldCount; i++) {
			String columnName = null;
			try {
				// pgsql 对于 select max(id) as maxId
				// 读取到的 columnName 会是maxid ，导致匹配 returnType 中的字段时匹配不到，
				// 所以统一自定义SQL AS 后面的写法使用下划线命名法.只要是下划线命名法就行了，不关心大小写，
				// 在此统一为小写了
				columnName = resultSetMetaData.getColumnLabel(i + 1).toLowerCase();
			} catch (final SQLException e) {
				e.printStackTrace();
			}

			final String javaFieldName = ZFieldConverter.toJavaField(columnName);

			final Field field = ClassU.getField(returnType, javaFieldName);
			if (field == null) {
				// 到此就continue而非抛异常，因为SQL和returnType都可以是自定义的。
				// 在此sql中的column匹配不到returnType中的Field，就直接忽略就行了
				// 有可能是手误多写了一个column，或者少写了一个Field等等情况
				// 前者会导致多一点流量，后者导致逻辑不通会自己发现的
				continue;
			}

			final Object columValue = getColumnValue(rs, i, field);
			if (columValue == null) {
				continue;
			}

			setFieldValue(dbEnum, object, field, columValue);
		}

		return object;
	}

	private static <T> void setFieldValue(final DBEnum dbEnum, final T object, final Field field, final Object columValue) {
		if (columValue == null) {
			return;
		}

		final String fieldName = field.getType().getName();

		try {

			field.setAccessible(true);

			if (fieldName.equals(Byte.class.getName())) {
				field.set(object, Byte.valueOf(String.valueOf(columValue)));
			} else if (fieldName.equals(Short.class.getName())) {
				field.set(object, Short.valueOf(String.valueOf(columValue)));
			} else if (fieldName.equals(Integer.class.getName())) {
				field.set(object, Integer.valueOf(String.valueOf(columValue)));
			} else if (fieldName.equals(Long.class.getName())) {
				field.set(object, Long.valueOf(String.valueOf(columValue)));
			} else if (fieldName.equals(Double.class.getName())) {
				field.set(object, Double.valueOf(String.valueOf(columValue)));
			} else if (fieldName.equals(BigDecimal.class.getName())) {
				field.set(object, new BigDecimal(String.valueOf(columValue)));
			} else if (fieldName.equals(Boolean.class.getName())) {
				field.set(object, (Integer.valueOf(1).equals(columValue) ? Boolean.TRUE : Boolean.FALSE));
			} else if (fieldName.equals(Character.class.getName())) {
				field.set(object, Character.valueOf(String.valueOf(columValue).charAt(0)));
			} else if (fieldName.equals(String.class.getName())) {
				field.set(object, String.valueOf(columValue));
			} else if (fieldName.equals(java.util.Date.class.getName())) {
				if ((dbEnum == DBEnum.SQLITE) && (columValue.getClass() == Long.class)) {
					// sqlite 中此值为long类型
					final java.util.Date date = new Date((long) columValue);
					field.set(object, date);
				} else {
					// FIXME 2024年6月14日 下午8:58:41 zhangzhen : mysql datetime 对应java LocalDateTime
					final boolean equals = columValue.getClass().equals(LocalDateTime.class);
					if (equals) {
						final Date newDate = Date
								.from(((LocalDateTime) (columValue)).atZone(ZoneId.systemDefault()).toInstant());
						field.set(object, newDate);
					} else {
						field.set(object, columValue);
					}
				}
			} else if (fieldName.equals(java.sql.Date.class.getName())) {
				if ((dbEnum == DBEnum.SQLITE) && (columValue.getClass() == Long.class)) {
					// sqlite 中此值为long类型
					final java.sql.Date date = new java.sql.Date((long) columValue);
					field.set(object, date);
				} else {
					field.set(object, columValue);
				}
			} else if (fieldName.equals(java.sql.Timestamp.class.getName())) {
				final DBEnum db = dbEnum;
				if (columValue.getClass().equals(Timestamp.class)) {
					field.set(object, columValue);
				} else if (columValue.getClass() == Long.class) {
					// sqlite 中此值为long类型
					final Timestamp time = new Timestamp((long) columValue);
					field.set(object, time);
				}
			}  else if (fieldName.equals(LocalDate.class.getName())) {
				if (columValue.getClass() == Long.class) {
					// sqlite 为Long类型
					final Instant instant = Instant.ofEpochMilli((Long) columValue);
					final LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
					field.set(object, localDate);
				} else {
					final java.sql.Date d = (java.sql.Date) columValue;
					final LocalDate localDate = d.toLocalDate();
					field.set(object, localDate);
				}
			}  else if (fieldName.equals(LocalTime.class.getName())) {
				if (columValue.getClass().equals(Time.class)) {
					final Time t1 = (Time) columValue;
					final Calendar c = Calendar.getInstance();
					c.clear();
					c.setTimeInMillis(t1.getTime());

					final LocalTime of = LocalTime.of(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
							c.get(Calendar.SECOND));
					field.set(object, of);
				} else if (columValue.getClass() == LocalTime.class) {
					field.set(object, columValue);
				} else if (columValue.getClass() == Integer.class) {

					final Time time = new Time((int) columValue);
					final Calendar c = Calendar.getInstance();
					c.clear();
					c.setTimeInMillis(time.getTime());

					final LocalTime of = LocalTime.of(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
							c.get(Calendar.SECOND));
					field.set(object, of);

				} else if (columValue.getClass() == Long.class) {
					// sqlite 中此值为long类型
					final Time time = new Time((long) columValue);
					field.set(object, time);
				}

			} else if (fieldName.equals(java.sql.Time.class.getName())) {
				final DBEnum db = dbEnum;
				if (columValue.getClass().equals(Time.class)) {
					field.set(object, columValue);
				} else if (columValue.getClass() == Integer.class) {
					final Time time = new Time((int) columValue);
					field.set(object, time);
				} else if (columValue.getClass() == Long.class) {
					// sqlite 中此值为long类型
					final Time time = new Time((long) columValue);
					field.set(object, time);
				}
			} else if (fieldName.equals(java.time.LocalDateTime.class.getName())) {
				if (columValue.getClass() == Long.class) {
					// sqlite 中此值为long类型
					final Instant ins = Instant.ofEpochMilli((long) columValue);
					final LocalDateTime localDateTime = ins.atZone(ZoneId.systemDefault()).toLocalDateTime();
					field.set(object, localDateTime);
				} else {
					final Timestamp timestamp = (Timestamp) columValue;
					final LocalDateTime localDateTime = timestamp.toLocalDateTime();
					field.set(object, localDateTime);
				}
			} else if (field.getClass().isArray()) {
			} else {
				field.set(object, columValue);
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static Object getColumnValue(final ResultSet rs, final int i, final Field field) {

		final Class<?> type = field.getType();
		if ((type == LocalTime.class)
		|| (type == String.class)
		|| (type == Time.class)) {
			try {
				return rs.getObject(i + 1, type);
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			return rs.getObject(i + 1);
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static <T> List<T> findByXXAndXXAndXXLikeAndXXLikeAndXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql,
			final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
								+ fieldArray[1] +','
								+ "%" + fieldArray[2] + "%,"
								+ "%" + fieldArray[3] + "%,"
								+ "%" + fieldArray[4] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue(fieldArray[1], ps, 2);
			setXX_fieldValue("%" + fieldArray[2] + "%", ps, 3);
			setXX_fieldValue("%" + fieldArray[3] + "%", ps, 4);
			setXX_fieldValue("%" + fieldArray[4] + "%", ps, 5);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXAndXXAndXXLikeAndXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql,
			final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
								+ fieldArray[1] +','
								+ fieldArray[2] + ","
								+ "%" + fieldArray[3] + "%,"
								+ "%" + fieldArray[4] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue(fieldArray[1], ps, 2);
			setXX_fieldValue(fieldArray[2], ps, 3);
			setXX_fieldValue("%" + fieldArray[3] + "%", ps, 4);
			setXX_fieldValue("%" + fieldArray[4] + "%", ps, 5);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXAndXXAndXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql,
			final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
								+ fieldArray[1] +','
								+ fieldArray[2] + ","
								+ "%" + fieldArray[3] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue(fieldArray[1], ps, 2);
			setXX_fieldValue(fieldArray[2], ps, 3);
			setXX_fieldValue("%" + fieldArray[3] + "%", ps, 4);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXAndXXLikeAndXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql,
			final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
								+ fieldArray[1] +','
								+ "%" + fieldArray[2] + "%,"
								+ "%" + fieldArray[3] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue(fieldArray[1], ps, 2);
			setXX_fieldValue("%" + fieldArray[2] + "%", ps, 3);
			setXX_fieldValue("%" + fieldArray[3] + "%", ps, 4);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXAndXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql,
			final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
								+ fieldArray[1] +','
								+ "%" + fieldArray[2] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue(fieldArray[1], ps, 2);
			setXX_fieldValue("%" + fieldArray[2] + "%", ps, 3);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXX(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql, final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2, Arrays.toString(fieldArray));
			}

			ps = connection.prepareStatement(s2);

			int i = 1;
			for (final Object object : fieldArray) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static String gSelectFromReturnType(final Class entityClass, final Class returnType) {
		final Supplier<String> supplier = () -> gSelectFromReturnType0(entityClass, returnType);
		final String key = "gSelectFromReturnType" + entityClass.getComponentType() +"@" + returnType.getName();
		return ZRC.singleton().computeIfAbsent(key, supplier);
	}

	private static String gSelectFromReturnType0(final Class entityClass, final Class returnType) {
		final Field[] declaredFields = returnType.getDeclaredFields();

		final Field[] efs = entityClass.getDeclaredFields();
		final StringJoiner joiner = new StringJoiner(ZRepositoryMain.DELIMITER);
		for (final Field f : declaredFields) {

			if (f.isAnnotationPresent(ZTransient.class)) {
				continue;
			}

			final Optional<Field> findAny = Arrays.stream(efs).filter(ef -> ef.getName().equals(f.getName())).findAny();
			if (!findAny.isPresent()) {
				continue;
			}

			final String javaFieldName = f.getName();
			final String dbColumnName = ZFieldConverter.toDbField(javaFieldName);
			joiner.add(dbColumnName);
		}

		return joiner.toString();
	}

	public static <T> List<T> findByXXAndXXLikeAndXXLikeAndXXLike(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldArray) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
								+ "%" + fieldArray[1] + "%,"
								+ "%" + fieldArray[2] + "%,"
								+ "%" + fieldArray[3] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue("%" + fieldArray[1] + "%", ps, 2);
			setXX_fieldValue("%" + fieldArray[2] + "%", ps, 3);
			setXX_fieldValue("%" + fieldArray[3] + "%", ps, 4);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXLikeAndXXLike(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldArray) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + ","
				+ "%" + fieldArray[1] + "%,"
				+ "%" + fieldArray[2] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue("%" + fieldArray[1] + "%", ps, 2);
			setXX_fieldValue("%" + fieldArray[2] + "%", ps, 3);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXLike(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] + "," + "%" + fieldArray[1] + "%"
						);
			}

			ps = connection.prepareStatement(s2);

			setXX_fieldValue(fieldArray[0], ps, 1);
			setXX_fieldValue("%" + fieldArray[1] + "%", ps, 2);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXX(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql, final Object fieldValue) {

		final ZC2 zc2 = getZCAndSetAutoCommitFALSEIfPG(mode, getDataSourceNameFromClassType(entityClass));

		// fieldValue 为null，没法使用缓存，因为不确定其类型，因为这个XX可以是任何字段任何类型
		if (fieldValue == null) {
			return findByXX0(zrSubClassName, callerMethodName, entityClass, returnType, sql, fieldValue, zc2);
		}

		final Supplier<List<T>> supplier = () -> findByXX0(zrSubClassName, callerMethodName, entityClass, returnType, sql, fieldValue, zc2);

		final String cachekey =
				zrSubClassName + "@"
						+ callerMethodName + "@"
						+ mode + "@"
						+ entityClass.getName() + "@"
						+ returnType.getName() + "@"
						+ sql + "@"
						+ fieldValue.getClass().getName() + "@"
						+ fieldValue;

		return (List<T>) selectFromCacheIfZT(cachekey, zc2, supplier);
	}

	private static <T> List<T> findByXX0(final String zrSubClassName, final String callerMethodName, final Class<T> entityClass,
			final Class<T> returnType, final String sql, final Object fieldValue, final ZC2 zc) {
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String x = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, x, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(getDataSourceNameFromClassType(entityClass))) {
				// FIXME 2024年11月26日 上午1:30:22 zhangzhen : 所有的日志信息，都要先判断是否byte[]，如下这个就没判断
				// 是否只显示"二进制内容"？因为打印它可能特别长，记得所有的T t参数的方法都要改
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, fieldValue);
			}

			ps = connection.prepareStatement(s);
			final int index = 1;
			setXX_fieldValue(fieldValue, ps, index);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(getDataSourceNameFromClassType(entityClass), zc);
		}

		return Collections.emptyList();
	}

	private static void setXX_fieldValue(final Object fieldValue, final PreparedStatement ps, final int index) throws SQLException {
		if (fieldValue == null) {
			ps.setObject(index, null);
			return;
		}

		if (fieldValue.getClass().equals(Character.class)) {
			// XXX 测试发现，char类型，setObject不行，还是用setString吧。其他类型如果不出错就仍然setObject吧
			ps.setString(index, String.valueOf(String.valueOf(fieldValue).charAt(0)));
		} else if(fieldValue.getClass().equals(java.util.Date.class)){
			ps.setTimestamp(index, new java.sql.Timestamp(((Date) fieldValue).getTime()));
		} else if(fieldValue.getClass().equals(java.sql.Date.class)){
			ps.setDate(index, (java.sql.Date)fieldValue);
		} else if(fieldValue.getClass().equals(java.sql.Time.class)){
			ps.setTime(index, (java.sql.Time)fieldValue);
		} else if(fieldValue.getClass().equals(java.time.LocalDateTime.class)){
			final LocalDateTime localDateTime = (LocalDateTime) fieldValue;
			final ZonedDateTime atZone = localDateTime.atZone(ZoneId.systemDefault());
			final Timestamp timestamp = Timestamp.from(atZone.toInstant());
			ps.setTimestamp(index, timestamp);
		} else if(fieldValue.getClass().equals(java.time.LocalDate.class)){
			ps.setDate(index, java.sql.Date.valueOf((LocalDate) fieldValue));
		} else {
			if(fieldValue.getClass().equals(java.time.LocalTime.class)){
			}
			ps.setObject(index, fieldValue);
		}
	}

	public static <T> List<T> findByXXIn(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,
			final String sql, final Object value, final String fieldName) {

		if (value == null) {
			return Collections.emptyList();
		}

		// FIXME 2024年7月16日 下午7:22:05 zhangzhen : 这个判断要待定，是否就是查询null的

		final ArrayList<Object> arrayList = new ArrayList<>();
		final Iterable iterable = (Iterable) value;
		for (final Object object : iterable) {
			arrayList.add(object);
		}

		final Set set = (arrayList).stream().filter(e -> e != null)
				.collect(Collectors.toSet());
		if (set.isEmpty()) {
			return Collections.emptyList();
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		final StringJoiner w = new StringJoiner(",");
		for (int i = 1; i <= set.size(); i++) {
			w.add("?");
		}

		final String select = gSelectFromReturnType(entityClass, returnType);
		final String sqlN = sql.replace("?", w.toString());


		final String sqlColumn = sqlN.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

		final SUA sua = excludedDeletedHandler(entityClass, select, returnType, sqlColumn, null, zc);
		final String sF = sua.getSql();

		final DBEnum dbEnum = ZRepositoryMain.getDB(dataSourceName);

		if (isShowSQL(dataSourceName)) {
			LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, sF, set);
		}

		PreparedStatement ps  = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sF);
			int index = 1;
			for (final Object v : set) {
				setXX_fieldValue(v, ps, index);
				index++;
			}
			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				r.add(newT(zc.getZConnection().getDbEnum(), entityClass, rs, metaData, count));
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();

	}

	public static <T> List<T> findByIdLessThan(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql, final Object field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, select, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, field);
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, field);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				r.add(newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count));
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXXEndingWith(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass,final Class<T> returnType, final String sql, final Object field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, select, returnType, sqlColumn, null, zc);

			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, "%" + field);
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, "%" + field);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				r.add(newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count));
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXXStartingWith(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final Class<T> returnType,
			final String sql, final Object field) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, field + "%");
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, field + "%");

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXOrYY(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql, final Object... field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, field, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, Arrays.toString(field));
			}
			ps = connection.prepareStatement(s);

			int index = 1;
			for (final Object object : field) {
				setXX_fieldValue(object, ps, index);
				index++;
			}


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXBetween(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fiedlArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fiedlArray, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, Arrays.toString(fiedlArray));
			}

			ps = connection.prepareStatement(s);

			int index = 1;
			for (final Object f : fiedlArray) {

				setXX_fieldValue(f, ps, index);

				index++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	// XXX 不和findByXXIsNullAndXX 复用代码，最后一个参数不同，区分Object... 仅传一个值并且为数组的情况，有bug
	public static <T> List<T> findByXXIsNullAndXXAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldArray) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fieldArray, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, Arrays.toString(fieldArray));
			}

			ps = connection.prepareStatement(s);

			int i = 1;
			for (final Object object : fieldArray) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				r.add(newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count));
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXIsNullAndXXIsNullAndXXAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldValue) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fieldValue, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, Arrays.toString(fieldValue));
			}

			ps = connection.prepareStatement(s);

			int i = 1;
			for (final Object object : fieldValue) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXIsNullAndXXIsNullAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object fieldValue) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, fieldValue);
			}

			ps = connection.prepareStatement(s);

			final int i = 1;
			setXX_fieldValue(fieldValue, ps, i);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXIsNullAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();
			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, fieldValue);
			}

			ps = connection.prepareStatement(s);

			final int i = 1;
			setXX_fieldValue(fieldValue, ps, i);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXNotNull(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}]", zrSubClassName, callerMethodName, s);
			}

			ps = connection.prepareStatement(s);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXLikeAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldArray) {


		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s1 = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sss = excludedDeletedHandler(entityClass, null, returnType, s1, null, zc);
			final String s2 = sss.getSql();

			if (isShowSQL(dataSourceName)) {
				// FIXME 2024年11月27日 下午10:13:43 zhangzhen : 第一个参数要处理%XX%的形式
				//				Object[] a2 = Arrays.copyOfRange(fieldArray, 1, fieldArray.length-1);

				final StringJoiner builder = new StringJoiner(",");
				for (int i = 1; i < fieldArray.length; i++) {
					builder.add(String.valueOf(fieldArray[i]));
				}

				LOG.info("[{}.{}：{}],[{},{}]", zrSubClassName, callerMethodName, s2,
						fieldArray[0] == null ? "%%" : "%" + fieldArray[0] + "%",
								builder
						);
			}

			ps = connection.prepareStatement(s2);


			System.out.println("fieldArray = " + Arrays.toString(fieldArray));

			// FIXME 2024年11月27日 下午10:54:59 zhangzhen : 待定，所有的Like/notLike和字符串等值匹配的等，
			// 都考虑好是不管传值(null/""/正常传值)的情况都适用统一的模板如：where name like '%?%'
			// 还是考虑如果传值null 则修改sql为 name is null
			// 如果传值"", 则修改为 name = ''
			// 其他正常传值则不修改sql，正常执行name like '%?%'
			System.out.println("sql = " + sql);
			if (fieldArray[0] == null) {



			}



			// FIXME 2024年11月27日 下午10:29:03 zhangzhen : 所有Like的都要和下面一样先区分值是否null
			//			if (fieldArray[0] == null) {
			//				ps.setObject(1, "%%");
			//			} else {
			ps.setObject(1, "%" + fieldArray[0] + "%");
			//			}

			for (int x = 1; x < fieldArray.length; x++) {
				setXX_fieldValue(fieldArray[x], ps, x + 1);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();


	}

	public static <T> List<T> findByXXLikeAndXXLike(final String zrSubClassName,
			final String callerMethodName,final Mode mode, final Class<T> entityClass,
			final Class<T> returnType, final String sql,
			final Object... field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s1 = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sss = excludedDeletedHandler(entityClass, null, returnType, s1, null, zc);
			final String s2 = sss.getSql();

			if (isShowSQL(dataSourceName)) {
				final StringJoiner joiner = new StringJoiner("%,%", "%", "%");
				for (final Object fName : field) {
					joiner.add(String.valueOf(fName));
				}
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2, joiner);
			}

			ps = connection.prepareStatement(s2);
			for (int i = 0; i < field.length; i++) {
				ps.setObject(i + 1, "%" + field[i] + "%");
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass,final Class<T> returnType, final String sql, final Object field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s1 = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sss = excludedDeletedHandler(entityClass, null, returnType, s1, null, zc);
			final String s2 = sss.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2, "%" + field + "%");
			}

			ps = connection.prepareStatement(s2);
			ps.setObject(1, "%" + field + "%");


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	/**
	 * ZConnection 来自于ZCPool的归还连接并提交;
	 * 来自SpringAOP的就不管了，而是由AOP类在目标方法执行结束后统一commit/rollback
	 *
	 * @param dataSourceName
	 * @param zc
	 */
	private static void returnZConnectionAndCommitIfZCPool(final String dataSourceName, final ZC2 zc) {
		if (zc.getSourceEnum() == ZCSourceEnum.ZCPOOL) {
			ZCPool.getInstance(dataSourceName).returnZConnectionAndCommit(zc.getZConnection());
		}
	}

	/**
	 * ZConnection 来自于ZCPool的归还连接;
	 * 来自SpringAOP的就不管了
	 * @param dataSourceName
	 * @param zc
	 */
	private static void returnZConnectionIfZCPool(final String dataSourceName, final ZC2 zc) {
		if (zc.getSourceEnum() == ZCSourceEnum.ZCPOOL) {
			ZCPool.getInstance(dataSourceName).returnZConnection(zc.getZConnection());
		}
	}

	private static void returnZConnectionAndCommit(final String dataSourceName, final ZConnection zc) {
		ZCPool.getInstance(dataSourceName).returnZConnectionAndCommit(zc);
	}

	public static <T> List<T> findByXXIsEmptyAndXXAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldValueArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, Arrays.toString(fieldValueArray));
			}

			ps = connection.prepareStatement(s);

			int i = 1;
			for (final Object object : fieldValueArray) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				r.add(newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count));
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXIsEmptyAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, fieldValue);
			}

			ps = connection.prepareStatement(s);

			final int i = 1;
			setXX_fieldValue(fieldValue, ps, i);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}


	public static <T> List<T> findByXXIsNull(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass,final Class<T> returnType, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}]", zrSubClassName, callerMethodName, s);
			}

			ps = connection.prepareStatement(s);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	private static <T> Long count(final String zrSubClassName, final String callerMethodName, final Mode mode,final Class<T> entityClass, final String sql, final ZConnection zc, final String dataSourceName, final ZC2 zc2) {

		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;



		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sql, null, zc2);
		final String s = sua.getSql();

		try {

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}]", zrSubClassName, callerMethodName, s);
			}

			ps = connection.prepareStatement(s);
			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getLong(1);
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc2);
		}

		return 0L;
	}

	public static <T> Long count(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> cls, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(cls);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		return count(zrSubClassName, callerMethodName, mode,cls, sql, zc.getZConnection(), dataSourceName, zc);
	}

	// FIXME 2024年5月18日 下午3:30:32 zhangzhen:  countingByXXAndXX 多个条件的不能改为Object...然后复用 countingByXX，因为可能一个条件的条件为byte[]
	// 会被认为是Object... a 是一个byte[]，而不是a.length = 1 并且第一个值是byte[].
	public static <T> Long countingByXXAndXX(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final String sql, final Object... fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);

			final String sqlF = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, sqlF, Arrays.toString(fieldValue));
			}
			ps = connection.prepareStatement(sqlF);

			int index = 1;
			for (final Object v : fieldValue) {
				setXX_fieldValue(v, ps, index);
				index++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			if (rs.next()) {
				return rs.getLong(1);
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return 0L;
	}

	public static <T> Long countingByXX(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final String sql, final Object fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);

			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, fieldValue);
			}
			ps = connection.prepareStatement(s);

			final int index = 1;
			setXX_fieldValue(fieldValue, ps, index);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			if (rs.next()) {
				return rs.getLong(1);
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return 0L;
	}

	public static <T> List<T> findByXXAndXXLikeAndXXLikeOrderByXXLimit(final String zrSubClassName,
			final String callerMethodName, final Mode mode, final Class<T> entityClass,
			final Class<T> returnType, final String sql, final Object... fieldV) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fieldV, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s,
						fieldV[0] + ","
								+ "%" + fieldV[1] + "%,"
								+ "%" + fieldV[2] + "%,"
								+ fieldV[3] + ","
								+ fieldV[4]
						);
			}

			ps = connection.prepareStatement(s);

			setXX_fieldValue(fieldV[0], ps, 1);
			setXX_fieldValue("%" + fieldV[1] + "%", ps, 2);
			setXX_fieldValue("%" + fieldV[2] + "%", ps, 3);
			setXX_fieldValue(fieldV[3], ps, 4);
			setXX_fieldValue(fieldV[4], ps, 5);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new  ArrayList<>();

			final int count = metaData.getColumnCount();
			final FI tcInfo = getTCInfo(returnType, metaData, count);
			while (rs.next()) {
				final Object t = newT2(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count, tcInfo);
				r.add(t);
			}

			return r;

		} catch (SQLException | SecurityException  e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXAndXXLikeOrderByXXLimit(final String zrSubClassName,
			final String callerMethodName, final Mode mode, final Class<T> entityClass,
			final Class<T> returnType, final String sql, final Object... fieldV) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fieldV, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s,
						fieldV[0] + ","
						+ "%" + fieldV[1] + "%,"
						+ fieldV[2] + ","
						+ fieldV[3]
						);
			}

			ps = connection.prepareStatement(s);

			setXX_fieldValue(fieldV[0], ps, 1);
			setXX_fieldValue("%" + fieldV[1] + "%", ps, 2);
			setXX_fieldValue(fieldV[2], ps, 3);
			setXX_fieldValue(fieldV[3], ps, 4);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new  ArrayList<>();

			final int count = metaData.getColumnCount();
			final FI tcInfo = getTCInfo(returnType, metaData, count);
			while (rs.next()) {
				final Object t = newT2(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count, tcInfo);
				r.add(t);
			}

			return r;

		} catch (SQLException | SecurityException  e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXOrderByXXLimit(final String zrSubClassName, final String callerMethodName, final Mode mode, final Class<T> entityClass,
			final Class<T> returnType, final String sql, final Object... field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		//		final ZC2 zc = getZC(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, field, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s, Arrays.toString(field));
			}

			ps = connection.prepareStatement(s);
			int i = 1;
			for (final Object object : field) {
				setXX_fieldValue(object, ps, i);
				i++;
			}


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List r = new  ArrayList<>();

			final int count = metaData.getColumnCount();
			final FI tcInfo = getTCInfo(returnType, metaData, count);
			while (rs.next()) {
				final Object t = newT2(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count, tcInfo);
				r.add(t);
			}

			return r;

		} catch (SQLException | SecurityException  e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static void close(final AutoCloseable... autoCloseables) {
		for (final AutoCloseable autoCloseable : autoCloseables) {
			if (autoCloseable == null) {
				continue;
			}
			try {
				autoCloseable.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 给update方法生成 COLUMN 部分，不管t中字段是否null，都生成 column = ?的形式，在后续的ps.setXX时区分null
	 *
	 * @param <T>
	 * @param t
	 * @param fs
	 * @return
	 */
	private static <T> String gUpdateColumn(final Object t, final Field[] fs) {
		final StringBuilder column =new StringBuilder();
		for (int i = 0; i < fs.length; i++) {
			final Field f = fs[i];

			// 兼顾pgsql，@ZID字段不可以update
			if (f.isAnnotationPresent(ZTransient.class) || f.isAnnotationPresent(ZID.class)) {
				continue;
			}

			f.setAccessible(true);
			try {
				final Object value = f.get(t);
				final String dbName = ZFieldConverter.toDbField(f.getName());
				column.append(dbName).append('=').append('?');
				if (i < (fs.length - 1)) {
					column.append(',');
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return column.toString();
	}

	// FIXME 2024年7月2日 下午3:07:28 zhangzhen : where xx in 这种形式还不支持
	public static List zQuerySelect(final String zrSubClassName, final String callerMethodName, final Mode mode,
//	public static <T> List<T> zQuerySelect(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Object entityTClassName, final Object returnTypeClassName, final String sqleModeName,
			final String sqlT, final Object... arg) {

		if (STU.isEmpty(sqlT)) {
			throw new ZRepositoryException(zrSubClassName + "." + callerMethodName + " " + " SQL不能为空");
		}

		if (!sqlT.trim().toUpperCase().startsWith(MethodRegex.SELECT)) {
			throw new ZRepositoryException(zrSubClassName + "." + callerMethodName + " " + " 只允许SELECT语句");
		}

		final ZEntity ze = (ZEntity) ((Class)entityTClassName).getAnnotation(ZEntity.class);
		final String dataSourceName = ze.dataSourceName();

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final int argcount = StringUtils.countMatches(sqlT, '?');
			if (argcount != arg.length) {
				final String message = "@" + ZQuery.class.getName() + " 自定义SQL参数个数[" + argcount
						+ "]和方法传入的参数个数[" + arg.length + "]不匹配";
				throw new ZQuerySQLException(message);
			}

			final String regex = "\\?(\\d+)";
			final String sql = sqlT.replaceAll(regex, "?");

			// FIXME 2024年5月20日 上午10:51:25 zhangzhen: @ZQuery 自定义select 也处理为了select 字段，
			// 即使 sql= select * 。但这样有点不太好，不受用户控制了
			// 应该时用户写什么，就select什么。或者提供一个特殊占位符，比如 select @T from ，这个 @T就作为占位符
			// 如果select语句中出现了这个@T，才处理为 select 字段，否则就是用户写了select什么就select什么。

			final Class returnClass = (Class) returnTypeClassName;
			final String select = gSelectFromReturnType((Class) entityTClassName, returnClass);
			final String s2 =
					SQLEMode.GENERATE.name().equals(sqleModeName)
					? sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select)
							: sql;

			if (isShowSQL(dataSourceName)) {
				if (AU.isEmpty(arg)) {
					LOG.info("[{}.{}：{}]", zrSubClassName, callerMethodName, s2);
				} else {
					LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, s2, Arrays.toString(arg));
				}
			}

			ps = connection.prepareStatement(s2);

			final Pattern pattern = Pattern.compile(regex);
			final java.util.regex.Matcher matcher = pattern.matcher(sqlT);
			final int[] argOrderArray = new int[arg.length];
			int i = 0;
			boolean find = false;
			while (matcher.find()) {
				find = true;
				final String a = matcher.group(1);
				argOrderArray[i] = Integer.parseInt(a);
				i++;
			}

			if (arg != null) {
				int index = 1;
				for (final int element : argOrderArray) {
					// 要判断a0的类型
					// 1
					//					Object a0 = arg[element-1];
					//					ps.setObject(index, a0);

					// 2
					// FIXME 2024年7月19日 下午9:11:24 zhangzhen : 暂不支持in操作,connection.createArrayOf测试有问题
					setXX_fieldValue(arg[element-1], ps, index);

					index++;
				}
			}
			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List ra = new ArrayList<>();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final Object t = newT(zc.getZConnection().getDbEnum(), returnClass, rs, metaData, count);
				ra.add(t);
			}

			return ra;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(rs, ps);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static Integer zQueryUpdate(final String zrSubClassName, final String callerMethodName,final Mode mode, final Object entityTName ,final Object object, final String sqleModeName,
			final String sql, final Object... arg) {

		return updateOrDeleteOrInsert(zrSubClassName, callerMethodName, mode, entityTName, object, sql, SUEnum.UPDATE, arg);
	}

	private static int updateOrDeleteOrInsert(final String zrSubClassName, final String callerMethodName, final Mode mode, final Object entityTName, final Object object, final String sql, final SUEnum suEnum, final Object... arg) {

		final Class cls = (Class) object;

		final ZEntity ze = (ZEntity) ((Class)entityTName).getAnnotation(ZEntity.class);
		final String dataSourceName = ze.dataSourceName();

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		if (isShowSQL(dataSourceName)) {
			LOG.info("[{}.{}：{}],[{}]", zrSubClassName, callerMethodName, sql, Arrays.toString(arg));
		}

		PreparedStatement prepareStatement = null;
		try {
			prepareStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			if (arg != null) {
				int n = 1;
				for (final Object a1 : arg) {
					setXX_fieldValue(a1, prepareStatement, n);
					n++;
				}
			}
			final int executeUpdate = prepareStatement.executeUpdate();
			if (suEnum == SUEnum.INSERT) {
				final ResultSet rs = prepareStatement.getGeneratedKeys();
				if (rs.next()) {

					// FIXME 2025年9月22日 下午7:42:48 zhangzhen: 注意类型，转换为int
					final int int1 = rs.getInt(1);
					return int1;
//					return (int) rs.getObject(1);
				}
			}
			return executeUpdate;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(prepareStatement);
			returnZConnectionAndCommitIfZCPool(dataSourceName, zc);
		}

		return NO_DELETE_OR_DELETE;
	}

	public static int zQueryDelete(final String zrSubClassName, final String callerMethodName,final Mode mode, final Object entityTName, final Object object,
			final String sqleModeName, final String sql, final Object... arg) {

		return updateOrDeleteOrInsert(zrSubClassName, callerMethodName, mode, entityTName, object, sql, SUEnum.DELETE, arg);
	}

	public static int zQueryInsert(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Object entityTName, final Class cls, final String sqleModeName, final String sql, final Object... arg) {
		return updateOrDeleteOrInsert(zrSubClassName, callerMethodName, mode, entityTName, cls, sql, SUEnum.INSERT, arg);
	}

	private static Field getZID(final Class cls) {
		final Field[] fs = cls.getDeclaredFields();
		for (final Field f : fs) {
			if(f.isAnnotationPresent(ZID.class)) {
				return f;
			}
		}
		return null;
	}

	// FIXME 2024年6月2日 下午9:57:46 zhangzhen : 测试
	public static void createTable(final String dataSourceName, final String createTable) {

		final ZC2 zc = getZCAndSetAutoCommitFALSEIfPG(Mode.WRITE, dataSourceName);

		try {
			final PreparedStatement ps = zc.getZConnection().getConnection().prepareStatement(createTable);
			final int executeUpdate = ps.executeUpdate();
			LOG.info("创建表结果-executeUpdate=[{}],sql=[{}]", executeUpdate, createTable);

		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			// 这个不用判断是否来自springAOP，直接commit
			returnZConnectionAndCommit(dataSourceName, zc.getZConnection());
		}

	}

	/**
	 * 把T中的byte[]字段写死为[二进制内容](考虑是否改为配置项)
	 * 因为其可能很大，导致日志特别长
	 *
	 * @param <T>
	 * @param entityClass
	 * @param t
	 * @return
	 */
	private static <T> Object hTBlob(final Class<T> entityClass, final Object t) {
		final boolean e = entityClass.isAnnotationPresent(ZEntity.class);
		if (!e) {
			return t;
		}

		final Field[] fs = entityClass.getDeclaredFields();
		final StringJoiner joiner = new StringJoiner("");
		for (int i = 0; i < fs.length; i++) {
			final Field field = fs[i];
			joiner.add(field.getName()).add("=");

			final boolean array = field.getType().isArray();
			if (array) {
				try {

					field.setAccessible(true);
					final Object v = field.get(t);
					if(v !=null) {
						joiner.add("[二进制内容]");
					}
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				}

			} else {

				try {

					field.setAccessible(true);
					final Object v = field.get(t);
					joiner.add(String.valueOf(v));
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}

			if (i < (fs.length - 1)) {
				joiner.add(",");
			}

		}

		return joiner.toString();
	}

}
