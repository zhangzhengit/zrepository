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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vo.anno.ZEntity;
import com.vo.anno.ZTransient;
import com.vo.conn.Mode;
import com.vo.conn.ZCPool;
import com.vo.conn.ZConnection;
import com.vo.conn.ZDatasourceProperties;
import com.vo.conn.ZDatasourcePropertiesLoader;
import com.vo.core.Page;
import com.vo.core.ZLog2;
import com.vo.transaction.ZTransactionAspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @see ZRepository 接口和其子接口里的方法的具体实现
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
// FIXME 2023年9月16日 下午7:57:12 zhanghen: 考虑清楚每个方法 @ZID 字段为空怎么处理
public class SU {
// FIXME 2024年5月10日 下午9:15:39 zhangzhen: 由于支持了二进制类型，参数传来数组，log.xx时需要 Array.toString 记得改
	private static final int NO_DELETE_OR_DELETE = -1;
	private static final int NO_DELETE = -1;
	private static final int NO_UPDATE = -1;
	private static final String COLUMN = "COLUMN";
	private static final String LIMIT = "limit";
	private static final ZLog2 LOG = ZLog2.getInstance();
	private static final ZDatasourceProperties ZDP = ZDatasourcePropertiesLoader.getInstance();

	private static final ZCPool INSTANCE = ZCPool.getInstance();

	public static <T> Page<T> page(final Mode mode, final Class<T> cls, final T t, final String sql, final Integer size, final Integer page) {
		System.out
				.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t" + "SU.page()");

		if (size <= 0) {
			throw new IllegalArgumentException("size 必须大于0！size = " + size);
		}
		if (page <= 0) {
			throw new IllegalArgumentException("page 必须大于0！page = " + page);
		}

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();
//		PreparedStatement ps = null;
//		ResultSet rs  = null;
		try {
			connection.setAutoCommit(false);

			final Map<String, Object> fMap = getNotNullFieldMap(t);
			final String sqlFinal = sql.replace(COLUMN, "").replace("where", "");
			if (CollUtil.isEmpty(fMap)) {
				return page0(mode, cls, size, page, zc, sqlFinal, true);
			}

			final Set<Entry<String, Object>> es = fMap.entrySet();
			final StringBuilder builder = new StringBuilder();
			for (final Entry<String, Object> entry : es) {
				final String fieldName = entry.getKey();
				final Object fieldValue = entry.getValue();

				// FIXME 2023年9月6日 下午9:05:35 zhanghen: 测试不同类型的字段，看直接append(toString)是否报错
				builder.append(" ").append(fieldName).append(" = ");
				if (fieldValue instanceof String) {
					builder.append("'").append(fieldValue).append("'");
				} else if (fieldValue instanceof Number) {
					builder.append(fieldValue);
				}

				builder.append(" and ");
			}
			final String x = builder.replace(builder.length()-5, builder.length(), "").toString();
			final String sqlFinalX = sql.replace(COLUMN, x);

			return page0(mode, cls, size, page, zc, sqlFinalX, false);

		} catch (final SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException e1) {
			e1.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		} finally {
			ZCPool.getInstance().returnZConnectionAndCommit(zc);
		}

		return null;
	}

	private static <T> Page<T> page0(final Mode mode, final Class<T> cls, final Integer size, final Integer page,
			final ZConnection zc, final String sqlFinal, final boolean tAllFieldNull)
			throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		final PreparedStatement	ps = zc.getConnection().prepareStatement(sqlFinal);
		final int offset = (page -1) * size;
		final int rows = size;
		ps.setInt(1, offset);
		ps.setInt(2, rows);

		if (ZDP.getShowSql()) {
			LOG.info("[{}],[{},{}]", sqlFinal, offset, rows);
		}

		final ResultSet	rs = ps.executeQuery();
		final ResultSetMetaData metaData = rs.getMetaData();

		final int count = metaData.getColumnCount();
		final List<T> rL = Lists.newArrayList();
		while (rs.next()) {
			final T tR = newT(cls, rs, metaData, count);
			rL.add(tR);
		}

		final String tableName = cls.getAnnotation(ZEntity.class).tableName();

		final int limitI = sqlFinal.indexOf(LIMIT);
		final String countSQLNotNUll = sqlFinal.replace("select * ", "select count(*) ");

		final String countSQL = tAllFieldNull ? "select count(*) from " + tableName
				: sqlFinal.substring(0, limitI).replace("select * ", "select count(*) ");
		final Long countR = count(mode, cls, countSQL, zc);

