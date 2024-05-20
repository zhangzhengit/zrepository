package com.vo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vo.anno.UserRepositoryTest1;
import com.vo.anno.ZEntity;
import com.vo.anno.ZRead;
import com.vo.anno.ZTransient;
import com.vo.anno.ZWrite;
import com.vo.conn.Mode;
import com.vo.conn.ZCPool;
import com.vo.conn.ZConnection;
import com.vo.core.ZClass;
import com.vo.core.ZField;
import com.vo.core.ZLog2;
import com.vo.core.ZMethod;
import com.vo.core.ZMethodArg;
import com.vo.core.ZPackage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;

/**
 *
 * ZR 启动类
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
public class ZRepositoryMain {

	public static final String DELIMITER = ",";

	private static final String TYPE_NAME = "TYPE_NAME";

	private static final String COLUMN_NAME = "COLUMN_NAME";

	private static final String EMTPY = "";

	public static final String TABLE_NAME = "TABLE_NAME";

	private static final ZLog2 LOG = ZLog2.getInstance();

	public static final String _Z_CLASS = "_ZClass";

	public static <T> List<T> test_select_1(final Class<T> cls) throws SQLException, InstantiationException,
			IllegalAccessException, NoSuchFieldException, SecurityException {
		final ZConnection zc = ZCPool.getInstance().getZConnection(Mode.WRITE);
		final Connection connection = zc.getConnection();

		final PreparedStatement ps = connection.prepareStatement("select * from user where id = ?");

		ps.setInt(1, 1);

		final ResultSet rs = ps.executeQuery();

		final ResultSetMetaData metaData = rs.getMetaData();
		final List<T> r = Lists.newArrayList();
		while (rs.next()) {
			final int count = metaData.getColumnCount();
			final T t = cls.newInstance();
			for (int i = 0; i < count; i++) {
				final Object columValue = rs.getObject(i + 1);
				final String columnName = metaData.getColumnLabel(i + 1);
				final String javaFieldName = ZFieldConverter.toJavaField(columnName);
				final Field field = cls.getDeclaredField(javaFieldName);
				field.setAccessible(true);
				field.set(t, columValue);
			}

			r.add(t);
//			System.out.println("T =" + t);
		}

		ZCPool.getInstance().returnZConnectionAndCommit(zc);

		return r;
	}

	public static void start() {
		System.out.println(
				java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t" + "ZRMain.start()");


		// 0 checkZEntityField
//		checkZEntityField();

		// 1 建立连接池
//		ZCPool.getInstance();

		// 2 扫描 @ZEntity的类
//		final Set<Class<?>> zeClassSet = scanZEntity();
//		gWrapperRepository(zeClassSet);

		// 3给 ZR 的子类根据方法名称来生成对应的sql
//		gSqlForZRSubclass(zeClassSet);
//		final Set<Class<?>> zrSubclassSet = scanZRSubclass();

//		generateSqlForZRSubclass(zrSubclassSet);


		// 测试 插入一条数据

		// FIXME 2023年6月16日 上午11:08:00 zhanghen: 先生成ZR的子接口的实现类
//		generateClassForZRSubinterface(zrSubclassSet);


// FIXME 2023年6月16日 上午10:59:52 zhanghen: 自己写一个aop

	}

	public static Map<Class, ZClass> generateClassForZRSubinterfaceMap(final Set<Class<?>> zrSubclassSet) {

		ZCPool.getInstance();

		LOG.info("开始给[{}]的子接口生成实现类", ZRepository.class.getCanonicalName());

		final Map<Class, ZClass> map = Maps.newConcurrentMap();

//		final Set<ZClass> zrsubIZClass = Sets.newHashSet();
		for (final Class<?> cls : zrSubclassSet) {
			LOG.info("开始给[{}]的子接口[{}]生成实现类", ZRepository.class.getCanonicalName(), cls.getCanonicalName());
			final String canonicalName = cls.getCanonicalName();
//			System.out.println("canonicalName = \n\n" + canonicalName);

			final ZClass generateZRepositorySubclass = generateMyZRepositorySubclass(cls);
			LOG.info("给[{}]的子接口[{}]生成实现类完成,className={},class=\n\n{}", ZRepository.class.getCanonicalName(),
					cls.getCanonicalName(), generateZRepositorySubclass.getName(),
					generateZRepositorySubclass.toString());

//			zrsubIZClass.add(generateZRepositorySubclass);
			map.put(cls, generateZRepositorySubclass);
//			final Object newInstance = generateZRepositorySubclass.newInstance();
//			System.out.println("generateZRepositorySubclass-newInstance = \n\n" + newInstance);
		}

		return map;
	}

	public static Map<Class, ZClass> generateClassForZRSubinterface(final Set<Class<?>> zrSubclassSet) {

		LOG.info("开始给[{}]的子接口生成实现类", ZRepository.class.getCanonicalName());

		final Map<Class, ZClass> map = Maps.newHashMap();

		for (final Class<?> cls : zrSubclassSet) {
			LOG.info("开始给[{}]的子接口[{}]生成实现类", ZRepository.class.getCanonicalName(), cls.getCanonicalName());

			final ZClass generateZRepositorySubclass = generateMyZRepositorySubclass(cls);
			LOG.info("给[{}]的子接口[{}]生成实现类完成,className={},class=\n\n{}", ZRepository.class.getCanonicalName(),
					cls.getCanonicalName(), generateZRepositorySubclass.getName(),
					generateZRepositorySubclass.toString());

			map.put(cls, generateZRepositorySubclass);

		}

		return map;
	}

	/**
	 * 扫描接口 ZRepository 的子接口(自定义的继承了ZRepository的接口)
	 *
	 * @param packageName 扫描的包名，如： com.vo
	 *
	 * @return
	 *
	 */
	public static Set<Class<?>> scanZRepositorySubinterface(final String packageName) {

		// FIXME 2024年5月4日 下午2:32:49 zhangzhen: 暂时允许字段中出现sql关键字
//		checkZEntityField(packageName);

		final Set<Class<?>> zrSubclassSet = Sets.newHashSet();
		final Set<Class<?>> clsSet = ClassMap.scanPackage(packageName);
		for (final Class<?> cls : clsSet) {

			final Class<?>[] ia = cls.getInterfaces();
			for (final Class<?> i : ia) {
				final boolean isZRSubclass = i.getCanonicalName().equals(ZRepository.class.getCanonicalName());
				if (isZRSubclass) {
					zrSubclassSet.add(cls);
				}
			}
		}

		return zrSubclassSet;
	}

	public static Set<Class<?>> scanPackage_COM() {
		final Set<String> set = ScanPackage.get();

		if (CollUtil.isEmpty(set)) {
			throw new IllegalArgumentException(ScanPackage.class.getCanonicalName() + " 扫描的包名未设置！");
		}

		final Set<Class<?>> r = Sets.newHashSet();
		for (final String p : set) {
			final Set<Class<?>> clsSet = ClassUtil.scanPackage(p);
			r.addAll(clsSet);
		}
		return r;
	}

	public synchronized static void showSupportedMethod() {
		System.out.println();

		final HashMap<String, HashMap<String, String>> map = MethodRegex.R_M;
		final Set<String> keySet = map.keySet();
		System.out.println("@" + ZRepository.class.getCanonicalName() + " 子接口中支持的声明式方法(共有" + keySet.size() + "个)形式如下：");
		for (final String m : keySet) {
			System.out.println("\t" + m);
		}

		System.out.println();
	}
	/**
	 * 展示出 create table 语句
	 *
	 * @param zrClassSet
	 */
	public synchronized static void showCreateTable(final Set<Class<?>> zrClassSet) {
		final List<Object> zel = extractedZEntity(zrClassSet);
		if (CollUtil.isEmpty(zel)) {
			return;
		}

		for (final Object object : zel) {
			final ZEntity annotation = (ZEntity) ((Class)object).getAnnotation(ZEntity.class);
			final String tableName = annotation.tableName();
			final ZConnection write = ZCPool.getInstance().getZConnection(Mode.WRITE);
			showCreateTable0(tableName, write);
			final ZConnection read = ZCPool.getInstance().getZConnection(Mode.READ);
			showCreateTable0(tableName, read);
		}

	}

	/**
	 * @param tableName
	 * @param zc
	 */
	// FIXME 2024年5月13日 上午12:03:38 zhangzhen: 考虑要做什么功能，在此得到了create table语句了，要不要做比如：
	// 1 校验读写数据源的引擎什么的必须保持一致（引擎似乎没必要一致，比如：写用innodb 读用myisam ）？
	// 2 之后是否做比如：根据 @ZEntity 注解来生成表结果DDL语句的功能，参考create table的返回结果
	private static void showCreateTable0(final String tableName, final ZConnection zc) {

		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);

			final String show = "show create table " + tableName;
			LOG.info("开始从[{}]数据源[{}]语句", zc.getMode(), show);

			final PreparedStatement ps = connection.prepareStatement(show);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final String v = rs.getString(2);
				LOG.info("在[{}]数据源[{}]语句的结果=[{}]", zc.getMode(), show, v);
			}

			SU.close(rs, ps);
		} catch (final SQLException e) {
			e.printStackTrace();
		} finally {
			ZCPool.getInstance().returnZConnectionAndCommit(zc);
		}
	}

	/**
	 * 过滤出带有 @ZEntity 注解的类
	 *
	 * @param zrClassSet
	 * @return
	 */
	private static List<Object> extractedZEntity(final Set<Class<?>> zrClassSet) {
		final List<Object> x = Lists.newArrayList();
		for (final Class<?> class1 : zrClassSet) {
			final String[] typeArray = UserRepositoryTest1.findZRSubclassFanxing(class1);
			final String type = typeArray[0];
			try {
				final Class<?> typeClass = Class.forName(type);
				final ZEntity zEntity = typeClass.getAnnotation(ZEntity.class);
				if (zEntity != null) {
					x.add(typeClass);
				}
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return x;
	}

	// FIXME 2023年9月5日 下午9:46:27 zhanghen: 改为private，并且在第一步scanZR子接口时就校验
	public synchronized static void checkTableExist(final Set<Class<?>> zrClassSet) {
		for (final Class<?> class1 : zrClassSet) {
			final String[] typeArray = UserRepositoryTest1.findZRSubclassFanxing(class1);
			final String type = typeArray[0];
			try {
				final Class<?> typeClass = Class.forName(type);
				final ZEntity zEntity = typeClass.getAnnotation(ZEntity.class);
				if (zEntity == null) {
					final String m = typeClass.getCanonicalName() + " 类缺少" + "@ " + ZEntity.class.getCanonicalName()
							+ " 注解";
					throw new IllegalArgumentException(m);
				}

				checkZEntity_TableNameExist(zEntity.tableName(), ZCPool.getInstance().getZConnection(Mode.WRITE));
				checkZEntity_TableNameExist(zEntity.tableName(), ZCPool.getInstance().getZConnection(Mode.READ));
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	 * @param zrClassSet
	 *
	 * @return
	 */
	public static List<SqlResult> generateSqlForZRSubclass(final Set<Class<?>> zrClassSet) {

		LOG.info("ZRepositoryStarter开始生成[{}]的子接口的SQL模板", ZRepository.class.getCanonicalName());

		final List<SqlResult> sqlResultlist = Lists.newArrayList();

		for (final Class<?> zrSubClass : zrClassSet) {
			LOG.info("ZRepositoryStarter开始生成[{}]的SQL模板", zrSubClass.getCanonicalName());
			final String[] typeArray = UserRepositoryTest1.findZRSubclassFanxing(zrSubClass);

			final Method[] ms = zrSubClass.getMethods();

			for (final Method m : ms) {
				LOG.info("ZRepositoryStarter开始生成[{}]的方法[{}]的SQL模板", zrSubClass.getCanonicalName(), m.getName());

				final MethodSQL methodSQL = MethodRegex.check(m.getName(), m);
//				final Entry<String, String> check = MethodRegex.check(m.getName(), m);

				final String type = typeArray[0];
				try {
					final Class<?> typeClass = Class.forName(type);
					final ZEntity zEntity = typeClass.getAnnotation(ZEntity.class);

					checkZEntity(typeClass);

					final String zrSubClassName = zrSubClass.getCanonicalName();
					final String methodName = m.getName();
					final String sqlTemplate = methodSQL.getSqlTemplate();

					final String tableName = zEntity.tableName();
					final String sqlTemplateTemp = sqlTemplate.replace(TABLE_NAME, tableName);

					final String sqlFinal = checkMethodName(typeClass, methodName, sqlTemplateTemp, methodSQL, zrSubClass);

					final SqlResult result = new SqlResult(zrSubClassName, methodName, sqlFinal);

					sqlResultlist.add(result);

				} catch (final ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return sqlResultlist;
	}

	/**
	 * 校验 @ZEntity 指定的tableName是否存在
	 *
	 * @param tableName
	 * @param zc TODO
	 *
	 */
	private static void checkZEntity_TableNameExist(final String tableName, final ZConnection zc) {

		ResultSet rs = null;
		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
			final DatabaseMetaData metaData = connection.getMetaData();
			rs = metaData.getTables(null, null, tableName, null);
			if (!rs.next()) {
				throw new IllegalArgumentException(
						ZEntity.class.getSimpleName() + " 指定的tableName不存在，tableName = " + tableName);
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			ZCPool.getInstance().returnZConnectionAndCommit(zc);
			if (rs != null) {
				try {
					rs.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 校验一下方法声明是否正确，是否符合命名规则，见MethodRegex.GROUP_ 开头的常量正则表达式
	 *
	 * @param typeClass  @ZEntity标记的类
	 * @param methodName ZRepository 子类中的自定义的findByXX的方法名称,如findByUserId
	 * @param sql        sql模板，如：select * from user where @ = ?
	 * @param methodSQL TODO
	 * @param zrClass ZRepository 的用户自定义的子接口
	 * @return 返回可用于java.sql.PreparedStatement 的SQL语句， 如 : select * from user where
	 *         id = ?
	 *
	 */
	private static String checkMethodName(final Class<?> typeClass, final String methodName, final String sql,
			final MethodSQL methodSQL, final Class zrClass) {
		if (methodSQL.isZQuery()) {
			return methodSQL.getSqlTemplate();
		}

		// methodName 从每个大写字母分开分成一个数组，如findByUserId 分成[find, By, User, Id]
		final List<String> fnLIst = getDeclaredFieldName(typeClass);

		// findByUserId 分成[find, By, User, Id] ，从前往后计算是否sql关键字，否则按照entity字段处理

		final HashSet<String> sqlKeyword = SqlPattern.SQL_KEYWORD;

		// SQL关键字按从长到短排序，防止出现 Or优先于Order被替换掉，剩余 der
		final ArrayList<String> skList = Lists.newArrayList(sqlKeyword);
		skList.sort(Comparator.comparing(String::length).reversed());

		final D d = findFieldName(typeClass, methodName);
		String sKeyword = d.getSqlKeyword();
		for (final String sk : skList) {
			sKeyword = sKeyword.replace(sk, EMTPY);
		}

		// 只剩SQL关键字的方法名称替换掉所有的SQL关键字后，必须是""
		if (!EMTPY.equals(sKeyword)) {
			// FIXME 2023年8月26日 下午5:50:54 zhanghen: TODO 提示信息再详细一点
			throw new IllegalArgumentException(ZRepository.class.getSimpleName() + " 子类自定义方法声明错误，methodName = "
					+ methodName + "，" + "请确认方法名由SQL关键字和@ZEntity类中的字段组成，" + "方法名称命名规则见 "
					+ MethodRegex.class.getSimpleName() + " 中以 GROUP_ 开头的常量。");
		}

		final List<String> fieldNameArray = d.getFiledName();

		final List<String> fieldNameList = Lists.newArrayList(fieldNameArray)
				.stream()
				.filter(StrUtil::isNotBlank)
				.map(x -> x.length() == 1 ? x.toLowerCase() : Character.toLowerCase(x.charAt(0)) + x.substring(1))
				.collect(Collectors.toList());


		for (final String fn : fieldNameList) {
			final Optional<String> findAny = fnLIst.stream().filter(f1 -> f1.equalsIgnoreCase(fn)).findAny();
			if (!findAny.isPresent()) {
				// FIXME 2023年8月26日 下午5:50:54 zhanghen: TODO 提示信息再详细一点
				throw new IllegalArgumentException(ZRepository.class.getSimpleName() + " 子类自定义方法声明错误，methodName = " + methodName + "，"
						+  "请确认方法名由SQL关键字和@ZEntity类中的字段组成，"
						+  "方法名称命名规则见 " + MethodRegex.class.getSimpleName() + " 中以 GROUP_ 开头的常量。"
						);
			}
		}

		final List<Method> ml = Arrays.stream(zrClass.getMethods()).filter(m -> m.getName().equals(methodName))
				.collect(Collectors.toList());
		if (ml.size() > 1) {
			throw new IllegalArgumentException(
					"@" + ZRepository.class.getCanonicalName() + " 类 " + typeClass.getSimpleName()
							+ " 有重复的方法 [" + ml + "] ，不允许重名！"
			);
		}

		final Method method = ml.get(0);
		final Parameter[] ps = method.getParameters();

		String sqlA = sql;

		// FIXME 2024年5月18日 上午10:02:04 zhangzhen: debug 代码，记得删除
		if("findByDateAndNameOrderByNameDescLimit".equals(method.getName())) {
			final int x2 = 1;
		}
		if (isZRClassMethod(method) || MethodRegex.isMethod_ANALYSIS_BY_ZENTITY_FIELD(method)) {
			final List<String> filedNameMethodNameOrder = d.getFiledNameMethodNameOrder();
			for (final String fieldName : filedNameMethodNameOrder) {
				final String dbColumnName = ZFieldConverter.toDbField(fieldName);
				sqlA = sqlA.replaceFirst("@", dbColumnName);
			}
		} else if (MethodRegex.isMethod_ANALYSIS_BY_METHOD_PARAMETERS(method)) {
			for (final Parameter p : ps) {
				final String dbColumnName = ZFieldConverter.toDbField(p.getName());
				sqlA = sqlA.replaceFirst("@", dbColumnName);
			}
		}

		// 仍然包含 @，则说明参数和字段数目对不上
 		if (sqlA.contains("@")) {
			throw new IllegalArgumentException("请检查自定义方法名称，methodName = " + methodName);
		}

		return sqlA;
	}

	private static boolean isZRClassMethod(final Method method) {
		final Method[] ms = ZRepository.class.getDeclaredMethods();
		for (final Method method2 : ms) {
			if(method.equals(method2)) {
				return true;
			}
		}

		return false;
	}

	private static List<String> getDeclaredFieldName(final Class<?> typeClass) {
		final Field[] fs = typeClass.getDeclaredFields();
		final List<String> fieldNameList = Arrays.asList(fs).stream().map(Field::getName).collect(Collectors.toList());
		return fieldNameList;
	}

	/**
	 * 把一个自定义方法名按大写字母拆开，拆成一个数组。如findByUserId拆分成[find, By, User, Id]
	 *
	 * @param zRepositoryMethodName
	 * @return
	 *
	 */
	private static List<String> splitMethodNameToArray(final String zRepositoryMethodName) {
		if (StrUtil.isEmpty(zRepositoryMethodName)) {
			return Collections.emptyList();
		}

		final char[] ch = zRepositoryMethodName.toCharArray();
		final List<String> aL = Lists.newArrayList();
		int from = 0;
		for (int i = 1; i < ch.length; i++) {
			final boolean isDaxie = SqlPattern.daxie.contains(ch[i]);
			if (isDaxie) {
				final String temp = zRepositoryMethodName.substring(from, i);
//				System.out.println("temp = " + temp);
				aL.add(temp);
				from = i;
			}
			if (i == (ch.length - 1)) {
				final String temp2 = zRepositoryMethodName.substring(from,  ch.length);
//				System.out.println("temp2 = " + temp2);
				aL.add(temp2);
			}

		}

		return aL;
	}


	/**
	 * 用指定的 className 生成一个 ZRepository 的实现类
	 *
	 * @return
	 *
	 */
	private static ZClass generateMyZRepositorySubclass(final Class<?> myZRClass) {

		final ZClass zClass = new ZClass();

		// FIXME 2023年8月26日 下午5:54:25 zhanghen: com.vo改为配置项
		zClass.setPackage1(new ZPackage("com"));

		//		userR.setImplementsSet(Sets.newHashSet(canonicalName + " <T, ID> "));
//		userR.setName(canonicalName + "_ZClass" + "<T,ID>");

		zClass.setImportSet(Sets.newHashSet(myZRClass.getName()));
		zClass.setImplementsSet(Sets.newHashSet(myZRClass.getSimpleName()));
		zClass.setName(myZRClass.getSimpleName() + _Z_CLASS);


		final String[] typeArray = UserRepositoryTest1.findZRSubclassFanxing(myZRClass);

		final ZField zField = new ZField("Class<" + typeArray[0] + ">", "classType",typeArray[0] + ".class");

		zClass.setFieldSet(Sets.newHashSet(zField));

		final Set<ZMethod> zmSet = new HashSet<>();

		final Set<ZMethod> zmSet1 = addZMethod(myZRClass);
		zmSet.addAll(zmSet1);


		zClass.setMethodSet(zmSet);
		return zClass;
	}

	private static Set<ZMethod> addZMethod(final Class<?> myZRClass) {
		final Method[] ms = myZRClass.getMethods();

		final String[] typeArray = UserRepositoryTest1.findZRSubclassFanxing(myZRClass);
//		System.out.println("typeArray = " + Arrays.toString(typeArray));

		final Set<ZMethod> zmSet = new HashSet<>();
		for (final Method method : ms) {

			final Class classType = getClassType(method);

			// FIXME 2024年5月19日 下午6:13:44 zhangzhen: debug 代码记得删除
			if("findByNameAndId".equals(method.getName())) {
				final int x2=1;
			}

			final ZWrite write = method.getAnnotation(ZWrite.class);
			final ZRead read = method.getAnnotation(ZRead.class);
			if ((write != null) && (read != null)) {
				throw new IllegalArgumentException("方法[" + method.getName() + "] 不能同时存在 "
						+ ZWrite.class.getCanonicalName() + " 和 " + ZRead.class.getCanonicalName());
			}

			final ZMethod zm = new ZMethod();
			zm.setAbstract(false);
			zm.setName(method.getName());
			final Class<?> returnType = method.getReturnType();
			zm.setReturnType(returnType.getCanonicalName());
//			System.out.println("returnType.getCanonicalName() = " + returnType.getCanonicalName());
			if (returnType.getCanonicalName().equals(Object.class.getCanonicalName())) {
				zm.setReturnType(typeArray[0]);
			}

			final ArrayList<ZMethodArg> argLIst = Lists.newArrayList();

			final Parameter[] parameters = method.getParameters();
			for (final Parameter p1 : parameters) {

				if ("id".equals(p1.getName())) {
					argLIst.add(new ZMethodArg(typeArray[1], p1.getName()));
					continue;
				}

				if ("save".equals(method.getName())) {
					if (p1.getType().getCanonicalName().equals(Object.class.getCanonicalName())) {
						argLIst.add(new ZMethodArg(typeArray[0], p1.getName()));
					}
				} else {
					argLIst.add(new ZMethodArg(p1.getType(), p1.getName()));
				}
			}

			zm.setgReturn(false);
			zm.setMethodArgList(argLIst);
			// FIXME 2023年9月6日 下午3:16:13 zhanghen: Save 方法加入sync关键字，因为它有save和findBYid两个方法组成


			zmSet.add(zm);
			final String sql = ZRSqlMap.get(myZRClass.getCanonicalName(), zm.getName());

			// 结束
			final String body = "String sql = \""+sql+"\";";

			final String body2 = EMTPY;
			final String entityT = typeArray[0];
			final String methodS = getSuMethod(method.getName(), method, entityT);

			zm.setBody("\t" +body + "\n\t" + body2  + "\n\t" + methodS);
		}
		return zmSet;
	}

	static String getSuMethod(final String methodName, final Method method, final String entityTName) {

		final String modeString = modeString(method);

		switch (method.getName()) {
		case "findById":
			return "return " + SU.class.getCanonicalName() + ".findById(" + modeString + ", id,classType,sql);";

		case "findByIdIn":
			return "return " + SU.class.getCanonicalName() + ".findByIdIn(" + modeString + " ,idList,classType,sql);";

		case "findAll":
			return "return " + SU.class.getCanonicalName() + ".findAll(" + modeString + ", classType,sql);";

		case "saveAll":
			return "return " + SU.class.getCanonicalName() + ".saveAll(" + modeString + ", classType,sql,tList);";

		case "update":
			return "return " + SU.class.getCanonicalName() + ".update(" + modeString + ", classType,t,sql);";

		case "save":
			return "return " + SU.class.getCanonicalName() + ".save(" + modeString + ", classType," + entityTName
					+ ",t,sql);";

		case "page":
			return "return " + SU.class.getCanonicalName() + ".page(" + modeString + ", classType," + entityTName
					+ ",t,sort,sql,size,page);";

		case "existById":
			return "return " + SU.class.getCanonicalName() + ".existById(" + modeString + ", id,classType,sql);";

		case "deleteById":
			return "return " + SU.class.getCanonicalName() + ".deleteById(" + modeString + ", id,classType,sql);";

		case "deleteByIdIn":
			return "return " + SU.class.getCanonicalName() + ".deleteByIdIn(" + modeString + ", idList,classType,sql);";

		case "deleteAll":
			return "return " + SU.class.getCanonicalName() + ".deleteAll(" + modeString + ", classType,sql);";

		default:

			// default  ZR的子类声明的方法
			final MethodSQL methodSQL = MethodRegex.check(methodName, method);


//			final Entry<String, String> check = MethodRegex.check(methodName, method);
//			System.out.println("getSuMethod-check = " + check);
			final String methodname = methodSQL.getMethodName();
			// FIXME 2024年5月17日 上午2:11:27 zhangzhen: debug 代码，记得删除
			if("pageByNameOrderById".equals(method.getName())) {
				final int x =1;
			}

			if (methodname.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXDescLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXOrderByXXDescLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXAndXXAndXXOrderByXXDescLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXAndXXOrderByXXDescLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXOrderByXXDescLimit)
					|| methodname.matches(MethodRegex.GROUP_findByXXOrderByXXDescLimit)) {
				return gROUP_findByXXOrderByXXDescLimit(method);
			}

			if (methodname.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXOrderByXXLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXAndXXAndXXOrderByXXLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXAndXXOrderByXXLimit)
					|| methodname.matches(MethodRegex.findByXXAndXXOrderByXXLimit)
					|| methodname.matches(MethodRegex.GROUP_findByXXOrderByXXLimit)) {
				return gROUP_findByXXOrderByXXLimit(method, methodname);
			}

			if (methodname.matches(MethodRegex.GROUP_findByXXXEndingWith)) {
				return gROUP_findByXXXEndingWith(method);
			}

			if (methodname.matches(MethodRegex.GROUP_findByXXXStartingWith)) {
				return gROUP_findByXXXStartingWith(method);
			}

			if (	methodname.matches(MethodRegex.GROUP_findByXXGreaterThanEquals)
				 || methodname.matches(MethodRegex.GROUP_findByXXGreaterThan)
				 || methodname.matches(MethodRegex.GROUP_findByXXLessThanEquals)
				 || methodname.matches(MethodRegex.GROUP_findByXXLessThan)
					) {
				return gROUP_findByXXGreaterThanEquals(method);
			}

			if (methodname.matches(MethodRegex.findByXXBetween)) {
				return gROUP_findByXXBetween(method);
			}

			if (methodname.matches(MethodRegex.GROUP_findByxx_in)) {
				return gROUP_findByxx_in(method);
			}

			if (methodname.matches(MethodRegex.countingByXXXAndXXAndXXAndXX)
					|| methodname.matches(MethodRegex.countingByXXXAndXXAndXX)
					|| methodname.matches(MethodRegex.countingByXXXAndXX)
					|| methodname.matches(MethodRegex.GROUP_CountingByXXX)) {
				return gROUP_CountingByXXX(method, methodname);
			}

			if (methodname.matches(MethodRegex.GROUP_findByxxNotNull)) {
				return GROUP_findByXXNotNull(method);
			}

			if (methodname.matches(MethodRegex.GROUP_findByXXIsNull)) {
				final Class returnType = getClassType(method);
				return "return " + SU.class.getCanonicalName() + ".findByXXIsNull(" + modeString + ",classType,"
						+ returnType.getCanonicalName() + ",sql);";
			}

			if (methodname.matches(MethodRegex.GROUP_findByXXLike)) {
				return gROUP_findByXXLike(method);
			}
			if (methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYYOrYY)
					|| methodname.matches(MethodRegex.findByXXOrYY)) {
				return gROUP_findByXXOrYY(method);
			}

			if (methodname.matches(MethodRegex.GROUP_count)) {
				return "return " + SU.class.getCanonicalName() + ".count(" + modeString + ",classType,sql);";
			}

			// 最短的排最后
			if (methodname.matches(MethodRegex.GROUP_findByXXAndXX) || methodname.matches(MethodRegex.GROUP_findByXX)) {
				return gROUP_findByXX(method);
			}

			// 最后面是@ZQuery自定义方法
			if (methodSQL.isZQuery()) {
				final String sqlTemplate = methodSQL.getSqlTemplate();
				final String x = zQuery(method, methodSQL.getSqlTemplate());
				return x;
			}

			break;
		}

		return EMTPY;
	}

	/**
	 * 返回声明方法是 @ZRead 还是 @ZWrite ，无默认为 @ZWrite
	 *
	 *
	 * @param method
	 * @return
	 */
	private static String modeString(final Method method) {
		if (method.isAnnotationPresent(ZRead.class)) {
			return Mode.class.getCanonicalName() + "." + Mode.READ.name();
		}
		return Mode.class.getCanonicalName() + "." + Mode.WRITE.name();
	}

	private static String gROUP_pageByXX_orderByXX(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final String r = "return " + SU.class.getCanonicalName() + ".pageByXXOrderByXX(" + modeString + ",classType,sql,"
		+ joiner.toString() + ");";
		return r;
	}

	private static String gROUP_findByXXXEndingWith(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class returnType = getClassType(method);
		return "return " + SU.class.getCanonicalName() + ".findByXXXEndingWith(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String gROUP_findByXXXStartingWith(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);

		final Class returnType = getClassType(method);
		final String modeString = modeString(method);
		return "return " + SU.class.getCanonicalName() + ".findByXXXStartingWith(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String gROUP_findByXXGreaterThanEquals(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);

		final Class returnType = getClassType(method);
		final String modeString = modeString(method);
		return "return " + SU.class.getCanonicalName() + ".findByIdLessThan(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String gROUP_findByXXOrderByXXLimit(final Method method, final String key) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class returnType = getClassType(method);
		final String r = "return " + SU.class.getCanonicalName() + ".findByXXOrderByXXLimit(" + modeString
				+ ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
		return r;
	}

	private static String gROUP_findByXXOrderByXXDescLimit(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class returnType = getClassType(method);
		return "return " + SU.class.getCanonicalName() + ".findByXXOrderByXXLimit(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String gROUP_CountingByXXX(final Method method, final String key) {

		// FIXME 2024年5月17日 上午2:11:27 zhangzhen: debug 代码，记得删除
		if("countingByContent".equals(method.getName())) {
			final int x =1;
		}

		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}
		final String modeString = modeString(method);

		final int ac = StrUtil.count(joiner.toString(), DELIMITER);

		// FIXME 2024年5月18日 下午3:32:25 zhangzhen: byte[] 类型引起的问题 : 很多方法都有此问题，都要好好再测试byte[] 类型
		// countBy多个条件的不能把countByXX单个的参数改为Object... a 然后复用，因为一个条件并且为byte[]类型的话，a会被认为是byte[]
		// 而不是a.length=1并且这唯一的值是一个byte[]

		final String suMethodName =
				ac == 0 ? "countingByXX" : "countingByXXAndXX";
		return "return " + SU.class.getCanonicalName() + "." + suMethodName + "(" + modeString + ",classType,sql,"
				+ joiner.toString() + ");";
	}

	private static String gROUP_findByXXOrYY(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class returnType = getClassType(method);

		final String r = "return " + SU.class.getCanonicalName() + ".findByXXOrYY(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
		return r;
	}

	private static String GROUP_findByXXNotNull(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class returnType = getClassType(method);

		final String r = "return " + SU.class.getCanonicalName() + ".findByXXNotNull(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
		return r;
	}

	private static String gROUP_findByXXLike(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);

		final Class returnType = getClassType(method);

		return "return " + SU.class.getCanonicalName() + ".findByXXLike(" + modeString + ",classType,"
				+ returnType.getName() + ",sql," + joiner.toString() + ");";
	}

	private static String gROUP_findByXXBetween(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final Class returnType = getClassType(method);

		final String modeString = modeString(method);
		final String r = "return " + SU.class.getCanonicalName() + ".findByXXBetween(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
		return r;
	}

	private static String gROUP_findByxx_in(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class returnType = getClassType(method);
		final String r = "return " + SU.class.getCanonicalName() + ".findByXXIn(" + modeString + ",classType,"
				+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
		return r;
	}

	private static String zQuery(final Method method, final String sqlTemplate) {
		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}

        final Class classType = getClassType(method);

		final String modeString = modeString(method);

		final String u = sqlTemplate.trim().toUpperCase();
		String subClassMethodName = null ;
		if (u.startsWith("SELECT")) {
			subClassMethodName = "zQuerySelect";
			checkZQuerySelect(method, sqlTemplate);
		} else if (u.startsWith("UPDATE")) {
			subClassMethodName = "zQueryUpdate";
		} else if (u.startsWith("DELETE")) {
			subClassMethodName = "zQueryDelete";
		} else if (u.startsWith("INSERT")) {
			subClassMethodName = "zQueryInsert";
		} else {
			throw new IllegalArgumentException(
					"@" + ZQuery.class.getSimpleName() + " 只支持 SELECT/UPDATE/DELETE/INSERT 语句");
		}

		final String r = "return " + SU.class.getCanonicalName() + "." + subClassMethodName + "(" + modeString + DELIMITER
				+ classType.getCanonicalName() + ",sql," + joiner.toString() + ");";
		return r;
	}

	/**
	 * 校验 select 语句中的 ?占位符，必须组为一个数组排序后符合 ?1 ?2 ?3 的顺序
	 *
	 * @param method
	 * @param sqlTemplate
	 * @return
	 */
	public static int[] checkZQuerySelect(final Method method, final String sqlTemplate) {

		if (method.getParameterAnnotations().length <= 0) {
			return null;
		}

		final String k = method.getName() + "@" + sqlTemplate;
		synchronized (k.intern()) {

			final int[] v = C.get(k);
			if(v !=null) {
				return v;
			}

			return checkZQuerySelect0(method, sqlTemplate, k);
		}


	}

	private static int[] checkZQuerySelect0(final Method method, final String sqlTemplate, final String k) {
		final String regex = "\\?(\\d+)";
		final Pattern pattern = Pattern.compile(regex);
		final java.util.regex.Matcher matcher = pattern.matcher(sqlTemplate);
		final int[] argOrderArray = new int[method.getParameters().length];

		int i = 0;
		boolean find = false;
		while (matcher.find()) {
			find = true;
			final String a = matcher.group(1);
			argOrderArray[i] = Integer.parseInt(a);
			i++;
		}

		if (!find) {
			throw new IllegalArgumentException(
					"@" + ZQuery.class.getCanonicalName() + " 方法"
							+ " ["
							+ method.getName()
							+ "] "
							+ "的自定义sql - [" + sqlTemplate + "] 中的?占位符必须符合 [?从1开始递增的数字] 的模式，如 ?1 ?2 ?3 ");
		}
		System.out.println("a = " + Arrays.toString(argOrderArray));
		System.out.println("method.name = " + method.getName());

		Arrays.sort(argOrderArray);
		for (int ix = 0; ix < argOrderArray.length; ix++) {
			if (argOrderArray[ix] != (ix + 1)) {
				throw new IllegalArgumentException("@" + ZQuery.class.getCanonicalName()
						+ " 方法"
						+ " ["
						+ method.getName()
						+ "] "
						+ "自定义sql - [" + sqlTemplate
						+ "] 中的 ?占位符 必须从1开始依次递增 "
						) ;
			}
		}

		C.put(k, argOrderArray);
		return argOrderArray;
	}

	private static final WeakHashMap<String, int[]> C = new WeakHashMap<>();

	private static Class getClassType(final Method method) {

		final Type returnType = method.getGenericReturnType();
		// 如果有泛型参数，如List<MyEntity>
		if (returnType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) returnType;
            final Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                final Type typeArgument = typeArguments[0];
                if (typeArgument instanceof Class) {
                    final Class<?> typeClass = (Class<?>) typeArgument;
                    return typeClass;
                }
            }
        }

		return method.getReturnType();
	}

	private static String gROUP_findByXX(final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);

		final Class returnType = getClassType(method);

		final String methodName = StrUtil.count(joiner.toString(), DELIMITER) == 0 ? "findByXX" : "findByXXAndXX";

		return "return " + SU.class.getCanonicalName() + "." + methodName + "(" + modeString + ",classType,"+returnType.getCanonicalName()+",sql,"
				+ joiner.toString() + ");";
	}

	/**
	 * 校验 @ZEntity 中的字段，如：不允许出现SQL关键字等等
	 * @param packageName TODO
	 *
	 * @return 返回 @ZEntity 的类
	 *
	 */
	private static Set<Class<?>> checkZEntityField(final String packageName) {
		final Set<Class<?>> zeSet = Sets.newHashSet();
		final Set<Class<?>> clsSet = ClassMap.scanPackage(packageName);
		for (final Class<?> c : clsSet) {
			final boolean isZE = c.isAnnotationPresent(ZEntity.class);
			if (!isZE) {
				continue;
			}

			final Field[] fs = c.getDeclaredFields();
			for (final Field f : fs) {
				final String fName = f.getName();
				final boolean sqlKeyword = SqlPattern.isSqlKeyword(fName);
				if (sqlKeyword) {
					throw new IllegalArgumentException(ZEntity.class.getSimpleName() + " 类中字段不允许出现SQL关键字," +ZEntity.class.getSimpleName()+" 类 = " +
							c.getSimpleName() + ",filed = " + fName);
				}

			}

			zeSet.add(c);
		}

		return zeSet;
	}

	/**
	 * 校验 @ZEntity 标记的类： 必须有 @ZID 注解并且字段是主键、必须每个字段类型和名称都和数据表中匹配 等等
	 *
	 * @param typeClass
	 *
	 */
	private synchronized static void checkZEntity(final Class<?> typeClass) {

		if (cc.contains(typeClass)) {
			return;
		}
		cc.add(typeClass);

		final ZEntity ze = typeClass.getAnnotation(ZEntity.class);
		if (ze == null) {
			return;
		}

		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZRMain.checkZEntityZID()");

		final Field[] fs = typeClass.getDeclaredFields();
		final List<Field> zidList = Lists.newArrayList(fs).stream().filter(f -> f.isAnnotationPresent(ZID.class))
				.collect(Collectors.toList());

		if (CollUtil.isEmpty(zidList)) {
			throw new IllegalArgumentException(ZEntity.class.getSimpleName() + " 类型 " + typeClass.getSimpleName()
					+ " 必须有 " + ZID.class.getSimpleName() + " 字段");
		}

		if (zidList.size() != 1) {
			final String fsA = zidList.stream().map(Field::getName).collect(Collectors.joining(DELIMITER));
			throw new IllegalArgumentException(ZEntity.class.getSimpleName() + " 类型 " + typeClass.getSimpleName()
					+ " 只能有一个 " + ZID.class.getSimpleName() + " 字段，现有有两个：" + fsA);
		}

		final String name = zidList.get(0).getName();

		checkZEntityPrimaryKey(typeClass, name, ZCPool.getInstance().getZConnection(Mode.WRITE));
		checkZEntityPrimaryKey(typeClass, name, ZCPool.getInstance().getZConnection(Mode.READ));

		checkZEntityFiled(typeClass, ZCPool.getInstance().getZConnection(Mode.WRITE));
		checkZEntityFiled(typeClass, ZCPool.getInstance().getZConnection(Mode.READ));

	}

	/**
	 * 校验 @ZEntity 类里的属性，必须和表的列名和类型匹配
	 *
	 * @param typeClass
	 * @param zConnection
	 *
	 */
	private static void checkZEntityFiled(final Class<?> typeClass, final ZConnection zConnection) {

		final String tableName = typeClass.getAnnotation(ZEntity.class).tableName();

		final Field[] fs = typeClass.getDeclaredFields();
		final List<Field> fieldList = Lists.newArrayList(fs);

		final Optional<Field> notSupport = fieldList.stream().filter(f -> !DBType.typeSupport(f.getType().getCanonicalName())).findAny();
		if (notSupport.isPresent()) {
			final String m = "@" + ZEntity.class.getSimpleName() + "类[" + typeClass.getSimpleName() + "] 中的字段 ["
					+ notSupport.get().getName()
					+ "] 的类型 ["
					+ notSupport.get().getType().getCanonicalName()
					+ "] 不支持"
					;
			throw new IllegalArgumentException(m);
		}

		final Optional<Field> findAny = fieldList.stream()
				.filter(fn -> DBType.JAVA.contains(fn.getType().getCanonicalName())).findAny();
		if (findAny.isPresent()) {
			final String m = "@" + ZEntity.class.getSimpleName() + "类[" + typeClass.getSimpleName() + "] 中的字段 ["
					+ findAny.get().getName() + "] 不允许使用基本类型 ："
					+ "" + findAny.get().getType().getCanonicalName() + " " + findAny.get().getName()
					+ " 请改为引用类型"
					;
			throw new IllegalArgumentException(m);
		}

		final Connection connection = zConnection.getConnection();
		try {
			connection.setAutoCommit(false);
			final DatabaseMetaData metaData = connection.getMetaData();

			final String catalog = findCatalog(zConnection.getUrl());
			try (ResultSet columns = metaData.getColumns(catalog, null, tableName, null)) {
				System.out.println("开始校验[" + zConnection.getMode().name() + "]数据表 = " + tableName);

				int columnsCount = 0;
				final List<String> columnNameList = Lists.newArrayList();

				while (columns.next()) {
					columnsCount++;
					final String columnName = columns.getString(COLUMN_NAME);
					columnNameList.add(columnName);
					final String columnType = columns.getString(TYPE_NAME);

					final String javaFieldName = ZFieldConverter.toJavaField(columnName);
					final Optional<Field> o = fieldList.stream().filter(fn -> fn.getName().equals(javaFieldName)).findFirst();

					if (!o.isPresent()) {
						final String m = "数据表[" + tableName + "]中存在" + "@" + ZEntity.class.getSimpleName() + "类["
								+ typeClass.getSimpleName() + "]不存在的字段" + columnName;
						throw new IllegalArgumentException(m);
					}

					final boolean match = DBType.match(o.get().getType().getCanonicalName(), columnType);
					if (!match) {
						final String m = "@" + ZEntity.class.getSimpleName() + "类[" + typeClass.getSimpleName()
								+ "]中的字段[" + o.get().getName() + "] 类型 [" + o.get().getType().getCanonicalName()
								+ "] 与数据表[" + tableName + "] 的字段 [" + columnName + "] 类型 [" + columnType + "] 不匹配";
						throw new IllegalArgumentException(m);
					}
				}

				final List<String> fieldNameList = Arrays.stream(fs).map(Field::getName)
						.collect(Collectors.toList());

				if (fieldNameList.size() != columnsCount) {
					final List<String> cnl = columnNameList.stream().map(ZFieldConverter::toJavaField)
							.collect(Collectors.toList());

					if (fieldNameList.size() > columnNameList.size()) {
						fieldNameList.removeAll(cnl);

						final List<Field> c = fieldList.stream().filter(f -> fieldNameList.contains(f.getName()))
								.collect(Collectors.toList());

						final List<Field> noZTransientFieldList = c.stream().filter(f -> !f.isAnnotationPresent(ZTransient.class)).collect(Collectors.toList());

						if (noZTransientFieldList.size() > 0) {
							final String m = "@" + ZEntity.class
									.getSimpleName() + "类[" + typeClass.getSimpleName()
									+ "]中存在数据表[" + tableName + "]中不存在的字段" + noZTransientFieldList + EMTPY
									+ "，如需与数据表字段对应，请在数据表中加入此字段；如不需与数据表对应，请在字段上加入@" + ZTransient.class.getCanonicalName() + " 注解"
									;
							throw new IllegalArgumentException(m);
						}

					} else if (fieldNameList.size() < columnNameList.size()) {

						cnl.removeAll(fieldNameList);
						final String m = "数据表[" + tableName + "]中存在" + "@" + ZEntity.class.getSimpleName() + "类["
								+ typeClass.getSimpleName() + "]不存在的字段" + cnl + EMTPY;

						throw new IllegalArgumentException(m);
					}

				}
			}

		} catch (final SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			ZCPool.getInstance().returnZConnectionAndCommit(zConnection);
		}

	}


	private static void checkPrimaryKey(final Class<?> typeClass, final String name, final DatabaseMetaData metaDataREAD) {

		ResultSet primaryKeys = null;

		try {

			final String tableName = typeClass.getAnnotation(ZEntity.class).tableName();
			primaryKeys = metaDataREAD.getPrimaryKeys(null, null, tableName);
			if (!primaryKeys.next()) {
				throw new IllegalArgumentException(ZEntity.class.getSimpleName() + " 类型 " + typeClass.getSimpleName()
						+ " " + ZID.class.getSimpleName() + " 字段 " + name + " 在数据库中不存在");
			}

			final String columnName = primaryKeys.getString(COLUMN_NAME);
			if (!Objects.equals(name, columnName)) {
				throw new IllegalArgumentException(ZEntity.class.getSimpleName() + " 类型 " + typeClass.getSimpleName()
						+ " " + ZID.class.getSimpleName() + " 字段名称与数据库主键名称不一致，" + ZID.class.getSimpleName() + " 名称："
						+ name + "，数据库主键名称：" + columnName);
			}

		} catch (final SQLException e) {
			e.printStackTrace();
		} finally {
			if (primaryKeys != null) {
				try {
					primaryKeys.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}

		}
	}

	static HashSet<Class> cc = Sets.newHashSet();

	private static void checkZEntityPrimaryKey(final Class<?> typeClass, final String name, final ZConnection zConnection) {
		final Connection connection = zConnection.getConnection();
		try {
			connection.setAutoCommit(false);

			final DatabaseMetaData metaData = connection.getMetaData();

			checkPrimaryKey(typeClass, name, metaData);
		} catch (final SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			ZCPool.getInstance().returnZConnectionAndCommit(zConnection);
		}
	}

	private static D findFieldName(final Class<?> typeClass, final String methodName) {

		final D d = new D();
		d.setMethodName(methodName);

		String m = methodName;
		final Field[] fs = typeClass.getDeclaredFields();

		// 现在问题:ZEntity 有字段 time 和 timestamp ，在此会处理 findBystamp，因为time在前。
		// 但显然不能要求用户自定义字段的顺序必须怎样，所以在此重排字段顺序
		// fs name.length 排序看是否解决问题
		Arrays.sort(fs, (o1, o2) -> {
			final String name1 = o1.getName();
			final String name2 = o2.getName();
			final int v = Integer.compare(name2.length(), name1.length());
			return v;
		});

		for (final Field f : fs) {
			final String name = f.getName();

			final String upFieldName = String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);

			if (m.contains(upFieldName)) {
				d.addFiledName(name);
			}

			m = m.replace(upFieldName, EMTPY);
		}

		d.setSqlKeyword(m);

		final ArrayList<String> filedNameOriginalOrder = Lists.newArrayList();
		final List<String> fn = d.getFiledName();
		final Field[] fa = typeClass.getDeclaredFields();
		for (final Field element : fa) {
			final Optional<String> o = fn.stream().filter(f -> f.equals(element.getName())).findFirst();
			if(!o.isPresent()) {
				continue;
			}
			filedNameOriginalOrder.add(o.get());
		}

		d.setFiledNameOriginalOrder(filedNameOriginalOrder);

		// FIXME 2024年5月19日 上午7:48:57 zhangzhen: debug 代码
		if("findByDateAndNameOrderByNameDescLimit".equals(methodName)) {
			final int x2 = 1;
		}
		final List<String> fieldNameArray = getFieldNameArray(methodName, fs);
		d.setFiledNameMethodNameOrder(fieldNameArray);

		return d;
	}

	private static List<String> getFieldNameArray(final String methodName,final Field[] fs) {

		final ArrayList<Field> fl = Lists.newArrayList(fs);
		final List<String> fnl = fl.stream().map(Field::getName).map(n -> String.valueOf(n.charAt(0)).toUpperCase() + n.substring(1))
		.collect(Collectors.toList());

		final HashSet<String> sqlKeyword = SqlPattern.SQL_KEYWORD;
		// SQL关键字按从长到短排序，防止出现 Or优先于Order被替换掉，剩余 der
		final ArrayList<String> skList = Lists.newArrayList(sqlKeyword);
		skList.sort(Comparator.comparing(String::length).reversed());

		final String ffN= methodName;

		if("findByDateAndNameOrderByNameDescLimit".equals(methodName)) {
			final int x2 = 1;
		}

		final ArrayList<String> x = Lists.newArrayList();
		for (final String fn : fnl) {
			int from = 0;
			while (from< ffN.length()) {
				final int i = ffN.indexOf(fn,from);
				if (i < 0) {
					break;
				}

				final String one = ffN.substring(i, i + fn.length());
				x.add(one);

				from = i + fn.length();
			}
		}

		x.sort((s1, s2) -> {
			final int i1 = methodName.indexOf(s1);
			final int i2 = methodName.indexOf(s2);
			return Integer.compare(i1, i2);
		});

		return x;
	}

	private static DBEnum DB_ENUM = null;

	/**
	 * @param url
	 * @return
	 */
	private static String findCatalog(final String url) {
		// FIXME 2024年5月4日 下午6:19:00 zhangzhen: 在程序启动时就严格校验url
		// jdbc:mysql://192.168.1.10:3306/learn?useSSL=false&characterEncoding=utf8

		if (url.toLowerCase().contains("mysql")) {

			final String jdbc = "jdbc:mysql://";
			final String start = "/";
			final String end = "?";

			final int i = url.indexOf(jdbc);
			final int s = url.indexOf(start, i + jdbc.length());
			final int e = url.indexOf(end, s + 1);
			final String x = url.substring(s + start.length(), e);

			DB_ENUM = DBEnum.MYSQL;
			return x;
		}
		if (url.toLowerCase().contains("postgresql")) {
			final String keyword = "/";
			final int lastIndexOf = url.lastIndexOf(keyword, url.length());

			final String substring = url.substring(lastIndexOf + keyword.length());

			DB_ENUM = DBEnum.POSTGRESQL;
			return substring;
		}
		throw new IllegalArgumentException("JDBC 配置不支持：" + url);
	}

	public static DBEnum getDB() {
		return DB_ENUM;
	}

	private static StringJoiner getParameterNameFromMethod(final Method method) {
		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}
		return joiner;
	}
}

