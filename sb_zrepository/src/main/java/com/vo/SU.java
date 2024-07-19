package com.vo;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vo.actuator.SqlInvocationLogsConfigurationProperties;
import com.vo.actuator.SqlInvocationLogsEntity;
import com.vo.actuator.SqlInvocationLogsService;
import com.vo.anno.ZEntity;
import com.vo.anno.ZTransient;
import com.vo.conn.Mode;
import com.vo.conn.ZCPool;
import com.vo.conn.ZConnection;
import com.vo.conn.ZDatasourcePropertiesLoader;
import com.vo.core.Page;
import com.vo.core.Sort;
import com.vo.core.ZContext;
import com.vo.core.ZLog2;
import com.vo.exception.ZRepositoryException;
import com.vo.transaction.ZIsolationEnum;
import com.vo.transaction.ZTransactionAOP;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

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
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("page分页查询-[{}]", pageSqlFinal2);
				LOG.info("page总条数查询-[{}]", pageCountSql2);
			}

			ps = connection.prepareStatement(pageSqlFinal2);

			rs = ps.executeQuery();
			final ResultSetMetaData metaData = rs.getMetaData();

			final int count = metaData.getColumnCount();
			final List<T> rL = Lists.newArrayList();
			while (rs.next()) {
				final T tR = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
				rL.add(tR);
			}

			psc = connection.prepareStatement(pageCountSql2);
			pscRS = psc.executeQuery();
			pscRS.next();

			final Long countR = pscRS.getLong(1);
			final long pages = (countR.longValue() % size) == 0 ? countR.longValue() / size
					: (countR.longValue() / size) + 1;
			return new Page(size, Long.valueOf(String.valueOf(page)), pages, countR,
					ImmutableList.copyOf(rL));

		} catch (final SQLException | IllegalArgumentException  e1) {
			e1.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		} finally {
			close(ps, rs, psc, pscRS);
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return new Page(size, Long.valueOf(String.valueOf(page)), 0L, 0L,
				ImmutableList.copyOf(Collections.emptyList()));
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
	private static <T> Map<String, Object> getNotNullFieldMap(final T t) {
		final Map<String, Object> fMap = Maps.newLinkedHashMap();
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
			final Class<T> entityClass, final T t, final String sql) {

		if (t == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".update方法: t 参数不能为null");
		}

		final Field[] fs = t.getClass().getDeclaredFields();

		final Optional<Field> zidO = Lists.newArrayList(fs).stream().filter(f -> f.isAnnotationPresent(ZID.class))
				.findAny();
		if (!zidO.isPresent()) {
			throw new IllegalArgumentException(
					"无 " + ZID.class.getSimpleName() + " 标记的属性，t = " + t.getClass().getCanonicalName());
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
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("[{}],[{}],[{}]", sqlFAR.get(), t,idValue);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	private static <T> SUA updateHandler(final Class<T> entityClass, final T t, final String sqlF, final ZC2 zc2) {
		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.UPDATE);
		final SUA sua = new SUA(entityClass, t, entityClass, sqlF, null);
		sua.setZc2(zc2);
		sh.forEach(h -> h.handle(sua));

		return sua;
	}

	private static <T> Object getUpdateIdValue(final T t, final Optional<Field> zidO) {
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

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("[{}]", s);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> boolean deleteByIdIn(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final List<Object> idList, final Class<T> entityClass, final String sql) {

		if ((idList == null) || idList.isEmpty()) {
			return false;
		}

		final HashSet<Object> idSet = Sets.newHashSet(idList);

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("根据主键批量删除 - [{}]个主键值 - [{}]", idSet.size(), s2);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> boolean deleteById(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Object id, final Class<T> entityClass, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("[{}],[{}]", s,id);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> Map<Object, Boolean> existByIdIn(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Object idList, final Class<T> entityClass, final String sql) {

		final Map<Object, Boolean> v = Maps.newHashMap();
		if (idList == null) {
			v.put(null, false);
			return v;
		}

		final List idX = (List) ((List) idList).stream()
				.distinct().collect(Collectors.toList());
		if (CollUtil.isEmpty(idX)) {
			return v;
		}

		final List idNullList = (List) idX.stream().filter(x -> x == null).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(idNullList)) {
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
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);

		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sqlF, null, zc);
		final String sqlF1 = sua.getSql();
		final String sqlF2 = sqlF1.replaceFirst("\\?", idJoiner.toString());


		// 开始查询
		if (isShowSQL(dataSourceName)) {
			LOG.info("根据[{}]个ID批量查询是否存在-[{}]", idNotNullList.size(), sqlF1);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
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

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sql,new Object[] { id}, zc );
		sua.setZc2(zc);

		final String s = sua.getSql();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s,id);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return false;
	}

	public static <T> List<Object> saveAll(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> cls, final String sql,
			final List<T> tList) {

		if (CollUtil.isEmpty(tList)) {
			return Collections.emptyList();
		}

		final List<T> tl2 = tList.stream().filter(x -> x!=null).collect(Collectors.toList());

		final String dataSourceName = getDataSourceNameFromClassType(cls);

		final DBEnum db = getDBFromDataSourceName(dataSourceName);
		switch (db) {

		case SQLITE:
			// FIXME 2024年5月27日 下午5:54:36 zhangzhen: 对于sqlite而专门特别处理，从批量插入改为在一个事务里执行多次insert.
			// save0里的日志还要改，区分是从save还是saveAll方法来的

			final ZC2 zc2 = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
			final ZConnection zc = zc2.getZConnection();
			final Connection connection = zc.getConnection();

			final ArrayList<Object> idl = Lists.newArrayListWithCapacity(tl2.size());
			for (final T t : tl2) {
				try {
					final Object[] a = save0(zc.getDbEnum(), cls, t, sql, connection, null);
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

			returnZConnectionIfZCPool(dataSourceName, zc2);
			return idl;

		case MYSQL:
		case POSTGRESQL:
			return saveAllMysqlAndPGSQL(mode, cls, sql, tl2);

		default:
			break;
		}

		return Collections.emptyList();
	}

	private static DBEnum getDBFromDataSourceName(final String dataSourceName) {
		return ZRepositoryMain.getDB(dataSourceName);
	}

	private static <T> List<Object> saveAllMysqlAndPGSQL(final Mode mode, final Class<T> entityClass, final String sqlParam,
			final List<T> tList) {

		final Field[] declaredFields = entityClass.getDeclaredFields();
		final Optional<Field> zid = Lists.newArrayList(declaredFields).stream()
				.filter(f -> f.isAnnotationPresent(ZID.class)).findAny();
		if (!zid.isPresent()) {
			throw new IllegalArgumentException(
					"类中无 " + ZID.class.getSimpleName() + " 字段，cls = " + entityClass.getCanonicalName());
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("批量插入{}条数据 - [{}]", tList.size(), sql);
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
				if (type.getCanonicalName().equals(String.class.getCanonicalName())) {
					r.add(String.valueOf(id));
				} else if (type.getCanonicalName().equals(Integer.class.getCanonicalName())) {
					r.add(Integer.valueOf(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(Byte.class.getCanonicalName())) {
					r.add(Byte.valueOf(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(Short.class.getCanonicalName())) {
					r.add(Short.valueOf(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(Long.class.getCanonicalName())) {
					r.add(Long.valueOf(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(Double.class.getCanonicalName())) {
					r.add(Double.valueOf(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(BigInteger.class.getCanonicalName())) {
					r.add(new BigInteger(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(BigDecimal.class.getCanonicalName())) {
					r.add(new BigDecimal(String.valueOf(id)));
				} else if (type.getCanonicalName().equals(Character.class.getCanonicalName())) {
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
			returnZConnectionIfZCPool(dataSourceName, zc);
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

		final String key = cls.getCanonicalName() + "@" + sql;

		return ZRC.computeIfAbsent(key, supplier);
	}

	public static <T> T save(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass,
			final Class<?> entityTName, final T t, final String sql) {

		if (t == null) {
			throw new ZRepositoryException(ZRepository.class.getSimpleName() + ".save方法: t 参数不能为null");
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();
		try {

			final Object[] a = save0(zc.getZConnection().getDbEnum(), entityClass, t, sql, connection, null);
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
					return findById0(zc.getZConnection().getDbEnum(), mode, id, entityTName, selectById, zc.getZConnection());
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return null;
	}

	private static <T> Object[] save0(final DBEnum dbEunum, final Class<T> entityClass, final T t, final String sql,
			final Connection connection, final ZC2 zc2) throws SQLException {

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
			final StringJoiner add = joiner.add("?");
		}

		final String sql2 = sql.replace(MethodRegex.COLUMNS, arg.toString()).replace(MethodRegex.COLUMN_VALUES,
				joiner.toString());

		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.SAVE);
		final SUA sua = new SUA(entityClass, t, entityClass, sql2, null);
		sua.setZc2(zc2);
		sh.forEach(h -> h.handle(sua));

		PreparedStatement ps;
		if (isShowSQL(getDataSourceNameFromClassType(entityClass))) {
			LOG.info("[{}],[{}]", sql2, t);
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


	private static <T> boolean addPS(final DBEnum dbEnum, final T t, final PreparedStatement ps, final int i, final Field field, final SUMode mode)
			throws SQLException {

		try {

			field.setAccessible(true);
			final Object v2 = field.get(t);
			if (v2 == null) {
				ps.setObject(i, null);
				return false;
			}

			final String fn = field.getType().getCanonicalName();

			// FIXME 2024年5月3日 下午9:51:23 zhangzhen: 各种类型，考虑好要不要特殊处理，继续测试
			if (fn.equals(Boolean.class.getCanonicalName())) {
				// XXX sqlite也暂时Boolean和tinyint 对应，和mysql一样
				if ((dbEnum == DBEnum.MYSQL) || (dbEnum == DBEnum.SQLITE)) {
					final boolean equals = Boolean.TRUE.equals(v2);
					final byte vb = (byte) (equals ? 1 : 0);
					ps.setByte(i, vb);
				} else if (dbEnum == DBEnum.POSTGRESQL) {
					ps.setBoolean(i, Boolean.parseBoolean(String.valueOf(v2)));
				}
			} else if (fn.equals(Character.class.getCanonicalName())) {
				// char 类型直接用String
				ps.setString(i, String.valueOf(v2));
			} else if (fn.equals(Byte.class.getCanonicalName())) {
				ps.setByte(i, (Byte) v2);
			} else if (fn.equals(Short.class.getCanonicalName())) {
				ps.setShort(i, (Short) v2);
			} else if (fn.equals(Integer.class.getCanonicalName())) {
				ps.setInt(i, (Integer) v2);
			} else if (fn.equals(Long.class.getCanonicalName())) {
				ps.setLong(i, (Long) v2);
			} else if (fn.equals(Double.class.getCanonicalName())) {
				ps.setDouble(i, (Double) v2);
			} else if (fn.equals(String.class.getCanonicalName())) {
				ps.setString(i, String.valueOf(v2));
			} else if (fn.equals(BigDecimal.class.getCanonicalName())) {
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
			} else if (fn.equals(java.util.Date.class.getCanonicalName())) {
				// FIXME 2023年8月1日 下午8:50:26 zhanghen: TODO
				// 日期时间的字段，新增注解：表示插入的格式
				// ps.setDate(i, new java.sql.Date(((Date) v2).getTime()));
				// FIXME 2024年5月19日 下午9:23:37 zhangzhen: 考虑好sql.date 要不要对应DATE
				ps.setTimestamp(i, new java.sql.Timestamp(((Date) v2).getTime()));
			} else if (fn.equals(java.sql.Date.class.getCanonicalName())) {
				ps.setDate(i, (java.sql.Date) v2);
			} else if (fn.equals(java.sql.Time.class.getCanonicalName())) {
				ps.setTime(i, (java.sql.Time) v2);
			} else if (fn.equals(LocalTime.class.getCanonicalName())) {
				ps.setTime(i, Time.valueOf((LocalTime) v2));
			} else if (fn.equals(Timestamp.class.getCanonicalName())) {
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

	private static ZC2 getZCAndSetAutoCommitFALSE(final Mode mode, final String dataSourceName) {
		final ZC2 zcT = ZTransactionAOP.getCurrentZConnection();
		if (zcT != null) {
			return zcT;
		}

		final String k = "getconnection" + dataSourceName;
		synchronized (k.intern()) {
			final ZConnection zConnection = ZCPool.getInstance(dataSourceName).getZConnection(mode);
			try {
				final Connection connection = zConnection.getConnection();
				connection.setAutoCommit(true);
			} catch (final SQLException e) {
				e.printStackTrace();
			}

			final ZC2 zc2 = new ZC2(zConnection, ZCSourceEnum.ZCPOOL);
			ZTransactionAOP.resetToDefaultTransactionIsolation(zc2);
			try {
				zc2.getZConnection().getConnection().setAutoCommit(false);
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			return zc2;
		}

	}

	public static <T> List<T> find(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass,final Class<T> returnType, final String sql, final Object wrapper) {


		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
				LOG.info("[{}]", x2);
			}

			ps = connection.prepareStatement(x2);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), entityClass, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findAll(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final String sql) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);
		final String s = sua.getSql();

		final String select = gSelectFromReturnType(entityClass, entityClass);
		final String sF = s.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

		PreparedStatement ps = null;
		ResultSet rs = null;

		if (isShowSQL(dataSourceName)) {
			LOG.info("[{}]", sF);
		}

		try {

			ps = connection.prepareStatement(sF);
			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), entityClass, rs, metaData, count);
				r.add(t);
			}
			returnZConnectionIfZCPool(dataSourceName, zc);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
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
						+ entityClass.getCanonicalName() + "@"
						+ sql + "@"
						+ idList.get(0).getClass().getCanonicalName() + "@"
						+ idList;

		final ZC2 zc2 = getZCAndSetAutoCommitFALSE(mode, getDataSourceNameFromClassType(entityClass));

		final Supplier<List<T>> supplier = (Supplier<List<T>>) () -> findByIdIn0(zrSubClassName, callerMethodName, mode, idList, entityClass, sql, zc2);

		return (List<T>) selectFromCacheIfZT(cachekey, zc2, supplier);
	}

	private static <T> List<T> findByIdIn0(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final List<Object> idList, final Class<T> entityClass, final String sql, final ZC2 zc2) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final HashSet<Object> idSet = Sets.newHashSet(idList);

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
				LOG.info("根据主键批量查询 - [{}]个主键值 - [{}]", idSet.size(), s);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> rList = Lists.newArrayListWithCapacity(idSet.size());
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc2.getZConnection().getDbEnum(), entityClass, rs, metaData, count);
				rList.add(t);
			}

			saveSQLInvokeTime(zrSubClassName, callerMethodName, invokeTime, start, s2, entityClass.getAnnotation(ZEntity.class).tableName());

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
			returnZConnectionIfZCPool(dataSourceName, zc2);
		}

		return Collections.emptyList();
	}

	// FIXME 2024年6月8日 下午9:15:32 zhangzhen : 各个方法都加入
	private static void saveSQLInvokeTime(final String zrSubClassName, final String callerMethodName,
			final Date invokeTime, final long start, final String s2, final String tableName) {
		final SqlInvocationLogsConfigurationProperties cp = ZContext
				.getBean(SqlInvocationLogsConfigurationProperties.class);
		if ((cp != null) && cp.getEnable()) {
			final SqlInvocationLogsEntity entity = new SqlInvocationLogsEntity();
			entity.setTimeConsuming((int) (System.currentTimeMillis() - start));
			entity.setZrSubClassName(zrSubClassName);
			entity.setMethodName(callerMethodName);
			entity.setInvokeTime(invokeTime);
			entity.setSql(s2);
			entity.setTableName(tableName);

			final String beanName = SqlInvocationLogsService.class.getCanonicalName() + "@" + "service";
			// FIXME 2024年6月8日 下午8:14:06 zhangzhen : 这行改为异步的
			final SqlInvocationLogsService service = (SqlInvocationLogsService) ZContext.getBean(beanName);
			service.add(entity);
		}
	}

	private static <T> T findById0(final DBEnum dbEnum, final Mode mode, final Object id, final Class entityClass,
			final String sql, final ZConnection zc) {

		final String select = gSelectFromReturnType(entityClass, entityClass);
		final String sF = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			if (isShowSQL(getDataSourceNameFromClassType(entityClass))) {
				LOG.info("[{}],[{}]", sF, id);
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

		synchronized (key.intern()) {
			// FIXME 2024年7月2日 下午10:38:40 zhangzhen : 是否要重写一个类?因为 ZRC 类是用 WeakHashMap实现的，可能会丢数据
			final Object v = ZRC.computeIfAbsent(key, supplier, true);
			return v;
		}
	}

	// FIXME 2024年7月2日 下午11:01:31 zhangzhen : 当前只有本方法支持了事务内缓存，其他select操作的方法继续加
	public static <T> T findById(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Object id, final Class<T> entityClass, final String sql) {

		if (id == null) {
			return null;
		}

		return findById1(zrSubClassName, callerMethodName, mode, id, entityClass, sql);
	}

	private static <T> T findById1(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Object id, final Class<T> entityClass, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final Date invokeTime = new Date();
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);

		final ZCSourceEnum sourceEnum = zc.getSourceEnum();

		final Supplier supplier = () -> {
			final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);
			final String sql2 = sua.getSql();

			try {
				final T t = findById0(zc.getZConnection().getDbEnum(), mode, id, entityClass, sql2,
						zc.getZConnection());
				saveSQLInvokeTime(zrSubClassName, callerMethodName, invokeTime, invokeTime.getTime(), sql,
						entityClass.getAnnotation(ZEntity.class).tableName());
				return t;
			} finally {
				returnZConnectionIfZCPool(dataSourceName, zc);
			}
		};

		final String cachekey =
				zrSubClassName + "@"
						+ callerMethodName + "@"
						+ mode + "@"
						+ entityClass.getCanonicalName() + "@"
						+ sql + "@"
						+ id.getClass().getCanonicalName() + "@"
						+ id;

		return (T) selectFromCacheIfZT(cachekey, zc, supplier);
	}

	private static <T> SUA excludedDeletedHandler(final Class<T> entityClass, final Object entityObject, final Class returnClass,
			final String sql, final Object[] arg, final ZC2 zc2) {
		final Set<ZEntityHandler> sh = ZEntityHandlerScanner.get(ZEHEnum.SELECT_EXCLUDED_DELETED);
		final SUA sua = new SUA(entityClass, entityObject, returnClass, sql, arg);
		sua.setZc2(zc2);

		sh.forEach(h -> ((ZAllHandler)h).handle(sua));
		return sua;
	}

	private static <T> T newT(final DBEnum dbEnum, final Class<T> returnType, final ResultSet rs,
			final ResultSetMetaData metaData, final int count) {
		T object = null;
		try {
			object = returnType.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < count; i++) {
			Object columValue = null;
			try {
				columValue = rs.getObject(i + 1);
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			String columnName = null;
			try {
				// pgsql 对于 select max(id) as maxId
				// 读取到的 columnName 会是maxid ，导致匹配 returnType 中的字段时匹配不到，
				// 所以统一自定义SQL AS 后面的写法使用下划线命名法.只要是下划线命名法就行了，不关心大小写，
				// 在此统一为小写了
				columnName = metaData.getColumnLabel(i + 1).toLowerCase();
			} catch (final SQLException e) {
				e.printStackTrace();
			}

			final String javaFieldName = ZFieldConverter.toJavaField(columnName);
			Field field = null;
			try {
				field = returnType.getDeclaredField(javaFieldName);
				field.setAccessible(true);
			} catch (NoSuchFieldException | SecurityException e) {
				// 到此就continue而非抛异常，因为SQL和returnType都可以是自定义的。
				// 在此sql中的column匹配不到returnType中的Field，就直接忽略就行了
				// 有可能是手误多写了一个column，或者少写了一个Field等等情况
				// 前者会导致多一点流量，后者导致逻辑不通会自己发现的
				continue;
			}

			if (columValue == null) {
				continue;
			}

			final String cn = field.getType().getCanonicalName();
			try {

				if (cn.equals(Byte.class.getCanonicalName())) {
					field.set(object, Byte.valueOf(String.valueOf(columValue)));
				} else if (cn.equals(Short.class.getCanonicalName())) {
					field.set(object, Short.valueOf(String.valueOf(columValue)));
				} else if (cn.equals(Integer.class.getCanonicalName())) {
					field.set(object, Integer.valueOf(String.valueOf(columValue)));
				} else if (cn.equals(Long.class.getCanonicalName())) {
					field.set(object, Long.valueOf(String.valueOf(columValue)));
				} else if (cn.equals(Double.class.getCanonicalName())) {
					field.set(object, Double.valueOf(String.valueOf(columValue)));
				} else if (cn.equals(BigDecimal.class.getCanonicalName())) {
					field.set(object, new BigDecimal(String.valueOf(columValue)));
				} else if (cn.equals(Boolean.class.getCanonicalName())) {
					field.set(object,
							columValue == null ? null : (Integer.valueOf(1).equals(columValue) ? Boolean.TRUE : Boolean.FALSE));
				} else if (cn.equals(Character.class.getCanonicalName())) {
					field.set(object, Character.valueOf(String.valueOf(columValue).charAt(0)));
				} else if (cn.equals(String.class.getCanonicalName())) {
					field.set(object, String.valueOf(columValue));
				} else if (cn.equals(java.util.Date.class.getCanonicalName())) {
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
				} else if (cn.equals(java.sql.Date.class.getCanonicalName())) {
					if ((dbEnum == DBEnum.SQLITE) && (columValue.getClass() == Long.class)) {
						// sqlite 中此值为long类型
						final java.sql.Date date = new java.sql.Date((long) columValue);
						field.set(object, date);
					} else {
						field.set(object, columValue);
					}
				} else if (cn.equals(java.sql.Timestamp.class.getCanonicalName())) {
					final DBEnum db = dbEnum;
					if (columValue.getClass().equals(Timestamp.class)) {
						field.set(object, columValue);
					} else if (columValue.getClass() == Long.class) {
						// sqlite 中此值为long类型
						final Timestamp time = new Timestamp((long) columValue);
						field.set(object, time);
					}
				}  else if (cn.equals(LocalTime.class.getCanonicalName())) {
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

				} else if (cn.equals(java.sql.Time.class.getCanonicalName())) {
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
				} else if (field.getClass().isArray()) {
				} else {
					field.set(object, columValue);
				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		return object;
	}

	public static <T> List<T> findByXXAndXX(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final Class<T> returnType,final String sql, final Object... fieldArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, s, fieldArray, zc);
			final String s2 = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s2, Arrays.toString(fieldArray));
			}

			ps = connection.prepareStatement(s2);

			int i = 1;
			for (final Object object : fieldArray) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static String gSelectFromReturnType(final Class entityClass, final Class returnType) {
		final Supplier<String> supplier = () -> gSelectFromReturnType0(entityClass, returnType);
		final String key = "gSelectFromReturnType" + entityClass.getComponentType() +"@" + returnType.getCanonicalName();
		return ZRC.computeIfAbsent(key, supplier);
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

	public static <T> List<T> findByXX(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql, final Object fieldValue) {

		final ZC2 zc2 = getZCAndSetAutoCommitFALSE(mode, getDataSourceNameFromClassType(entityClass));

		// fieldValue 为null，没法使用缓存，因为不确定其类型，因为这个XX可以是任何字段任何类型
		if (fieldValue == null) {
			return findByXX0(entityClass, returnType, sql, fieldValue, zc2);
		}

		final Supplier<List<T>> supplier = () -> findByXX0(entityClass, returnType, sql, fieldValue, zc2);

		final String cachekey =
				zrSubClassName + "@"
						+ callerMethodName + "@"
						+ mode + "@"
						+ entityClass.getCanonicalName() + "@"
						+ returnType.getCanonicalName() + "@"
						+ sql + "@"
						+ fieldValue.getClass().getCanonicalName() + "@"
						+ fieldValue;

		return (List<T>) selectFromCacheIfZT(cachekey, zc2, supplier);
	}

	private static <T> List<T> findByXX0(final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object fieldValue, final ZC2 zc) {
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String x = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, x, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(getDataSourceNameFromClassType(entityClass))) {
				LOG.info("[{}],[{}]", s, fieldValue);
			}

			ps = connection.prepareStatement(s);
			final int index = 1;
			setXX_fieldValue(fieldValue, ps, index);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(getDataSourceNameFromClassType(entityClass), zc);
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
		} else {
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
		final Set set = (Set) (Lists.newArrayList((Iterable) value)).stream().filter(e -> e != null)
				.collect(Collectors.toSet());
		if (set.isEmpty()) {
			return Collections.emptyList();
		}

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
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
			LOG.info("[{}],{}", sF, set);
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

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), entityClass, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();

	}

	public static <T> List<T> findByIdLessThan(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql, final Object field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, select, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, field);
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, field);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXXEndingWith(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass,final Class<T> returnType, final String sql, final Object field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, select, returnType, sqlColumn, null, zc);

			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, "%" + field);
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, "%" + field);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXXStartingWith(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final Class<T> returnType,
			final String sql, final Object field) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, field + "%");
			}

			ps = connection.prepareStatement(s);
			ps.setObject(1, field + "%");

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXOrYY(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Class<T> entityClass, final Class<T> returnType, final String sql, final Object... field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, field, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, Arrays.toString(field));
			}
			ps = connection.prepareStatement(s);

			int index = 1;
			for (final Object object : field) {
				setXX_fieldValue(object, ps, index);
				index++;
			}


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXBetween(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fiedlArray) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fiedlArray, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, Arrays.toString(fiedlArray));
			}

			ps = connection.prepareStatement(s);

			int index = 1;
			for (final Object f : fiedlArray) {

				setXX_fieldValue(f, ps, index);

				index++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	// XXX 不和findByXXIsNullAndXX 复用代码，最后一个参数不同，区分Object... 仅传一个值并且为数组的情况，有bug
	public static <T> List<T> findByXXIsNullAndXXAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldArray) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fieldArray, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, Arrays.toString(fieldArray));
			}

			ps = connection.prepareStatement(s);

			int i = 1;
			for (final Object object : fieldArray) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXIsNullAndXXIsNullAndXXAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object... fieldValue) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, fieldValue, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, Arrays.toString(fieldValue));
			}

			ps = connection.prepareStatement(s);

			int i = 1;
			for (final Object object : fieldValue) {
				setXX_fieldValue(object, ps, i);
				i++;
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}
	public static <T> List<T> findByXXIsNullAndXXIsNullAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object fieldValue) {

		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();
			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, fieldValue);
			}

			ps = connection.prepareStatement(s);

			final int i = 1;
			setXX_fieldValue(fieldValue, ps, i);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXIsNullAndXX(final String zrSubClassName, final String callerMethodName,
			final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql,
			final Object fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();
			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, fieldValue);
			}

			ps = connection.prepareStatement(s);

			final int i = 1;
			setXX_fieldValue(fieldValue, ps, i);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXNotNull(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final Class<T> returnType, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}]", s);
			}

			ps = connection.prepareStatement(s);

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXLike(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass,final Class<T> returnType, final String sql, final Object field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String s1 = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sss = excludedDeletedHandler(entityClass, null, returnType, s1, null, zc);
			final String s2 = sss.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s2, "%" + field + "%");
			}

			ps = connection.prepareStatement(s2);
			ps.setObject(1, "%" + field + "%");


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	/**
	 * ZConnection 来自于ZCPool的才提交;来自SpringAOP的就不提交，而是由AOP类在目标方法执行结束后统一commit/rollback
	 *
	 * @param dataSourceName
	 * @param zc
	 */
	private static void returnZConnectionIfZCPool(final String dataSourceName, final ZC2 zc) {
		if (zc.getSourceEnum() == ZCSourceEnum.ZCPOOL) {
			returnZConnectionAndCommit(dataSourceName, zc.getZConnection());
		}
	}

	private static void returnZConnectionAndCommit(final String dataSourceName, final ZConnection zc) {
		ZCPool.getInstance(dataSourceName).returnZConnectionAndCommit(zc);
	}

	public static <T> List<T> findByXXIsNull(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass,final Class<T> returnType, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace ( MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, null, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}]", s);
			}

			ps = connection.prepareStatement(s);


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	private static <T> Long count(final Mode mode, final Class<T> entityClass, final String sql,final ZConnection zc, final String dataSourceName, final ZC2 zc2) {

		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;



		final SUA sua = excludedDeletedHandler(entityClass, null, entityClass, sql, null, zc2);
		final String s = sua.getSql();

		try {

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}]", s);
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
			returnZConnectionIfZCPool(dataSourceName, zc2);
		}

		return 0L;
	}

	public static <T> Long count(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> cls, final String sql) {
		final String dataSourceName = getDataSourceNameFromClassType(cls);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		return count(mode, cls, sql,zc.getZConnection(), dataSourceName, zc);
	}

	// FIXME 2024年5月18日 下午3:30:32 zhangzhen:  countingByXXAndXX 多个条件的不能改为Object...然后复用 countingByXX，因为可能一个条件的条件为byte[]
	// 会被认为是Object... a 是一个byte[]，而不是a.length = 1 并且第一个值是byte[].
	public static <T> Long countingByXXAndXX(final String zrSubClassName, final String callerMethodName,final Mode mode, final Class<T> entityClass, final String sql, final Object... fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);

			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, Arrays.toString(fieldValue));
			}
			ps = connection.prepareStatement(s);

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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return 0L;
	}

	public static <T> Long countingByXX(final String zrSubClassName, final String callerMethodName,final Mode mode,
			final Class<T> entityClass, final String sql, final Object fieldValue) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final SUA sua = excludedDeletedHandler(entityClass, null, null, sql, null, zc);

			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, fieldValue);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return 0L;
	}

	//	public static <T> List<T> findByXXOrderByXXLimit(final Mode mode, final Class<T> cls,
	public static <T> List<T> findByXXOrderByXXLimit(final String zrSubClassName, final String callerMethodName, final Mode mode, final Class<T> entityClass,
			final Class<T> returnType, final String sql, final Object... field) {
		final String dataSourceName = getDataSourceNameFromClassType(entityClass);
		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final String select = gSelectFromReturnType(entityClass, returnType);
			final String sqlColumn = sql.replace(MethodRegex.SELECT + " *", MethodRegex.SELECT + Sort.SPACE + select);

			final SUA sua = excludedDeletedHandler(entityClass, null, returnType, sqlColumn, field, zc);
			final String s = sua.getSql();

			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", s, Arrays.toString(field));
			}

			ps = connection.prepareStatement(s);
			int i = 1;
			for (final Object object : field) {
				setXX_fieldValue(object, ps, i);
				i++;
			}


			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(zc.getZConnection().getDbEnum(), returnType, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
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
	private static <T> String gUpdateColumn(final T t, final Field[] fs) {
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
	public static <T> List<T> zQuerySelect(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Object entityTClassName, final Object returnTypeClassName, final String sqleModeName,
			final String sqlT, final Object... arg) {

		if (StrUtil.isEmpty(sqlT)) {
			throw new ZRepositoryException(zrSubClassName + "." + callerMethodName + " " + " SQL不能为空");
		}

		if (!sqlT.trim().toUpperCase().startsWith(MethodRegex.SELECT)) {
			throw new ZRepositoryException(zrSubClassName + "." + callerMethodName + " " + " 只允许SELECT语句");
		}

		final ZEntity ze = (ZEntity) ((Class)entityTClassName).getAnnotation(ZEntity.class);
		final String dataSourceName = ze.dataSourceName();

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final int argcount = StrUtil.count(sqlT, '?');
			if (argcount != arg.length) {
				final String message = "@" + ZQuery.class.getCanonicalName() + " 自定义SQL参数个数[" + argcount
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
				if (ArrayUtil.isEmpty(arg)) {
					LOG.info("[{}]", s2);
				} else {
					LOG.info("[{}],[{}]", s2, Arrays.toString(arg));
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

			final List<T> ra = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = (T) newT(zc.getZConnection().getDbEnum(), returnClass, rs, metaData, count);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return Collections.emptyList();
	}

	public static Integer zQueryUpdate(final String zrSubClassName, final String callerMethodName,final Mode mode, final Object entityTName ,final Object object, final String sqleModeName,
			final String sql, final Object... arg) throws IllegalAccessException {

		return (Integer) updateOrDeleteOrInsert(mode, entityTName, object, sql, SUEnum.UPDATE, arg);
	}

	private static Object updateOrDeleteOrInsert(final Mode mode, final Object entityTName, final Object object, final String sql, final SUEnum suEnum, final Object... arg) {

		final Class cls = (Class) object;

		final ZEntity ze = (ZEntity) ((Class)entityTName).getAnnotation(ZEntity.class);
		final String dataSourceName = ze.dataSourceName();

		final ZC2 zc = getZCAndSetAutoCommitFALSE(mode, dataSourceName);
		final Connection connection = zc.getZConnection().getConnection();

		PreparedStatement prepareStatement = null;
		try {
			if (isShowSQL(dataSourceName)) {
				LOG.info("[{}],[{}]", sql, Arrays.toString(arg));
			}
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
					return rs.getObject(1);
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
			returnZConnectionIfZCPool(dataSourceName, zc);
		}

		return NO_DELETE_OR_DELETE;
	}

	public static <T> Integer zQueryDelete(final String zrSubClassName, final String callerMethodName,final Mode mode, final Object entityTName, final Object object,
			final String sqleModeName, final String sql, final Object... arg) {

		return (Integer) updateOrDeleteOrInsert(mode, entityTName, object, sql, SUEnum.DELETE, arg);
	}

	public static <T> Object zQueryInsert(final String zrSubClassName, final String callerMethodName, final Mode mode,
			final Object entityTName, final Class<T> cls, final String sqleModeName, final String sql, final Object... arg) {
		return updateOrDeleteOrInsert(mode, entityTName, cls, sql, SUEnum.INSERT, arg);
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

		final ZC2 zc = getZCAndSetAutoCommitFALSE(Mode.WRITE, dataSourceName);

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
	
}