		final long pages = (countR.longValue() % size) == 0 ? countR.longValue() / size
				: (countR.longValue() / size) + 1;
		final Page<T> pageR = new Page(size, Long.valueOf(String.valueOf(page)), pages, countR, ImmutableList.copyOf(rL));
		return pageR;
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
		final Map<String, Object> fMap = Maps.newHashMap();
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

	public static <T> T update(final Mode mode, final Class<T> cls, final T t, final String sql) {
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

		final String sqlF = sql.replace(COLUMN, gUpdateColumn);

		final ZConnection zc = ZCPool.getInstance().getZConnection(mode);

		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}
		PreparedStatement ps  = null;
		try {
			ps = connection.prepareStatement(sqlF);
			int zTransientCount = 0;
			int index = 0;
			for (int i = 0; i < (fs.length); i++) {
				final Field f = fs[i];
				if (f.isAnnotationPresent(ZTransient.class)) {
					zTransientCount++;
					continue;
				}
				f.setAccessible(true);
				index++;
				addPS(t, ps, index, f, SUMode.UPDATE);
			}

			// 最后面的where id = ？ 赋值
			ps.setObject((fs.length + 1) -zTransientCount , idValue);

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}],[{}]", sqlF, t,idValue);
			}

			final int executeUpdate = ps.executeUpdate();

		} catch (final Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			ZCPool.getInstance().returnZConnectionAndCommit(zc);
			close(ps);
		}
		// XXX 直接返回T可以吗？
		return t;
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

	public static <T> boolean deleteAll(final Mode mode, final Class<T> cls, final String sql) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}
		PreparedStatement ps =null;
		try {
			final String s = sql;
			ps = connection.prepareStatement(s);
			// FIXME 2023年9月6日 上午2:39:45 zhanghen: 配置为参数
			ps.setQueryTimeout(22);

			if (ZDP.getShowSql()) {
				LOG.info("[{}]", s);
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
			INSTANCE.returnZConnectionAndCommit(zc);
			close(ps);
		}

		return false;
	}

	public static <T> boolean deleteByIdIn(final Mode mode, final List<Object> idList, final Class<T> cls, final String sql) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		PreparedStatement ps = null;

		try {

			final String params = String.join(",", Collections.nCopies(idList.size(), "?"));

			final String sqlT = sql.replace("?", params);

			ps = connection.prepareStatement(sqlT);

			int index = 1;
			for (final Object id : idList) {
				ps.setObject(index, id);
				index++;
			}

			if (ZDP.getShowSql()) {
				LOG.info("根据主键批量删除 - [{}]个主键值 - [{}]", idList.size(), sql);
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
//			try {
//				connection.commit();
//			} catch (final SQLException e1) {
//				e1.printStackTrace();
//			}
			INSTANCE.returnZConnectionAndCommit(zc);
			close(ps);
		}

		return false;
	}

	public  static <T> boolean deleteById(final Mode mode, final Object id, final Class<T> cls, final String sql) {
		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}
		PreparedStatement ps = null;
		try {
			final String s = sql;
			ps= connection.prepareStatement(s);
			ps.setObject(1, id);

			if (ZDP.getShowSql()) {
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
			try {
				connection.commit();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			close(ps);
			INSTANCE.returnZConnectionAndCommit(zc);
		}

		return false;
	}

	public static <T> boolean existById(final Mode mode, final Object id, final Class<T> cls, final String sql) {

		if (Objects.isNull(id)) {
			return false;
		}

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, id);

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s,id);
			}

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
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return false;
	}

	// FIXME 2024年5月4日 下午9:10:32 zhangzhen:TODO  支持了blob类型后，saveAll会NPE？继续测试
	public static <T> List<Object> saveAll(final Mode mode, final Class<T> cls, final String sqlParam,
			final List<T> tList) {

		if (CollUtil.isEmpty(tList)) {
			return Collections.emptyList();
		}

		final Field[] declaredFields = cls.getDeclaredFields();
		final Optional<Field> zid = Lists.newArrayList(declaredFields).stream()
				.filter(f -> f.isAnnotationPresent(ZID.class)).findAny();
		if (!zid.isPresent()) {
			throw new IllegalArgumentException(
					"类中无 " + ZID.class.getSimpleName() + " 字段，cls = " + cls.getCanonicalName());
		}

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		final String sql = generateSaveAllSQL(cls, sqlParam);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);

			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			for (final T t : tList) {
				int index = 1;
				for (final Field f : declaredFields) {
					if (f.isAnnotationPresent(ZID.class) || f.isAnnotationPresent(ZTransient.class)) {
						continue;
					}
					addPS(t, ps, index, f, SUMode.SAVE);
					index++;
				}
				ps.addBatch();
			}

			if (ZDP.getShowSql()) {
				LOG.info("批量插入{}条数据 - [{}]", tList.size(),sql);
			}

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
				} else if (type.getCanonicalName().equals(Float.class.getCanonicalName())) {
					r.add(Float.valueOf(String.valueOf(id)));
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
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}

		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	private static <T> String generateSaveAllSQL(final Class<T> cls, final String sql) {
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

		final String sql2 = sql.replace("F", arg.toString()).replace("A", v.toString());
		return sql2;
	}

	public static <T> T save(final Mode mode, final Class<T> cls, final T t, final String sql) {
		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		final StringJoiner arg = new StringJoiner(",");
		final Field[] fs = cls.getDeclaredFields();
		int fieldCount = 0;
		for (final Field field : fs) {
			if (field.isAnnotationPresent(ZID.class) || field.isAnnotationPresent(ZTransient.class)) {
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

		final String sql2 = sql.replace("F", arg.toString()).replace("A", joiner.toString());
		PreparedStatement ps = null;
		try {
			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", sql2, t);
			}
			ps = connection.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
			int i = 0;
			for (final Field field : fs) {
				if (field.isAnnotationPresent(ZID.class) || field.isAnnotationPresent(ZTransient.class)) {
					continue;
				}

				i++;
				addPS(t, ps, i, field, SUMode.SAVE);
			}
			final int executeUpdate = ps.executeUpdate();
			final ResultSet rs = ps.getGeneratedKeys();
			try {

				if (rs.next()) {
					final Object id = rs.getObject(1);
					final ZEntity zEntity = t.getClass().getAnnotation(ZEntity.class);
					final String selectById = "select * from " + zEntity.tableName() + " where id = ?";
					final T findByIdNew = findById(mode, id, cls, selectById, zc);
					return findByIdNew;
				}
			} finally {
				rs.close();
			}

		} catch (final SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			close(ps);
			INSTANCE.returnZConnectionAndCommit(zc);
		}

		return null;
	}

	private static <T> boolean addPS(final T t, final PreparedStatement ps, final int i, final Field field, final SUMode mode)
			throws SQLException {
		final String dbFieldname = ZFieldConverter.toDbField(field.getName());
		field.setAccessible(true);

		try {

			final Object v2 = field.get(t);
			// FIXME 2024年5月3日 下午9:31:08 zhangzhen:
			// 在此要不要处理为Entity里类型不允许为基本类型，这样在这里的逻辑就简单了
			// Field.get 的v为null就setnull就行了
			if (v2 == null) {
				ps.setObject(i, null);
				return false;
			}

			final String fn = field.getType().getCanonicalName();

			// FIXME 2024年5月3日 下午9:51:23 zhangzhen: 各种类型，考虑好要不要特殊处理，继续测试
			if (fn.equals(Boolean.class.getCanonicalName())) {
				final boolean equals = Boolean.TRUE.equals(v2);
				final byte vb = (byte) (equals ? 1 : 0);
				ps.setByte(i, vb);
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
			} else if (fn.equals(Float.class.getCanonicalName())) {
				ps.setFloat(i, (Float) v2);
			} else if (fn.equals(Double.class.getCanonicalName())) {
				ps.setDouble(i, (Double) v2);
			} else if (fn.equals(String.class.getCanonicalName())) {
				ps.setString(i, String.valueOf(v2));
			} else if (fn.equals(BigDecimal.class.getCanonicalName())) {
				ps.setBigDecimal(i, (BigDecimal) v2);
			} else if (v2.getClass().isArray()) {
				// blob类型
				// FIXME 2024年5月5日 下午9:14:57 zhangzhen: saveAll 时，setBlob和setBinaryStream都会导致ps.excuteBatch NPE,所有在此用setObject
				ps.setObject(i, v2);
//				final ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) v2);
//				ps.setBlob(i, inputStream);
//				ps.setBinaryStream(i, inputStream);
			} else if (fn.equals(Date.class.getCanonicalName())) {
				// FIXME 2023年8月1日 下午8:50:26 zhanghen: TODO
				// 日期时间的字段，新增注解：表示插入的格式
				ps.setDate(i, new java.sql.Date(((Date) v2).getTime()));
			} else if (fn.equals(Time.class.getCanonicalName())) {
				ps.setTime(i, (Time) v2);
			} else if (fn.equals(Timestamp.class.getCanonicalName())) {
				ps.setTimestamp(i, (Timestamp) v2);
			} else {
				// FIXME 2024年5月4日 下午2:41:05 zhangzhen: TODO
				// 暂时只支持上面这些类型，在程序启动时就校验字段类型是否支持，而不是在此提示，在此提示太晚了（程序已经开始运行了）
//							throw new IllegalArgumentException("size 必须大于0！size = " + size);
			}

			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return false;
	}

	private static ZConnection getZCAndSetAutoCommitFALSE(final Mode mode) {
		// FIXME 2023年6月18日 上午12:00:42 zhanghen: 先从 ZTAs 拿，无再从下面方法拿
		final ZConnection zcT = ZTransactionAspect.ZCONNECTION_THREADLOCAL.get();
		if (zcT != null) {
			try {
				zcT.getConnection().setAutoCommit(false);
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			return zcT;
		}

		// FIXME 2023年9月6日 下午2:33:36 zhanghen: 在此setAutoCommit(false)然后在归还方法里commit?
		final ZConnection zc = INSTANCE.getZConnection(mode);
		try {
			zc.getConnection().setAutoCommit(false);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return zc;
	}

	public static <T> List<T> findAll(final Mode mode, final Class<T> cls, final String sql) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);
			if (ZDP.getShowSql()) {
				LOG.info("[{}]", sql);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}
			INSTANCE.returnZConnectionAndCommit(zc);
			return r;

		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	// FIXME 2023年9月24日 下午3:41:35 zhanghen: 继续写，测试各种db和java的日期类型转换
	private static <T> Object handValue(final T t, final Object columValue, final Field field) {
		if (columValue instanceof LocalDateTime) {
			final Date date = Date.from(((LocalDateTime) columValue).atZone(ZoneId.systemDefault()).toInstant());
			final ZDateFormat zdf = field.getAnnotation(ZDateFormat.class);
			if (zdf != null) {
				final String format = zdf.format().getFormat();
				final SimpleDateFormat sss = new SimpleDateFormat(format);

				final String format2 = sss.format(date);

				final DateTime parse = DateUtil.parse(format2, format);
				return parse;
			}

			return date;
		}

		return columValue;
	}

	public static <T> List<T> findByIdIn(final Mode mode, final List<Object> idList, final Class<T> cls, final String sql) {

		final ZConnection zc = INSTANCE.getZConnection(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String s = sql;
			// select * from user where id in (?)
			final StringJoiner joiner = new StringJoiner(",");
			for (final Object id : idList) {
				joiner.add(String.valueOf(id));
			}
			final String param = joiner.toString();
			final String s2 = s.replace("?", param);
			ps = connection.prepareStatement(s2);
			if (ZDP.getShowSql()) {
				LOG.info("根据主键批量查询 - [{}]个主键值 - [{}]", idList.size(), s);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> rList = Lists.newArrayListWithCapacity(idList.size());
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				rList.add(t);
			}
			return rList;
		} catch (SQLException | SecurityException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();

			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	private static <T> T findById(final Mode mode, final Object id, final Class<T> cls, final String sql,
			final ZConnection zc) {
		if (Objects.isNull(id)) {
			return null;
		}

		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}


		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, id);

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, id);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			if (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				return t;
			}

		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return null;
	}

	public static <T> T findById(final Mode mode, final Object id, final Class<T> cls, final String sql) {
		final ZConnection zc = INSTANCE.getZConnection(mode);
		return findById(mode, id, cls, sql, zc);
	}

	private static <T> T newT(final Class<T> cls, final ResultSet rs, final ResultSetMetaData metaData, final int count)
			throws InstantiationException, IllegalAccessException, SQLException, NoSuchFieldException {
		final T object = cls.newInstance();
		for (int i = 0; i < count; i++) {
			final Object columValue = rs.getObject(i + 1);
			final String columnName = metaData.getColumnLabel(i + 1);
			final String javaFieldName = ZFieldConverter.toJavaField(columnName);
			final Field ffffff = cls.getDeclaredField(javaFieldName);
			ffffff.setAccessible(true);

			final Object value = handValue(object, columValue, ffffff);
			if (value == null) {
				continue;
			}

			final String cn = ffffff.getType().getCanonicalName();
			if (cn.equals(Byte.class.getCanonicalName())) {
				ffffff.set(object, Byte.valueOf(String.valueOf(value)));
			} else if (cn.equals(Short.class.getCanonicalName())) {
				ffffff.set(object, Short.valueOf(String.valueOf(value)));
			} else if (cn.equals(Integer.class.getCanonicalName())) {
				ffffff.set(object, Integer.valueOf(String.valueOf(value)));
			} else if (cn.equals(Long.class.getCanonicalName())) {
				ffffff.set(object, Long.valueOf(String.valueOf(value)));
			} else if (cn.equals(Float.class.getCanonicalName())) {
				ffffff.set(object, Float.valueOf(String.valueOf(value)));
			} else if (cn.equals(Double.class.getCanonicalName())) {
				ffffff.set(object, Double.valueOf(String.valueOf(value)));
			} else if (cn.equals(BigDecimal.class.getCanonicalName())) {
				ffffff.set(object, new BigDecimal(String.valueOf(value)));
			} else if (cn.equals(Boolean.class.getCanonicalName())) {
				ffffff.set(object,
						value == null ? null : (Integer.valueOf(1).equals(value) ? Boolean.TRUE : Boolean.FALSE));
			} else if (cn.equals(Character.class.getCanonicalName())){
				ffffff.set(object, Character.valueOf(String.valueOf(value).charAt(0)));
			} else if (cn.equals(String.class.getCanonicalName())){
				ffffff.set(object, String.valueOf(value));
			} else {
				if (ffffff.getClass().isArray()){
				}
				ffffff.set(object, value);
			}

		}
		return object;
	}

	public static <T> List<T> findByXX(final Mode mode, final Class<T> cls, final String sql, final Object... fieldArray) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);

			int i = 1;
			for (final Object object : fieldArray) {
				ps.setObject(i, object);
				i++;
			}

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, Arrays.toString(fieldArray));
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXX(final Mode mode, final Class<T> cls, final String sql, final Object fieldValue) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, fieldValue);

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, fieldValue);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXIn(final Mode mode, final Class<T> cls, final String sql, final Object... fieldArray) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		Statement statement = null;
		ResultSet rs = null;
		try {
			// "select * from user where id = ?"
			statement = connection.createStatement();

			String s = sql;
			for (final Object object : fieldArray) {
				// FIXME 2023年6月16日 下午5:14:50 zhanghen: 处理这里，自定义声明式方法不仅可以用List，改为支持Iterable和数组
				if(object instanceof List) {
					final List list = (List) object;

					final StringJoiner joiner = new StringJoiner(",");
					final Object[] a1 = ((List)object).toArray();
					for (final Object object2 : list) {
						if(object2  instanceof String) {
							joiner.add("'" + String.valueOf(object2) + "'");
						}else {
							joiner.add(String.valueOf(object2));
						}
					}
					final String string = Arrays.toString(a1);
					final String sss = joiner.toString().replace("[", "").replace("]", "");
					// ? 是 sql目标中的参数值占位符
					s = s.replaceFirst("\\?", sss);
				}
			}

			if (ZDP.getShowSql()) {
				LOG.info("[{}]", s);
			}

			rs = statement.executeQuery(s);

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, statement);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByIdLessThan(final Mode mode, final Class<T> cls, final String sql, final Object field) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, field);


			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, field);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXXEndingWith(final Mode mode, final Class<T> cls, final String sql, final Object field) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, "%" + field);

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, "%" + field);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);

			close(rs, ps);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXXStartingWith(final Mode mode, final Class<T> cls, final String sql, final Object field) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, field + "%");

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, field + "%");
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}
		return Collections.emptyList();
	}

	public static <T> List<T> findByXXLike(final Mode mode, final Class<T> cls, final String sql, final Object field) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, "%" + field + "%");

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, "%" + field + "%");
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	public static <T> List<T> findByXXIsNull(final Mode mode, final Class<T> cls, final String sql) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// "select * from user where id = ?"
			final String s = sql;
			ps = connection.prepareStatement(s);

			if (ZDP.getShowSql()) {
				LOG.info("[{}]", s);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;
		} catch (SQLException | InstantiationException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	public static <T> Long count(final Mode mode, final Class<T> cls, final String sql,final ZConnection zc) {

		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String s = sql;
			ps = connection.prepareStatement(s);

			if (ZDP.getShowSql()) {
				LOG.info("[{}]", s);
			}

			rs = ps.executeQuery();

			if (rs.next()) {
				final Long count = rs.getLong(1);
				return count;
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return 0L;
	}

	public static <T> Long count(final Mode mode, final Class<T> cls, final String sql) {
		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		return count(mode, cls, sql,zc);
	}

	public static <T> Long countingByXX(final Mode mode, final Class<T> cls, final String sql, final Object field) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String s = sql;
			ps = connection.prepareStatement(s);
			ps.setObject(1, field);

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, field);
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			if (rs.next()) {
				final Long count = rs.getLong(1);
				return count;
			}

		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return 0L;
	}

	public static <T> List<T> findByXXOrderByXXLimit(final Mode mode, final Class<T> cls, final String sql, final Object... field) {

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			final String s = sql;
			ps = connection.prepareStatement(s);
			int i = 1;
			for (final Object object : field) {
				ps.setObject(i, object);
				i++;
			}

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", s, Arrays.toString(field));
			}

			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final ArrayList<T> r = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = newT(cls, rs, metaData, count);
				r.add(t);
			}

			return r;

		} catch (SQLException | SecurityException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return Collections.emptyList();
	}

	private static void close(final AutoCloseable... autoCloseables) {
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

			if (f.isAnnotationPresent(ZTransient.class)) {
				continue;
			}
			// update语句，即使是id也生成：id = ？
//			if (f.isAnnotationPresent(ZID.class)) {
//				continue;
//			}

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

	public static <T> List<T> zQuerySelect(final Mode mode, final Object object, final String sql, final Object... arg)
			throws InstantiationException {
		final Class cls = (Class) object;

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			final int argcount = StrUtil.count(sql, '?');
			if (argcount != arg.length) {
				final String message = "@" + ZQuery.class.getCanonicalName() + " 自定义SQL参数个数[" + argcount
						+ "]和方法传入的参数个数[" + arg.length + "]不匹配";
				throw new ZQuerySQLException(message);
			}

			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", sql, Arrays.toString(arg));
			}
			ps = connection.prepareStatement(sql);
			if (arg != null) {
				int n = 1;
				for (final Object a1 : arg) {
					ps.setObject(n, a1);
					n++;
				}
			}
			rs = ps.executeQuery();

			final ResultSetMetaData metaData = rs.getMetaData();

			final List<T> ra = Lists.newArrayList();
			while (rs.next()) {
				final int count = metaData.getColumnCount();
				final T t = (T) newT(cls, rs, metaData, count);
				ra.add(t);
			}

			return ra;
		} catch (SQLException | SecurityException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(rs, ps);
		}

		return null;
	}

	public static int zQueryUpdate(final Mode mode, final Object object, final String sql,
			final Object... arg) throws IllegalAccessException {

		final Integer updateOrDelete = updateOrDelete(mode, object, sql, arg);

		return updateOrDelete;
	}

	private static Integer updateOrDelete(final Mode mode, final Object object, final String sql, final Object... arg) {
		final Class cls = (Class) object;

		final ZConnection zc = getZCAndSetAutoCommitFALSE(mode);
		final Connection connection = zc.getConnection();

		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		}

		PreparedStatement prepareStatement = null;
		try {
			if (ZDP.getShowSql()) {
				LOG.info("[{}],[{}]", sql, Arrays.toString(arg));
			}
			prepareStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			if (arg != null) {
				int n = 1;
				for (final Object a1 : arg) {
					prepareStatement.setObject(n, a1);
					n++;
				}
			}
			final int executeUpdate = prepareStatement.executeUpdate();
			return executeUpdate;
		} catch (SQLException | SecurityException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			INSTANCE.returnZConnectionAndCommit(zc);
			close(prepareStatement);
		}

		return NO_DELETE_OR_DELETE;
	}

	public static <T> Integer zQueryDelete(final Mode mode, final Object object, final String sql,
			final Object... arg) {

		final Integer updateOrDelete = updateOrDelete(mode, object, sql, arg);

		return updateOrDelete;
	}

	public static  <T> List<T> zQueryInsert(final Mode mode, final Class<T> cls, final String sql) {
		// FIXME 2024年5月5日 下午10:51:53 zhangzhen: 写这个
		// FIXME 2024年5月9日 下午11:44:27 zhangzhen: insert貌似没必须要写，直接用save方法就行了吧？
		return Collections.emptyList();
	}

}
