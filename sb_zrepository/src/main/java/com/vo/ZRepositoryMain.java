package com.vo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
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
import com.vo.exception.DBNotSupportException;
import com.vo.exception.MethodNameDeclarationException;
import com.vo.exception.ParameterCountDeclarationException;
import com.vo.exception.ParameterNameDeclarationException;
import com.vo.exception.ParameterTypeDeclarationException;
import com.vo.exception.ZRepositoryException;

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

	private static final int findByXXIsNullAndXXIsNullAndXXAndXX_PARAMETER_SIZE = 2;
	private static final int findByXXIsNullAndXXIsNullAndXX_PARAMETER_SIZE = 1;
	private static final int findByXXIsNullAndXXAndXXAndXX_PARAMETER_SIZE = 3;
	private static final int findByXXIsNullAndXXAndXX_PARAMETER_SIZE = 2;
	private static final int findByXXIsNullAndXX_PARAMETER_SIZE = 1;
	private static final int findByXXIsNull_PARAMETER_SIZE = 0;

	private static final int FIND_BY_XX_NOT_NULL_PARAMETER_SIZE = 0;

	private static final int FIND_BY_XX_BETWEEN_PARAMETERS_SIZE = 2;

	private static final int findByXXOrderByXXDescLimit_Basic_PSIZE = 2;

	public static final String DELIMITER = ",";

	private static final String TYPE_NAME = "TYPE_NAME";

	private static final String COLUMN_NAME = "COLUMN_NAME";

	private static final String EMTPY = "";

	public static final String TABLE_NAME = "TABLE_NAME";

	private static final ZLog2 LOG = ZLog2.getInstance();

	public static final String _Z_CLASS = "_ZClass";

	private static ZCPool getPoolInstance(final String dataSourceName) {
		return ZCPool.getInstance(dataSourceName);
	}

	public static Map<Class, ZClass> generateClassForZRSubinterfaceMap(final Set<Class<?>> zrSubclassSet) {

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
		System.out.println("@" + ZRepository.class.getCanonicalName() + " 子接口中支持的声明式方法形式如下：");
		for (final String m : keySet) {
			final HashMap<String, String> hashMap = map.get(m);
			System.out.println("\t" + hashMap.keySet());
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
			final ZConnection write = getPoolInstance(annotation.dataSourceName()).getZConnection(Mode.WRITE);
			showCreateTable0(tableName, write, annotation.dataSourceName());
			final ZConnection read = getPoolInstance(annotation.dataSourceName()).getZConnection(Mode.READ);
			showCreateTable0(tableName, read, annotation.dataSourceName());
		}

	}

	/**
	 * @param tableName
	 * @param zc
	 * @param dataSourceName TODO
	 */
	// FIXME 2024年5月13日 上午12:03:38 zhangzhen: 考虑要做什么功能，在此得到了create table语句了，要不要做比如：
	// 1 校验读写数据源的引擎什么的必须保持一致（引擎似乎没必要一致，比如：写用innodb 读用myisam ）？
	// 2 之后是否做比如：根据 @ZEntity 注解来生成表结果DDL语句的功能，参考create table的返回结果
	private static void showCreateTable0(final String tableName, final ZConnection zc, final String dataSourceName) {

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
			getPoolInstance(dataSourceName).returnZConnectionAndCommit(zc);
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

				checkZEntity_TableNameExist(zEntity.tableName(), getPoolInstance(zEntity.dataSourceName()).getZConnection(Mode.WRITE), zEntity.dataSourceName());
				checkZEntity_TableNameExist(zEntity.tableName(), getPoolInstance(zEntity.dataSourceName()).getZConnection(Mode.READ), zEntity.dataSourceName());
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
			final String tType = typeArray[0];
			checkZRG(tType, typeArray[1], zrSubClass);

			final Method[] ms = zrSubClass.getMethods();

			for (final Method m : ms) {
				LOG.info("ZRepositoryStarter开始生成[{}]的方法[{}]的SQL模板", zrSubClass.getCanonicalName(), m.getName());

				final MethodSQL methodSQL = MethodRegex.check(m.getName(), m);
				//				final Entry<String, String> check = MethodRegex.check(m.getName(), m);

				try {
					final Class<?> typeClass = Class.forName(tType);
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
	 * @param dataSourceName TODO
	 *
	 */
	private static void checkZEntity_TableNameExist(final String tableName, final ZConnection zc, final String dataSourceName) {

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
			getPoolInstance(dataSourceName).returnZConnectionAndCommit(zc);
			SU.close(rs);
		}
	}

	/**
	 * 校验一下方法声明是否正确，是否符合命名规则，见MethodRegex.GROUP_ 开头的常量正则表达式
	 *
	 * @param entityClass  @ZEntity标记的类
	 * @param methodName ZRepository 子类中的自定义的findByXX的方法名称,如findByUserId
	 * @param sql        sql模板，如：select * from user where @ = ?
	 * @param methodSQL TODO
	 * @param zrClass ZRepository 的用户自定义的子接口
	 * @return 返回可用于java.sql.PreparedStatement 的SQL语句， 如 : select * from user where
	 *         id = ?
	 *
	 */
	private static String checkMethodName(final Class<?> entityClass, final String methodName, final String sql,
			final MethodSQL methodSQL, final Class zrClass) {
		if (methodSQL.isZQuery()) {
			return methodSQL.getSqlTemplate();
		}

		// methodName 从每个大写字母分开分成一个数组，如findByUserId 分成[find, By, User, Id]
		final List<String> fnLIst = getDeclaredFieldName(entityClass);

		// findByUserId 分成[find, By, User, Id] ，从前往后计算是否sql关键字，否则按照entity字段处理

		final HashSet<String> sqlKeyword = SqlPattern.SQL_KEYWORD;

		// SQL关键字按从长到短排序，防止出现 Or优先于Order被替换掉，剩余 der
		final ArrayList<String> skList = Lists.newArrayList(sqlKeyword);
		skList.sort(Comparator.comparing(String::length).reversed());

		final D d = findFieldName(entityClass, methodName);
		String sKeyword = d.getSqlKeyword();
		for (final String sk : skList) {
			sKeyword = sKeyword.replace(sk, EMTPY);
		}

		// 只剩SQL关键字的方法名称替换掉所有的SQL关键字后，必须是""
		if (!EMTPY.equals(sKeyword)) {
			final List<Field> fl = gNoZTransientFieldList(entityClass);
			final List<String> noZTFNL = fl.stream().map(Field::getName).collect(Collectors.toList());

			// FIXME 2024年6月7日 下午8:18:09 zhangzhen : 要不要先再详细点，具体到模糊匹配Field看哪个更接近（因为可能笔误写错了）
			final List<String> biwu = biwu(entityClass, methodName,  methodSQL.getMethodName());
			final String m =
					"[" + zrClass.getSimpleName() + "." + methodName + "]"
							+ "\r\n\t"
							+ "请确认方法名由SQL关键字和@" + ZEntity.class.getSimpleName()
							+ "两部分组成:"
							+ "\r\n\t"
							+ "SQL关键字包含:" + sqlKeyword
							+ "\r\n\t"
							+ entityClass.getSimpleName() + "中Field有:"
							+ noZTFNL
							+ "\r\n\t"
							+ "当前方法模板为:[" + methodSQL.getMethodName() + "]"
							+ ",请检查方法名称:[" + methodName + "]"
							//							+ ",是否@" + ZEntity.class.getSimpleName() + "中的Field名称写错了?"
							+ (biwu.isEmpty() ? EMTPY : ("\r\n\t" + "是否手误写错了?想写的是:" + biwu + "?"))

							+ "\r\n\t"
							+ "方法名称命名规则见 " + MethodRegex.class.getSimpleName()
							+ " 中以 GROUP_ 开头的常量。"
							+ "\r\n\t"
							;

			throw new MethodNameDeclarationException(m);
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
					"@" + ZRepository.class.getCanonicalName() + " 类 " + entityClass.getSimpleName()
					+ " 有重复的方法 [" + ml + "] ，不允许重名！"
					);
		}

		final Method method = ml.get(0);
		final Parameter[] ps = method.getParameters();

		String sqlA = sql;

		// FIXME 2024年5月18日 上午10:02:04 zhangzhen: debug 代码，记得删除
		if("findByTimestampNotLike".equals(method.getName())) {
			final int x2 = 1;
		}
		if (isZRClassMethod(method) || MethodRegex.isMethod_ANALYSIS_BY_ZENTITY_FIELD(method)) {
			final List<String> filedNameMethodNameOrder = d.getFiledNameMethodNameOrder();
			for (final String fieldName : filedNameMethodNameOrder) {
				final String dbColumnName = ZFieldConverter.toDbField(fieldName);
				sqlA = sqlA.replaceFirst("@", dbColumnName);
			}
		} else if (MethodRegex.isMethod_ANALYSIS_BY_METHOD_PARAMETERS(method)) {
			final List<String> filedNameMethodNameOrder = d.getFiledNameMethodNameOrder();
			if (filedNameMethodNameOrder.size() != ps.length) {

				final List<String> pnl = Arrays.stream(ps).map(Parameter::getName).collect(Collectors.toList());

				final String collect = d.getFiledNameMethodNameOrder().stream().map(ff -> {
					final Field declaredField = getDeclaredField(entityClass,
							ZFieldConverter.toJavaField(ZFieldConverter.toDbField(ff)));
					return declaredField.getType().getSimpleName() + " " + declaredField.getName();
				}).collect(Collectors.joining(DELIMITER));

				final String x1 =
						"[" + zrClass.getSimpleName() + "." + methodName + "]"
								+ "\r\n\t"
								+ "必须有且只有[" +filedNameMethodNameOrder.size() + "]个参数"
								+ "\r\n\t"
								+ "如:" + zrClass.getSimpleName() + "." + methodName +"(" + collect + ")"
								+ "\r\n\t"
								+ "实际方法参数为[" + ps.length + "]个,名称为" + pnl
								+ "\r\n\t"
								+ "请检查代码:去掉多余的参数"
								+ "\r\n\t"
								;
				throw new ParameterCountDeclarationException(x1);
			}

			for (int i = 0; i < ps.length; i++) {
				final Parameter p = ps[i];
				final String dbColumnName = ZFieldConverter.toDbField(p.getName());
				final String javaFiledName = filedNameMethodNameOrder.get(i);
				final String dbColumnNameFromJavaFieldName = ZFieldConverter.toDbField(javaFiledName);
				if (!Objects.equals(dbColumnNameFromJavaFieldName, dbColumnName)) {

					final String jfn2 = String.valueOf(javaFiledName.charAt(0)).toLowerCase() + javaFiledName.substring(1);
					final String m =
							"[" + zrClass.getSimpleName() + "." + methodName + "]"
									+ "\r\n\t"
									+ "请检查代码:参数名称[" + p.getName() + "]"
									+ "修改为方法名称中对应的Field的名称[" + jfn2 + "]一致."
									+ "\r\n\t"
									+ "把参数[" + p.getName() + "]名称修改为[" + jfn2 + "]"
									+ "\r\n\t"
									;

					throw new ParameterNameDeclarationException(m);
				}

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
		return Arrays.asList(fs).stream().map(Field::getName).collect(Collectors.toList());
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
			// FIXME 2024年6月7日 下午6:23:13 zhangzhen : debug 代码，记得删除
			if("findByIdNotLike".equals(zm.getName())) {
				final int xx2 = 1;
			}

			final String sql = ZRSqlMap.get(myZRClass.getCanonicalName(), zm.getName());

			// 结束
			final String body = "String sql = \""+sql+"\";";

			final String body2 = EMTPY;
			final String entityT = typeArray[0];
			final String methodS = getSuMethod(method, entityT, myZRClass, forName(entityT));

			final String x = "\t" +body + "\n\t" + body2  + "\n\t" + methodS;
			zm.setBody(x);
		}
		return zmSet;
	}

	private static Class<?> forName(final String entityT)  {
		try {
			return Class.forName(entityT);
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getSuMethod(final Method method, final String entityTName, final Class<?> myZRClass, final Class<?> entityClass) {

		final String modeString = modeString(method);

		final String className1 = "\"" + myZRClass.getCanonicalName() + "\"";
		final String methodName1 = "\"" + method.getName() + "\"";

		switch (method.getName()) {
		case "findById":
			return "return " + SU.class.getCanonicalName() + ".findById(" + className1 + "," + methodName1 + "," + modeString + ", id,classType,sql);";

		case "findByIdIn":
			return "return " + SU.class.getCanonicalName() + ".findByIdIn(" + className1 + "," + methodName1 + "," + modeString + " ,idList,classType,sql);";

		case "findAll":
			return "return " + SU.class.getCanonicalName() + ".findAll(" + className1 + "," + methodName1 + "," + modeString + ", classType,sql);";

		case "saveAll":
			return "return " + SU.class.getCanonicalName() + ".saveAll(" + className1 + "," + methodName1 + "," + modeString + ", classType,sql,tList);";

		case "update":
			return "return " + SU.class.getCanonicalName() + ".update(" + className1 + "," + methodName1 + "," + modeString + ", classType,t,sql);";

		case "save":
			return "return " + SU.class.getCanonicalName() + ".save(" + className1 + "," + methodName1 + ","
			+ modeString + "," + ", classType," + entityTName + ",t,sql);";

		case "page":
			return "return " + SU.class.getCanonicalName() + ".page(" + className1 + "," + methodName1 + "," + modeString + ", classType," + entityTName
					+ ",t,sort,sql,size,page);";

		case "existByIdIn":
			return "return " + SU.class.getCanonicalName() + ".existByIdIn(" + className1 + "," + methodName1 + "," + modeString + ", idList,classType,sql);";
		case "existById":
			return "return " + SU.class.getCanonicalName() + ".existById(" + className1 + "," + methodName1 + "," + modeString + ", id,classType,sql);";

		case "deleteById":
			return "return " + SU.class.getCanonicalName() + ".deleteById(" + className1 + "," + methodName1 + "," + modeString + ", id,classType,sql);";

		case "deleteByIdIn":
			return "return " + SU.class.getCanonicalName() + ".deleteByIdIn(" + className1 + "," + methodName1 + "," + modeString + ", idList,classType,sql);";

		case "deleteAll":
			return "return " + SU.class.getCanonicalName() + ".deleteAll(" + className1 + "," + methodName1 + "," + modeString + ", classType,sql);";

		case "count":
			return "return " + SU.class.getCanonicalName() + ".count(" + className1 + "," + methodName1
					+ ","  + modeString + ",classType,sql);";

		case "find":
			return find(myZRClass, entityClass, modeString, className1, methodName1, method);

		default:

			// default  ZR的子类声明的方法
			final MethodSQL methodSQL = MethodRegex.check(method.getName(), method);
			final String methodName = methodSQL.getMethodName();

			if (methodName.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXDescLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXOrderByXXDescLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXAndXXAndXXOrderByXXDescLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXAndXXOrderByXXDescLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXOrderByXXDescLimit)
					|| methodName.matches(MethodRegex.GROUP_findByXXOrderByXXDescLimit)
					// 下面是OrderByXXAsc的了
					||methodName.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXAndXXAndXXAndXXOrderByXXLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXAndXXAndXXOrderByXXLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXAndXXOrderByXXLimit)
					|| methodName.matches(MethodRegex.findByXXAndXXOrderByXXLimit)
					|| methodName.matches(MethodRegex.GROUP_findByXXOrderByXXLimit)) {
				return findByXXOrderByXXDescLimit(myZRClass, entityClass, className1, method);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXXEndingWith)) {
				return findByXXXEndingWith(myZRClass, entityClass, className1, method);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXXStartingWith)) {
				return findByXXXStartingWith(myZRClass, entityClass, className1, method);
			}

			if (	methodName.matches(MethodRegex.GROUP_findByXXGreaterThanEquals)
					|| methodName.matches(MethodRegex.GROUP_findByXXGreaterThan)
					|| methodName.matches(MethodRegex.GROUP_findByXXLessThanEquals)
					|| methodName.matches(MethodRegex.GROUP_findByXXLessThan)
					) {
				return findByXXGreaterThanEquals(myZRClass, entityClass, className1, method);
			}

			if (methodName.matches(MethodRegex.findByXXNotBetween)) {
				return findByXXNotBetween(myZRClass, entityClass, className1, method);
			}
			if (methodName.matches(MethodRegex.findByXXBetween)) {
				return findByXXBetween(myZRClass, entityClass, className1, method);
			}

			if (methodName.matches(MethodRegex.GROUP_findByxx_in)) {
				return findByXXIn(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByxx_in);
			}

			if (methodName.matches(MethodRegex.countingByXXXAndXXAndXXAndXX)) {
				return countingByXXX(myZRClass, entityClass, className1, method, MethodRegex.countingByXXXAndXXAndXXAndXX);
			}
			if (methodName.matches(MethodRegex.countingByXXXAndXXAndXX)) {
				return countingByXXX(myZRClass, entityClass, className1, method, MethodRegex.countingByXXXAndXXAndXX);
			}
			if (methodName.matches(MethodRegex.countingByXXXAndXX)) {
				return countingByXXX(myZRClass, entityClass, className1, method, MethodRegex.countingByXXXAndXX);
			}
			if (methodName.matches(MethodRegex.GROUP_CountingByXXX)) {
				return countingByXXX(myZRClass, entityClass, className1, method, MethodRegex.GROUP_CountingByXXX);
			}

			if (methodName.matches(MethodRegex.GROUP_findByxxNotNull)) {
				return findByXXNotNull(myZRClass, entityClass, className1, method);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXIsNullAndXXIsNullAndXXAndXX)) {
				return findByXXIsNullAndXXIsNullAndXXAndXX(myZRClass, entityClass, className1,
						method, MethodRegex.GROUP_findByXXIsNullAndXXIsNullAndXXAndXX);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXIsNullAndXXIsNullAndXX)) {
				return findByXXIsNullAndXXIsNullAndXX(myZRClass, entityClass, className1,
						method, MethodRegex.GROUP_findByXXIsNullAndXXIsNullAndXX);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXIsNullAndXXAndXXAndXX)) {
				return findByXXIsNullAndXXAndXXAndXX(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByXXIsNullAndXXAndXXAndXX);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXIsNullAndXXAndXX)) {
				return findByXXIsNullAndXXAndXX(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByXXIsNullAndXXAndXX);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXIsNullAndXX)) {
				return findByXXIsNullAndXX(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByXXIsNullAndXX);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXIsNull)) {
				return findByXXIsNull(myZRClass, entityClass, method, modeString, className1, methodName1, MethodRegex.GROUP_findByXXIsNull);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXNotLike)) {
				return findByXXNotLike(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByXXNotLike);
			}

			if (methodName.matches(MethodRegex.GROUP_findByXXLike)) {
				return findByXXLike(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByXXLike);
			}

			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1,
						method, MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1,
						method, MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1,
						method, MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYYOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYYOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYYOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYYOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYYOrYY);
			}
			if (methodName.matches(MethodRegex.findByXXOrYY)) {
				return findByXXOrYY(myZRClass, entityClass, className1, method, MethodRegex.findByXXOrYY);
			}

			// 最短的排最后
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY) || methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYYAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYYAndYY);
			}
			if (methodName.matches(MethodRegex.findByXXAndYY)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.findByXXAndYY);
			}
			if (methodName.matches(MethodRegex.GROUP_findByXX)) {
				return findByXX(myZRClass, entityClass, className1, method, MethodRegex.GROUP_findByXX);
			}

			// 最后面是@ZQuery自定义方法
			if (methodSQL.isZQuery()) {
				return zQuery(myZRClass, entityClass, className1, method, methodSQL.getSqlTemplate(), entityTName);
			}

			break;
		}

		return EMTPY;
	}

	private static String find(final Class<?> myZRClass, final Class<?> entityClass, final String modeString, final String className1, final String methodName1, final Method method) {
		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		return "return " + SU.class.getCanonicalName() + ".find(" + className1 + "," + methodName1
				+ ","  + modeString + ",classType,"+returnType.getCanonicalName()+",sql," + joiner.toString()  + ");";
	}

	private static String findByXXIsNull(final Class<?> myZRClass, final Class<?> entityClass, final Method method,
			final String modeString, final String className1, final String methodName1, final String methodRegex) {

		final Parameter[] parameters = method.getParameters();
		if (parameters.length != findByXXIsNull_PARAMETER_SIZE) {

			final String xxx =
					"\r\n\t"
							+ methodRegex + "声明式方法"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]"
							+ "\r\n\t"
							+ "无需参数,当前参数个数为[" + parameters.length + "]"
							+ "\r\n\t"
							+ "请修改方法声明:去掉所有参数"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(xxx);
		}

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		return "return " + SU.class.getCanonicalName() + ".findByXXIsNull(" + className1 + "," + methodName1
				+ "," + modeString + ",classType," + returnType.getCanonicalName() + ",sql);";
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

	private static String findByXXXEndingWith(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {

		final StringJoiner joiner = checkFindByXXEndingWithAndStartingWith(myZRClass, entityClass,
				method, MethodRegex.GROUP_findByXXXEndingWith, ZRMethodU.STARTING_WITH);

		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";

		return "return " + SU.class.getCanonicalName() + ".findByXXXEndingWith(" + className1 + "," + methodName1 + ","
		+ modeString + ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static StringJoiner checkFindByXXEndingWithAndStartingWith(final Class<?> zrSubClass, final Class<?> entityClass,
			final Method method, final String methodRegex, final Set<Class<?>> supportedClassSet) {
		final D d = findFieldName(entityClass, method.getName());
		final List<String> fl = d.getFiledNameMethodNameOrder();
		final String javaFieldName = ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fl.get(0)));
		final Field f = getDeclaredField(entityClass, javaFieldName);

		checkParameterNameAndFiledNameMethodNameOrderEquals(zrSubClass, method, fl);

		if (!supportedClassSet.contains(f.getType())) {

			final List<Field> stringOrCharacterFieldList = Arrays.stream(entityClass.getDeclaredFields())
					.filter(fx -> !fx.isAnnotationPresent(ZTransient.class))
					.filter(fx -> fx.getType().equals(String.class) || fx.getType().equals(Character.class))
					.collect(Collectors.toList());

			final List<String> mm = stringOrCharacterFieldList.stream().map(fx -> ZFieldConverter.toMethodName(fx.getName()))
					.collect(Collectors.toList());

			final List<String> methodNameL = mm.stream().map(m -> methodRegex.replaceFirst("\\.\\+", m))
					.collect(Collectors.toList());

			final String supportedClassName = supportedClassSet.stream().map(Class::getSimpleName).collect(Collectors.joining("/"));

			final String xxx =
					"\r\n\t"
							+ methodRegex + "声明式方法"
							+ "[" + zrSubClass.getSimpleName() + "." + method.getName() + "]"
							+ "\r\n\t"
							+ "不支持声明中" + fl + "的类型[" + f.getType().getCanonicalName() + "]"
							+ "\r\n\t"
							+ "支持类型为[" + supportedClassName + "],"
							+ "请修改方法声明:"
							+ "\r\n\t"
							+ "修改为findByXX中的XX为["
							+ entityClass.getSimpleName() + "]中的"
							+ "[" + String.class.getSimpleName() + "/" + Character.class.getSimpleName() + "]类型的Field:"
							+ "\r\n\t"
							+ mm
							+ "\r\n\t"
							+ "如:" + methodNameL
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(xxx);
		}

		final Parameter p0 = method.getParameters()[0];

		if (!supportedClassSet.contains(p0.getType())) {
			final String supportedClassName = supportedClassSet.stream().map(Class::getSimpleName).collect(Collectors.joining("/"));

			final String xxx =
					"\r\n\t"
							+ methodRegex + "声明式方法"
							+ "\r\n\t"
							+ "[" + zrSubClass.getSimpleName() + "." + method.getName() + "]参数类型必须为"
							+ "\r\n\t"
							+ "[" + supportedClassName + "]"
							+ ",当前参数声明为(" + p0.getType().getSimpleName() + " " + p0.getName() + ")"
							+ "\r\n\t"
							+ "请检查代码:修改参数["+p0.getName()+"]类型为"
							+ "[" + supportedClassName + "]"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(xxx);
		}
		// FIXME 2024年6月8日 下午11:57:49 zhangzhen : 不继续校验了，比如findByStringEndingWith(String x)/(Character x)都可以
		// 就这么算一个feature吧。不算bug,只要参数是支持的类型其中之一就行，没必要那么严格,实际上还可能造成不方便使用

		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}
		return joiner;
	}

	private static String findByXXXStartingWith(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {

		final StringJoiner joiner = checkFindByXXEndingWithAndStartingWith(myZRClass, entityClass,
				method, MethodRegex.GROUP_findByXXXStartingWith, ZRMethodU.STARTING_WITH);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String modeString = modeString(method);
		final String methodName1 = "\"" + method.getName() + "\"";

		return "return " + SU.class.getCanonicalName() + ".findByXXXStartingWith(" + className1 + "," + methodName1
				+ "," + modeString + ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXGreaterThanEquals(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {

		final StringJoiner joiner = checkFindByXXEndingWithAndStartingWith(myZRClass, entityClass,
				method, MethodRegex.GROUP_findByXXGreaterThan, ZRMethodU.GTE);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String modeString = modeString(method);
		final String methodName1 = "\"" + method.getName() + "\"";

		return "return " + SU.class.getCanonicalName() + ".findByIdLessThan(" + className1 + "," + methodName1 + "," + modeString
				+ ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXOrderByXXDescLimit(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {
		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}

		final D d = findFieldName(entityClass, method.getName());

		if (((method.getParameters().length - 1)) != (d.getFiledNameMethodNameOrder().size())) {
			final StringJoiner fnj = new StringJoiner(",");
			for(int k =0;k<(d.getFiledNameMethodNameOrder().size()-1);k++) {
				final String fieldName = d.getFiledNameMethodNameOrder().get(k);
				final String javaFieldName = ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fieldName));
				final Field f = getDeclaredField(entityClass, javaFieldName);

				fnj.add(f.getType().getSimpleName() + " " + javaFieldName);
			}

			final String m1 =
					"\r\n\t"
							+ "findByXXOrderByXXLimit/findByXXOrderByXXDescLimit 声明式方法"
							+ "\r\n\t"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]"
							+ "\r\n\t"
							+ "必须有且只有["+((d.getFiledNameMethodNameOrder().size() + findByXXOrderByXXDescLimit_Basic_PSIZE) - 1)+"]个参数,"
							+ "形式为"
							+ "\r\n\t"
							+ method.getName() + "("+fnj+",Integer limit,Integer offset)"
							+ "\r\n\t"
							+ "当前有[" + method.getParameterCount() + "]个,请检查代码"
							;
			throw new IllegalArgumentException(m1);
		}

		// 前面的除了OrderByXX和limit和offset之外的参数
		for (int k = 0; k < (d.getFiledNameMethodNameOrder().size() - 1); k++) {
			final String fieldName = d.getFiledNameMethodNameOrder().get(k);
			final String javaFieldName = ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fieldName));
			final Field f = getDeclaredField(entityClass, javaFieldName);
			if (!f.getType().equals(method.getParameters()[k].getType())) {

				final StringJoiner fnj = new StringJoiner(",");
				for(int kx =0;kx<(method.getParameters().length);kx++) {
					final String fieldNamex = method.getParameters()[kx].getName();
					fnj.add(method.getParameters()[kx].getType().getSimpleName() + " " + fieldNamex);
				}

				final String m1 =
						"\r\n\t"
								+ "findByXXOrderByXXLimit/findByXXOrderByXXDescLimit 声明式方法参数类型错误:"
								+ "\r\n\t"
								+ method.getName() + "(" + fnj + ")"
								+ "\r\n\t"
								+ "按当前方法声明,第["+(k+1)+"]个参数类型必须为[" + f.getType().getSimpleName() + "]"
								+ ",当前为[" + method.getParameters()[k].getType().getSimpleName() + "]"
								+ "\r\n\t"
								+ "请检查代码:替换掉方法声明中的["+fieldName+"],"
								+ "或者修改参数列表中的["
								+ method.getParameters()[k].getType().getSimpleName() + " " + method.getParameters()[k].getName()+ "]类型为"
								+ "[" + f.getType().getSimpleName() + "]"
								+ "\r\n\t"
								;
				throw new IllegalArgumentException(m1);
			}
		}

		// 最后面必须是固定的 (Integer limit,Inetger offset)参数
		if (!method.getParameters()[method.getParameters().length - 2].getType().equals(Integer.class)
				|| !method.getParameters()[method.getParameters().length - 1].getType().equals(Integer.class)) {
			final String m1 =
					"\r\n\t"
							+ "findByXXOrderByXXLimit/findByXXOrderByXXDescLimit 声明式方法参数类型错误:"
							+ "\r\n\t"
							+ method.getName()
							+ "\r\n\t"
							+ "最后面两个参数必须固定为(Integer limit,Integer offset)的形式,"
							+ "\r\n\t"
							+ "类型必须为[" + Integer.class.getSimpleName() + "],名称推荐统一为limit和offset"
							+ ",当前为(" +
							method.getParameters()[method.getParameters().length - 2].getType().getSimpleName()
							+ " " + method.getParameters()[method.getParameters().length - 2].getName()
							+ ","
							+ method.getParameters()[method.getParameters().length - 1].getType().getSimpleName()
							+ " "
							+ method.getParameters()[method.getParameters().length - 1].getName()
							+ ")"
							+ "\r\n\t"
							+ "请检查代码:修正类型为[" + Integer.class.getSimpleName() + "]"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(m1);
		}

		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXOrderByXXLimit(" + className1 + "," + methodName1 + ","
		+ modeString + ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static Field getDeclaredField(final Class<?> cls, final String fieldName) {
		try {
			final Field f = cls.getDeclaredField(fieldName);
			return f;
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String countingByXXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1,
			final Method method, final String methodRegex) {

		checkParameterTypeAndName(myZRClass, entityClass, method, methodRegex);

		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}
		final String modeString = modeString(method);

		final int ac = StrUtil.count(joiner.toString(), DELIMITER);

		// FIXME 2024年5月18日 下午3:32:25 zhangzhen: byte[] 类型引起的问题 : 很多方法都有此问题，都要好好再测试byte[] 类型
		// countBy多个条件的不能把countByXX单个的参数改为Object... a 然后复用，因为一个条件并且为byte[]类型的话，a会被认为是byte[]
		// 而不是a.length=1并且这唯一的值是一个byte[]

		final String methodName1 = "\"" + method.getName() + "\"";
		final String suMethodName =
				ac == 0 ? "countingByXX" : "countingByXXAndXX";
		return "return " + SU.class.getCanonicalName() + "." + suMethodName + "(" + className1 + "," + methodName1 + ","
		+ modeString + ",classType,sql," + joiner.toString() + ");";
	}

	private static void checkParameterTypeAndName(final Class<?> myZRClass, final Class<?> entityClass, final Method method, final String methodRegex) {
		final D d = findFieldName(entityClass, method.getName());
		// 先校验method.parameters 个数
		checkParameterLength(myZRClass, entityClass, method, methodRegex, d);

		// 校验参数类型和名称
		final Parameter[] ps = method.getParameters();
		for (int i = 0; i < ps.length; i++) {
			final Parameter p = ps[i];
			final String fieldName = d.getFiledNameMethodNameOrder().get(i);
			final String javaFieldName = ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fieldName));
			final Field f = getDeclaredField(entityClass, javaFieldName);
			if (!f.getType().equals(p.getType())) {
				throwParameterTypeDeclarationException(myZRClass, entityClass, method, methodRegex, d, ps, i, f);
			}
		}
	}

	private static void checkParameterLength(final Class<?> myZRClass, final Class<?> entityClass, final Method method,
			final String methodRegex, final D d) {
		if (method.getParameters().length != d.getFiledNameMethodNameOrder().size()) {
			final Field f2 = getDeclaredField(entityClass, ZFieldConverter.toJavaField(ZFieldConverter.toDbField(d.getFiledNameMethodNameOrder().get(0))));
			final String fnj = f2.getType().getSimpleName() + " " + f2.getName()+ "1" + "," + f2.getType().getSimpleName() + " " + f2.getName() + "2";

			final String m1 =
					methodRegex + " 声明式方法"
							+ "\r\n\t"
							+ "[" + myZRClass.getSimpleName() +"." + method.getName() + "]"
							+ "\r\n\t"
							+ "必须有且只有["+(d.getFiledNameMethodNameOrder().size())+"]个参数,"
							+ "形式为"
							+ "\r\n\t"
							+ method.getName() + "(" + fnj + ")"
							+ "\r\n\t"
							+ "当前有[" + method.getParameterCount() + "]个,请检查代码:"
							+ "去掉多余的参数"
							+ "\r\n\t"
							;
			throw new ParameterCountDeclarationException(m1);
		}
	}

	private static void throwParameterTypeDeclarationException(final Class<?> myZRClass, final Class<?> entityClass, final Method method,
			final String methodRegex, final D d, final Parameter[] ps, final int i, final Field f) {
		final String collect = d.getFiledNameMethodNameOrder().stream().map(ff -> {
			final Field declaredField = getDeclaredField(entityClass,
					ZFieldConverter.toJavaField(ZFieldConverter.toDbField(ff)));
			return declaredField.getType().getSimpleName() + " " + declaredField.getName();
		}).collect(Collectors.joining(DELIMITER));

		final String fnj = Arrays.stream(ps).map(p1 -> p1.getType().getSimpleName() + " " + p1.getName())
				.collect(Collectors.joining(","));

		final String m1 =
				"\r\n\t"
						+ methodRegex + " 声明式方法参数类型声明错误"
						+ "\r\n\t"
						+ "[" + myZRClass.getSimpleName() +"." + method.getName() + "]"
						+ "\r\n\t"
						+ "第["+(i+1)+"]参数类型必须声明为[" + f.getType().getSimpleName() + "]"
						+ ",如:(" + collect + ")"
						+ "\r\n\t"
						+ "当前参数类型声明为(" + fnj + ")"
						+ "\r\n\t"
						+ "请检查代码:把参数声明修改为(" + collect + ")的形式.只需要修改参数类型,不需要修改参数名称"
						+ "\r\n\t"
						;
		throw new ParameterTypeDeclarationException(m1);
	}

	private static String findByXXOrYY(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {

		checkParameterTypeAndName(myZRClass, entityClass, method, methodRegex);

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";

		return "return " + SU.class.getCanonicalName() + ".findByXXOrYY(" + className1 + "," + methodName1
				+ "," + modeString + ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXIsNullAndXXIsNullAndXXAndXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {

		checkFindByXXIsNull(myZRClass, entityClass, method, methodRegex, findByXXIsNullAndXXIsNullAndXXAndXX_PARAMETER_SIZE);

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXIsNullAndXXIsNullAndXXAndXX(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static void checkFindByXXIsNull(final Class<?> myZRClass, final Class<?> entityClass, final Method method, final String methodRegex, final int PARAMETER_SIZE) {
		final D d = findFieldName(entityClass, method.getName());
		// 先校验method.parameters 个数
		final List<String> filedNameMethodNameOrder = d.getFiledNameMethodNameOrder().subList(
				d.getFiledNameMethodNameOrder().size() - PARAMETER_SIZE,
				d.getFiledNameMethodNameOrder().size());

		if (method.getParameters().length != PARAMETER_SIZE) {
			final String collect = filedNameMethodNameOrder
					.stream().map(ff -> {
						final Field declaredField = getDeclaredField(entityClass,
								ZFieldConverter.toJavaField(ZFieldConverter.toDbField(ff)));
						return declaredField.getType().getSimpleName() + " " + declaredField.getName();
					}).collect(Collectors.joining(DELIMITER));

			final String m1 =
					"\r\n\t"
							+ methodRegex + " 声明式方法"
							+ "\r\n\t"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]"
							+ "\r\n\t"
							+ "必须有且只有["+(PARAMETER_SIZE)+"]个参数,"
							+ "形式为"
							+ "\r\n\t"
							+ method.getName() + "(" + collect + ")"
							+ "\r\n\t"
							+ "当前有[" + method.getParameterCount() + "]个,"
							+ "请检查代码:把方法参数声明修改为(" + collect + ")"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(m1);
		}

		// 校验参数类型和名称
		final Parameter[] ps = method.getParameters();
		for (int i = 0; i < ps.length; i++) {
			final Parameter p = ps[i];
			final String fieldName = filedNameMethodNameOrder.get(i);
			final String javaFieldName = ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fieldName));
			final Field f = getDeclaredField(entityClass, javaFieldName);
			if (!f.getType().equals(p.getType())) {

				final String collect = filedNameMethodNameOrder.stream().map(ff -> {
					final Field declaredField = getDeclaredField(entityClass,
							ZFieldConverter.toJavaField(ZFieldConverter.toDbField(ff)));
					return declaredField.getType().getSimpleName() + " " + declaredField.getName();
				}).collect(Collectors.joining(DELIMITER));

				final String fnj = Arrays.stream(ps).map(p1 -> p1.getType().getSimpleName() + " " + p1.getName())
						.collect(Collectors.joining(DELIMITER));

				final String m1 =
						"\r\n\t"
								+ methodRegex + " 声明式方法参数类型声明错误"
								+ "\r\n\t"
								+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]"
								+ "\r\n\t"
								+ "第["+(i+1)+"]参数类型必须声明为[" + f.getType().getSimpleName() + "]"
								+ ",如:(" + collect + ")"
								+ "\r\n\t"
								+ "当前参数类型声明为(" + fnj + ")"
								+ "\r\n\t"
								+ "请检查代码:把参数声明修改为(" + collect + ")的形式.只需要修改参数类型,不需要修改参数名称"
								+ "\r\n\t"
								;
				throw new IllegalArgumentException(m1);
			}
		}
	}

	private static String findByXXIsNullAndXXIsNullAndXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {

		checkFindByXXIsNull(myZRClass, entityClass, method, methodRegex, findByXXIsNullAndXXIsNullAndXX_PARAMETER_SIZE);

		final StringJoiner joiner = getParameterNameFromMethod(method);

		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXIsNullAndXXIsNullAndXX(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXIsNullAndXXAndXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {

		checkFindByXXIsNull(myZRClass, entityClass, method, methodRegex, findByXXIsNullAndXXAndXX_PARAMETER_SIZE);

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXIsNullAndXXAndXX(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXIsNullAndXXAndXXAndXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {

		checkFindByXXIsNull(myZRClass, entityClass, method, methodRegex, findByXXIsNullAndXXAndXXAndXX_PARAMETER_SIZE);

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXIsNullAndXXAndXX(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXIsNullAndXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {

		checkFindByXXIsNull(myZRClass, entityClass, method, methodRegex, findByXXIsNullAndXX_PARAMETER_SIZE);

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXIsNullAndXX(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXNotNull(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {

		// findByXXNotNull 无需参数，且必须无参数
		if (method.getParameters().length != FIND_BY_XX_NOT_NULL_PARAMETER_SIZE) {

			final String m1 =
					"\r\n\t"
							+ MethodRegex.GROUP_findByxxNotNull + " 声明式方法"
							+ "\r\n\t"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]"
							+ "\r\n\t"
							+ "参数个数必须为["+FIND_BY_XX_NOT_NULL_PARAMETER_SIZE+"]"
							+ "\r\n\t"
							+ "当前有[" + method.getParameterCount() + "]个"
							+ "\r\n\t"
							+ "请检查代码:去掉所有的的参数"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(m1);
		}

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);
		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXNotNull(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String findByXXNotLike(final Class<?> myZRClass, final Class entityClass, final String className1, final Method method, final String methodRegex) {
		return findByXXLike0(myZRClass, entityClass, className1, method, methodRegex);
	}

	private static String findByXXLike(final Class<?> myZRClass, final Class entityClass, final String className1, final Method method, final String methodRegex) {
		return findByXXLike0(myZRClass, entityClass, className1, method, methodRegex);
	}

	private static String findByXXLike0(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String methodRegex) {
		checkFindByXXLike(myZRClass, method, methodRegex);

		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}

		final String modeString = modeString(method);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);
		final String methodName1 = "\"" + method.getName() + "\"";

		return "return " + SU.class.getCanonicalName() + ".findByXXLike(" + className1 + "," + methodName1 + ","
		+ modeString + ",classType," + returnType.getName() + ",sql," + joiner.toString() + ");";
	}

	private static void checkFindByXXLike(final Class<?> myZRClass, final Method method, final String methodRegex) {
		if (!method.getParameters()[0].getType().equals(String.class) && !method.getParameters()[0].getType().equals(Character.class)) {
			final String xxx =

					"\r\n\t"
							+ methodRegex + " 声明式方法"
							+ "\r\n\t"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName()
							+ "] 参数类型必须为["
							+ String.class.getSimpleName()
							+ "/" + Character.class.getSimpleName()
							+ "],当前为" + method.getParameters()[0].getType().getCanonicalName()
							+ "\r\n\t"
							+ "请检查代码:修改参数类型为["
							+ String.class.getSimpleName()
							+ "/" + Character.class.getSimpleName() + "]"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(xxx);
		}
	}

	private static String findByXXNotBetween(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {
		checkFindByXXBetween(myZRClass, entityClass, method, MethodRegex.findByXXNotBetween);
		return findByXXBetween0(myZRClass, entityClass, className1, method);
	}

	private static String findByXXBetween(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {
		checkFindByXXBetween(myZRClass, entityClass, method, MethodRegex.findByXXBetween);
		return findByXXBetween0(myZRClass, entityClass, className1, method);
	}

	private static String findByXXBetween0(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method) {
		final StringJoiner joiner = getParameterNameFromMethod(method);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String modeString = modeString(method);
		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXBetween(" + className1 + "," + methodName1 + "," + modeString
				+ ",classType," + returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static void checkFindByXXBetween(final Class<?> myZRClass, final Class<?> entityClass, final Method method, final String methodRegex) {

		final D d = findFieldName(entityClass, method.getName());
		// 先校验method.parameters 个数
		if (method.getParameters().length != FIND_BY_XX_BETWEEN_PARAMETERS_SIZE) {
			final Field f2 = getDeclaredField(entityClass, ZFieldConverter.toJavaField(ZFieldConverter.toDbField(d.getFiledNameMethodNameOrder().get(0))));
			final String fnj = f2.getType().getSimpleName() + " " + f2.getName()+ "1" + "," + f2.getType().getSimpleName() + " " + f2.getName() + "2";

			final String m1 =
					"\r\n\t"
							+ methodRegex + " 声明式方法"
							+ "\r\n\t"
							+ "[" +myZRClass.getSimpleName() + "." + method.getName() + "]"
							+ "\r\n\t"
							+ "必须有且只有["+(FIND_BY_XX_BETWEEN_PARAMETERS_SIZE)+"]个参数,"
							+ "形式为"
							+ "\r\n\t"
							+ method.getName() + "(" + fnj + ")"
							+ "\r\n\t"
							+ "当前有[" + method.getParameterCount() + "]个,请检查代码:"
							+ "去掉多余的参数"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(m1);
		}

		// 校验参数类型和名称
		final Field f2 = getDeclaredField(entityClass,
				ZFieldConverter.toJavaField(ZFieldConverter.toDbField(d.getFiledNameMethodNameOrder().get(0))));
		if (!method.getParameters()[0].getType().equals(f2.getType())
				|| !method.getParameters()[1].getType().equals(f2.getType())) {

			final String fnj = f2.getType().getSimpleName() + " " + f2.getName()+ "1" + "," + f2.getType().getSimpleName() + " " + f2.getName() + "2";

			final String m1 =
					"\r\n\t"
							+ methodRegex + " 声明式方法"
							+ "\r\n\t"
							+ myZRClass.getSimpleName()  + "." + method.getName() + "(" +  method.getParameters()[0].getType().getSimpleName() + " " +
							method.getParameters()[0].getName() + "," + method.getParameters()[1].getType().getSimpleName() + " "
							+ method.getParameters()[1].getName() + ")"
							+ "\r\n\t"
							+ "两个参数类型必须都为[" + f2.getType().getSimpleName() + "]"
							+ ",如:(" + fnj + ")"
							+ "\r\n\t"
							+ "当前参数类型声明为(" + method.getParameters()[0].getType().getSimpleName() + " " +
							method.getParameters()[0].getName() + "," + method.getParameters()[1].getType().getSimpleName() + " "
							+ method.getParameters()[1].getName() + ")"
							+ "\r\n\t"
							+ "请检查代码:把参数声明修改为(" + fnj + ")的形式.只需要修改参数类型,不需要修改参数名称"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(m1);
		}
	}

	private static String findByXXIn(final Class<?> myZRClass, final Class<?> entityClass, final String className1,
			final Method method, final String methodRegex) {

		final D d = findFieldName(entityClass, method.getName());

		final List<String> filedNameMethodNameOrder = d.getFiledNameMethodNameOrder();
		final String x = filedNameMethodNameOrder.get(0);
		final Optional<Field> o = Arrays.stream(entityClass.getDeclaredFields()).filter(f -> f.getName().equals(ZFieldConverter.toJavaField(ZFieldConverter.toDbField(x)))).findAny();
		final Field fo = o.get();
		if (fo.getType().isArray()) {
			throw new IllegalArgumentException(
					"\r\n\t"
							+ methodRegex + "声明式方法["+myZRClass.getSimpleName() + "."+method.getName()+"]类型不支持:"
							+ "\r\n\t"
							+ "不支持数组类型"
							+ "\r\n\t"
							+ "["+method.getName()+"]中的["
							+ filedNameMethodNameOrder.get(0) + "]是数组类型,请修改方法声明为支持的类型"
							+ "\r\n\t"
					);
		}

		if (method.getParameters().length == 0) {
			throw new IllegalArgumentException(
					"\r\n\t"
							+ methodRegex + "声明式方法["+myZRClass.getSimpleName() + "."+method.getName()+"]参数个数错误:"
							+ "\r\n\t"
							+ "必须有且只有一个类型为["+ List.class.getSimpleName() +"/" + Set.class.getSimpleName() + "]的参数"
							+ "\r\n\t"
							+ "请修改方法声明:给方法添加一个类型为[" + List.class.getSimpleName() +"/" + Set.class.getSimpleName() +"]的参数"
							+ "\r\n\t"
					);
		}

		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}

		final String modeString = modeString(method);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String methodName1 = "\"" + method.getName() + "\"";
		return "return " + SU.class.getCanonicalName() + ".findByXXIn(" + className1 + "," + methodName1 + "," + modeString + ",classType,"
		+ returnType.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static String zQuery(final Class<?> myZRClass, final Class<?> entityClass, final String className1, final Method method, final String sqlTemplate, final String entityTName) {

		final String u = sqlTemplate.trim().toUpperCase();
		String subClassMethodName = null ;

		final Class<?> returnType = method.getReturnType();
		if (u.startsWith(MethodRegex.SELECT)) {
			if (!List.class.equals(returnType)) {
				final String m =
						"\r\n\t"
								+ "@" + ZQuery.class.getSimpleName()
								+ "方法[" + myZRClass.getSimpleName() + "." + method.getName() + "]返回类型声明错误:"
								+ "\r\n\t"
								+ "[SELECT]操作必须返回[" + List.class.getCanonicalName() + "]类型,当前返回类型为["
								+ returnType.getCanonicalName() + "]"
								+ "\r\n\t"
								+ "请修改代码:把返回类型改为[" + List.class.getCanonicalName() + "<T>" + "]的形式"
								+ "\r\n\t"
								;
				throw new IllegalArgumentException(m);
			}

			final Class<?> genericReturnType = getGenericReturnType(method);
			if (genericReturnType == null) {
				final String m =
						"\r\n\t"
								+ "@" + ZQuery.class.getSimpleName()
								+ "方法[" + myZRClass.getSimpleName() + "." + method.getName() + "]返回类型声明错误:"
								+ "\r\n\t"
								+ "返回类型必须声明为[" + List.class.getCanonicalName() + "<T>]带泛型参数的形式,"
								+ "当前无泛型参数"
								+ "\r\n\t"
								+ "请修改代码:把返回类型改为[" + List.class.getCanonicalName() + "<T>" + "]带泛型参数的形式"
								+ "\r\n\t"
								;
				throw new IllegalArgumentException(m);
			}

			subClassMethodName = "zQuerySelect";
			checkZQuerySelect(myZRClass, method, sqlTemplate);
		} else if (u.startsWith("UPDATE")) {
			checkZQueryUpdateDeleteInsert(myZRClass, method, returnType);
			subClassMethodName = "zQueryUpdate";
		} else if (u.startsWith("DELETE")) {
			checkZQueryUpdateDeleteInsert(myZRClass, method, returnType);
			subClassMethodName = "zQueryDelete";
		} else if (u.startsWith("INSERT")) {
			checkZQueryUpdateDeleteInsert(myZRClass, method, returnType);
			subClassMethodName = "zQueryInsert";
		} else {
			final String m = "@" + ZQuery.class.getSimpleName() + " 只支持 SELECT/UPDATE/DELETE/INSERT 语句";
			throw new IllegalArgumentException(m);
		}


		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}

		final String modeString = modeString(method);
		final String methodName1 = "\"" + method.getName() + "\"";

		final Class<?> returnType2 = getReturnType(method);

		return "return " + SU.class.getCanonicalName() + "." + subClassMethodName
				+ "(" + className1 + "," + methodName1 + "," + modeString + ","
				+ entityTName + ","
				+ returnType2.getCanonicalName() + ",sql," + joiner.toString() + ");";
	}

	private static void checkZQueryUpdateDeleteInsert(final Class<?> myZRClass, final Method method,
			final Class<?> returnType) {
		if (!Integer.class.equals(returnType)) {
			final String m =
					"\r\n\t"
							+ "@" + ZQuery.class.getSimpleName()
							+ "方法[" + myZRClass.getSimpleName() + "." + method.getName() + "]返回类型声明错误:"
							+ "\r\n\t"
							+ "返回类型必须声明为[" + Integer.class.getCanonicalName() + "]类型,当前类型为["
							+ returnType.getCanonicalName() + "]"
							+ "\r\n\t"
							+ "请修改代码:把返回类型改为[" + Integer.class.getCanonicalName() + "]类型"
							+ "\r\n\t"
							;
			throw new IllegalArgumentException(m);
		}
	}

	/**
	 * 校验 select 语句中的 ?占位符，必须组为一个数组排序后符合 ?1 ?2 ?3 的顺序
	 * @param myZRClass TODO
	 * @param method
	 * @param sqlTemplate
	 *
	 * @return
	 */
	public static int[] checkZQuerySelect(final Class<?> myZRClass, final Method method, final String sqlTemplate) {

		if (method.getParameterAnnotations().length <= 0) {
			return null;
		}

		final String k = method.getName() + "@" + sqlTemplate;
		synchronized (k.intern()) {

			final int[] v = (int[]) C.get(k);
			if(v !=null) {
				return v;
			}

			return checkZQuerySelect0(myZRClass, method, sqlTemplate, k);
		}
	}

	private static int[] checkZQuerySelect0(final Class<?> myZRClass, final Method method, final String sqlTemplate, final String k) {
		final String regex = "\\?(\\d+)";
		final Pattern pattern = Pattern.compile(regex);
		final java.util.regex.Matcher matcher = pattern.matcher(sqlTemplate);
		final int[] argOrderArray = new int[method.getParameters().length];

		int i = 0;
		boolean find = false;
		while (matcher.find()) {
			find = true;
			final String a = matcher.group(1);
			final int aV = Integer.parseInt(a);
			if (i >= argOrderArray.length) {
				throw new IllegalArgumentException(

						"\r\n\t"
								+ "@" + ZQuery.class.getSimpleName() + " 方法"
								+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]SQL参数个数声明错误:"
								+ "\r\n\t"
								+ "@"+ZQuery.class.getSimpleName() + ".sql=[" + sqlTemplate + "]"
								+ "\r\n\t"
								+ "方法参数个数有[" + method.getParameterCount() + "]个,当前已解析出["
								+ (i + 1) + "]个?占位符"
								+ "\r\n\t"
								+ "请检查代码:修改方法参数个数和?个数一致"
								+ "\r\n\t"
						);
			}

			argOrderArray[i] = aV;
			i++;
		}

		if (!find) {
			throw new IllegalArgumentException(
					"\r\n\t"
							+ "@" + ZQuery.class.getSimpleName() + " 方法"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]占位符声明错误:"
							+ "\r\n\t"
							+ "的自定义sql - [" + sqlTemplate + "] 中的?占位符必须符合 [?从1开始递增的数字] 的模式，如 ?1 ?2 ?3 "
							+ "\r\n\t"
					);
		}

		final int c = StrUtil.count(sqlTemplate, "?");
		if (c != method.getParameterCount()) {
			throw new IllegalArgumentException(
					"\r\n\t"
							+ "@" + ZQuery.class.getSimpleName() + " 方法"
							+ "[" + myZRClass.getSimpleName() + "." + method.getName() + "]SQL参数个数声明错误:"
							+ "\r\n\t"
							+ "方法参数个数[" + method.getParameterCount() + "]和SQL中?个数["+c+"]不一致"
							+ "\r\n\t"
							+ "@"+ZQuery.class.getSimpleName() + ".sql=[" + sqlTemplate + "]"
							+ "\r\n\t"
							+ "请检查代码:修改方法参数个数和?个数一致"
							+ "\r\n\t"

					);
		}

		Arrays.sort(argOrderArray);

		final HashSet<Integer> set = Sets.newHashSet();
		for (final Integer x : argOrderArray) {
			if (!set.add(x)) {
				throw new IllegalArgumentException(

						"\r\n\t"
								+ "@" + ZQuery.class.getSimpleName()
								+ "方法[" + myZRClass.getSimpleName() + "." + method.getName() + "]SQL中的?占位符声明错误:"
								+ "\r\n\t"
								+ "@"+ZQuery.class.getSimpleName() + ".sql=[" + sqlTemplate + "]"
								+ "\r\n\t"
								+ "?必须从?1开始递增,如?1 ?2 ?3...(不要求第一个必须是?1,也可以是[?2 ?3 ?1]等形式)"
								+ "\r\n\t"
								+ "请检查代码:修改当前SQL中的?为从从?1开始递增"
								+ "\r\n\t"
						) ;
			}
		}

		// FIXME 2024年6月23日 下午3:47:06 zhangzhen : debug 代码，记得删除
		if ("selectMaxBd1ByIdxxxx".equals(method.getName())) {
			final int x = 20;
		}

		for (int ix = 0; ix < argOrderArray.length; ix++) {
			if (argOrderArray[ix] != (ix + 1)) {
				throw new IllegalArgumentException(
						"\r\n\t"
								+ "@" + ZQuery.class.getSimpleName()
								+ "方法[" + myZRClass.getSimpleName() + "." + method.getName() + "]SQL中的?占位符声明错误:"
								+ "\r\n\t"
								+ "@"+ZQuery.class.getSimpleName() + ".sql=[" + sqlTemplate + "]"
								+ "\r\n\t"
								+ "?必须从?1开始递增,如?1 ?2 ?3...(不要求第一个必须是?1,也可以是[?2 ?3 ?1]等形式)"
								+ "\r\n\t"
								+ "请检查代码:修改当前SQL中的?为从从?1开始递增"
								+ "\r\n\t"
						) ;
			}
		}

		C.put(k, argOrderArray);
		return argOrderArray;
	}

	private static final WeakHashMap<String, Object> C = new WeakHashMap<>();

	private static Class<?> getReturnTypeAndCheckTFields(final Class<?> myZRClass, final Class<?> entityClass, final Method method) {
		final Class<?> returnType = getReturnType(method);
		if (returnType != method.getReturnType()) {

			final Field[] declaredFields = returnType.getDeclaredFields();
			final Field[] efs = entityClass.getDeclaredFields();
			for (final Field f : declaredFields) {

				if (f.isAnnotationPresent(ZTransient.class)) {
					continue;
				}

				final Optional<Field> findAny = Arrays.stream(efs).filter(ef -> ef.getName().equals(f.getName()))
						.findAny();
				if (!findAny.isPresent()) {
					continue;
				}

				final Field esF1 = findAny.get();
				if (!esF1.getType().equals(f.getType())) {
					final String m =
							"\r\n\t"
									+ "方法[" + myZRClass.getSimpleName() + "." + method.getName() + "]返回类型中的字段类型错误:"
									+ "\r\n\t"
									+ "返回类型[" + returnType.getSimpleName() + "]"
									+ "中的字段[" + f.getName() + "]类型["+f.getType().getCanonicalName()+"]"
									+ "\r\n\t"
									+ "和@" + ZEntity.class.getSimpleName()
									+ "对象[" + entityClass.getSimpleName() + "]中的字段[" + esF1.getName() + "]类型["
									+ esF1.getType().getCanonicalName() + "]不一致"
									+ "\r\n\t"
									+ "请修改代码:把[" + returnType.getSimpleName() +"." + f.getName() + "]类型修改为[" + esF1.getType().getCanonicalName() + "]"
									+ "\r\n\t"

							;
					throw new ZRepositoryException(m);
				}
			}

		}

		return returnType;
	}

	private static Class<?> getGenericReturnType(final Method method) {
		final Type returnType = method.getGenericReturnType();
		// 如果有泛型参数，如List<MyEntity>
		if (returnType instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) returnType;
			final Type[] typeArguments = parameterizedType.getActualTypeArguments();
			if (typeArguments.length > 0) {
				final Type typeArgument = typeArguments[0];
				if (typeArgument instanceof Class) {
					return (Class<?>) typeArgument;
				}
			}
		}
		return null;
	}

	private static Class<?> getReturnType(final Method method) {

		final Type returnType = method.getGenericReturnType();
		// 如果有泛型参数，如List<MyEntity>
		if (returnType instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) returnType;
			final Type[] typeArguments = parameterizedType.getActualTypeArguments();
			if (typeArguments.length > 0) {
				final Type typeArgument = typeArguments[0];
				if (typeArgument instanceof Class) {
					return (Class<?>) typeArgument;
				}
			}
		}

		return method.getReturnType();
	}

	private static String findByXX(final Class<?> myZRClass, final Class<?> entityClass, final String className1,
			final Method method, final String methodRegex) {

		checkParameterTypeAndName(myZRClass, entityClass, method, methodRegex);

		final StringJoiner joiner = getParameterNameFromMethod(method);
		final String modeString = modeString(method);

		final Class<?> returnType = getReturnTypeAndCheckTFields(myZRClass, entityClass, method);

		final String methodName = StrUtil.count(joiner.toString(), DELIMITER) == 0 ? "findByXX" : "findByXXAndXX";
		final String methodName1 = "\"" + method.getName() + "\"";

		return "return " + SU.class.getCanonicalName() + "." + methodName + "(" + className1 + "," + methodName1 + "," + modeString + ",classType,"+returnType.getCanonicalName()+",sql,"
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

		checkZEntityPrimaryKey(typeClass, name, getPoolInstance(ze.dataSourceName()).getZConnection(Mode.WRITE), ze.dataSourceName());
		checkZEntityPrimaryKey(typeClass, name, getPoolInstance(ze.dataSourceName()).getZConnection(Mode.READ), ze.dataSourceName());

		checkZEntityFiled(typeClass, getPoolInstance(ze.dataSourceName()).getZConnection(Mode.WRITE));
		checkZEntityFiled(typeClass, getPoolInstance(ze.dataSourceName()).getZConnection(Mode.READ));

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

		final long zversionCount = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZVersion.class)).count();
		if (zversionCount > 1) {
			final String m = "@" + ZEntity.class.getSimpleName()
					+ "类[" + typeClass.getSimpleName() + "] "
					+ "只允许有一个 @" + ZVersion.class + " 字段,"
					+ "当前有[" + zversionCount + "]个"
					;
			throw new IllegalArgumentException(m);
		}

		for (final Field f : fs) {
			// FIXME 2024年5月27日 下午8:49:49 zhangzhen: 继续测试命名
			final String name = f.getName();

			if (ZFieldConverter.UPPERCASE_LETTER.contains(name.charAt(0))) {
				final String m = "@" + ZEntity.class.getSimpleName()
						+ "类[" + typeClass.getSimpleName() + "] 中的Field ["
						+ name
						+ "] 不允许以大写字母开头,请严格遵循驼峰式命名法"
						;
				throw new IllegalArgumentException(m);
			}

			if (name.contains("_")) {
				final String m = "@" + ZEntity.class.getSimpleName()
						+ "类[" + typeClass.getSimpleName() + "] 中的Field ["
						+ name
						+ "] 不允许出现_下划线符号,请严格遵循驼峰式命名法"
						;
				throw new IllegalArgumentException(m);
			}
		}

		final List<Field> fieldList = Lists.newArrayList(fs);

		final String dataSourceName = typeClass.getAnnotation(ZEntity.class).dataSourceName();

		final Optional<Field> notSupport = fieldList.stream().filter(f -> !DBType.typeSupport(dataSourceName, f.getType().getCanonicalName())).findAny();
		if (notSupport.isPresent()) {
			final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);
			final Multimap<String, String> sm = DBType.getAllSupportType(dbEnum);
			final Multiset<String> keys = sm.keys();
			final String sts = keys.stream()
					.distinct()
					. collect(Collectors.joining(",","[","]"));
			final String m =
					"\r\n\t"
							+ dbEnum.name() + ":"
							+ "@" + ZEntity.class.getSimpleName()
							+ "类[" + typeClass.getSimpleName() + "] 中的字段 ["
							+ notSupport.get().getName()
							+ "] 的类型 ["
							+ notSupport.get().getType().getCanonicalName()
							+ "] 不支持"
							+ "\r\n\t"
							+ "支持类型为:"
							+ "\r\n\t"
							+  sts
							+ "\r\n\t"
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

			final DataSourceDTO dataSourceDTO = findCatalog(zConnection.getUrl());
			try (ResultSet columns = metaData.getColumns(dataSourceDTO.getCatalog(), null, tableName, null)) {
				System.out.println("开始校验[" + zConnection.getMode().name() + "]数据表 = " + tableName);

				int columnsCount = 0;
				final List<String> columnNameList = Lists.newArrayList();

				while (columns.next()) {
					columnsCount++;
					final String columnName = columns.getString(COLUMN_NAME);
					columnNameList.add(columnName);
					final String columnType = columns.getString(TYPE_NAME);

					final String javaFieldName = ZFieldConverter.toJavaField(columnName);
					final Optional<Field> o = fieldList.stream().filter(fn -> fn.getName().equals(javaFieldName)).findAny();

					if (!o.isPresent()) {
						final Optional<Field> oequalsIgnoreCase = fieldList.stream()
								.filter(fn -> fn.getName().equalsIgnoreCase(javaFieldName)).findAny();
						if (oequalsIgnoreCase.isPresent()) {
							final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);
							final String m = "\r\n\t"
									+  dbEnum.name()
									+ ":"
									+ "数据表[" + tableName + "]中存在"
									+ "[" + typeClass.getSimpleName() + "]中不存在的column [" + columnName + "]"
									+ " ,是否手误写错了?想写的是java Field [" + oequalsIgnoreCase.get().getName() +"]"
									+ " 转换为DB的下划线命名法后的 ["+ ZFieldConverter.toDbField(oequalsIgnoreCase.get().getName()) +"] ?)"
									;
							throw new IllegalArgumentException(m);
						}
						final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);
						final String m =
								"\r\n\t"
										+  dbEnum.name()
										+ ":"
										+ "数据表[" + tableName + "]中存在" + "@" + ZEntity.class.getSimpleName() + "类["
										+ typeClass.getSimpleName() + "]不存在的字段" + columnName
										+ " 此字段从转为java驼峰式命名为[" + javaFieldName + "]"
										;
						throw new IllegalArgumentException(m);

					}

					final boolean match = DBType.match(dataSourceDTO.getDbEnum(), o.get().getType().getCanonicalName(), columnType);
					if (!match) {
						final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);
						final String m =
								"\r\n\t"
										+  dbEnum.name()
										+ ":"
										+ "@" + ZEntity.class.getSimpleName() + "类[" + typeClass.getSimpleName()
										+ "]中的字段[" + o.get().getName() + "] 类型 [" + o.get().getType().getCanonicalName()
										+ "] 与数据表[" + tableName + "] 的字段 [" + columnName + "] 类型 [" + columnType + "] 不匹配"
										+ "\r\n\t"
										+ "java类型和DB类型匹配关系请看 @see " + DBType.class.getCanonicalName()
										+ "\r\n\t"

										;
						throw new IllegalArgumentException(m);
					}

					checkZCT(typeClass, o, ZCreateTime.class, ZCreateTimeHandler.SUPPORTED_CLASS_SET);
					checkZCT(typeClass, o, ZUpdateTime.class, ZUpdateTimeHandler.SUPPORTED_CLASS_SET);

					checkZCT(typeClass, o, ZVersion.class, ZVersionHandler.SUPPORTED_CLASS_SET);

				}

				final List<String> fieldNameList = Arrays.stream(fs).map(Field::getName)
						.collect(Collectors.toList());

				if (fieldNameList.size() != columnsCount) {
					final List<String> cnl = columnNameList.stream().map(ZFieldConverter::toJavaField)
							.collect(Collectors.toList());
					final HashSet<String> cns = Sets.newHashSet(cnl);
					if (cnl.size() != cns.size()) {
						final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);
						final List<Entry<String, Long>> collect = cnl.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting())).entrySet().stream().filter(e-> e.getValue().longValue() > 1).collect(Collectors.toList());
						final String m =
								"\r\n\t"
										+  dbEnum.name()
										+ ":"
										+ "数据表[" + tableName + "]" + "中的column"
										+ "\r\n\t\t\t"
										+ columnNameList
										+ "转换为java驼峰式"
										+ "\r\n\t\t\t"
										+ cnl
										+ "命名后有重复的,会导致处理数据时混乱."
										+ "\r\n\t\t\t"
										+ "转换后的java名称和出现次数如下："
										+ "\r\n\t\t\t"
										+ collect
										+ "请检查";
						throw new IllegalArgumentException(m);
					}

					if (fieldNameList.size() > columnNameList.size()) {
						fieldNameList.removeAll(cnl);

						final List<Field> c = fieldList.stream().filter(f -> fieldNameList.contains(f.getName()))
								.collect(Collectors.toList());

						final List<Field> noZTransientFieldList = c.stream().filter(f -> !f.isAnnotationPresent(ZTransient.class)).collect(Collectors.toList());

						if (noZTransientFieldList.size() > 0) {
							final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);
							final String m = "\r\n\t"
									+  dbEnum.name()
									+ ":"
									+ "@" + ZEntity.class
									.getSimpleName() + "类[" + typeClass.getSimpleName()
									+ "]中存在数据表[" + tableName + "]中不存在的字段" + noZTransientFieldList + EMTPY
									+ "，如需与数据表字段对应，请在数据表中加入此字段；如不需与数据表对应，请在字段上加入@" + ZTransient.class.getCanonicalName() + " 注解"
									;
							throw new IllegalArgumentException(m);
						}

					} else if (fieldNameList.size() < columnNameList.size()) {

						cnl.removeAll(fieldNameList);

						final DBEnum dbEnum = ZCPool.getInstance(dataSourceName).getDbEnum(Mode.WRITE);

						final String m =
								"\r\n\t"
										+  dbEnum.name()
										+ ":"
										+ "数据表[" + tableName + "]中存在" + "@" + ZEntity.class.getSimpleName() + "类["
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
			getPoolInstance(typeClass.getAnnotation(ZEntity.class).dataSourceName()).returnZConnectionAndCommit(zConnection);
		}

	}

	private static void checkZCT(final Class<?> typeClass, final Optional<Field> o, final Class<? extends Annotation> checkClass, final ImmutableSet<Class<?>> supportedClassSet) {
		final Field field = o.get();
		if (field.isAnnotationPresent(checkClass)) {
			final Optional<Class<?>> findAny2 = supportedClassSet.stream()
					.filter(x -> x.equals(field.getType())).findAny();

			if (!findAny2.isPresent()) {

				final String m =
						"\r\n\t"
								+ "@" + checkClass.getSimpleName() + "字段["
								+ typeClass.getSimpleName()
								+ "." + o.get().getName() + "] 的类型 [" + o.get().getType().getCanonicalName()
								+ "] 不支持"
								+ "\r\n\t"
								+ "支持类型为: " + supportedClassSet
								+ "\r\n\t"
								+ "请检查代码:修改为" + supportedClassSet + "其中之一"
								+ "\r\n\t"

								;
				throw new IllegalArgumentException(m);
			}
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

	private static void checkZEntityPrimaryKey(final Class<?> typeClass, final String name, final ZConnection zConnection, final String dataSourceName) {
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
			getPoolInstance(dataSourceName).returnZConnectionAndCommit(zConnection);
		}
	}

	private static D findFieldName(final Class<?> entityClass, final String methodName) {

		final D d = new D();
		d.setMethodName(methodName);

		String m = methodName;
		final Field[] fs = entityClass.getDeclaredFields();

		Arrays.sort(fs, (o1, o2) -> {
			final String name1 = o1.getName();
			final String name2 = o2.getName();
			return Integer.compare(name2.length(), name1.length());
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
		final Field[] fa = entityClass.getDeclaredFields();
		for (final Field element : fa) {
			final Optional<String> o = fn.stream().filter(f -> f.equals(element.getName())).findFirst();
			if(!o.isPresent()) {
				continue;
			}
			filedNameOriginalOrder.add(o.get());
		}

		d.setFiledNameOriginalOrder(filedNameOriginalOrder);

		final List<String> fieldNameArray = getFieldNameArray(methodName, fs);
		d.setFiledNameMethodNameOrder(fieldNameArray);

		return d;
	}

	private static boolean isMethodNameAllSQLKeyword(final String methodName) {
		final HashSet<String> sqlKeyword = SqlPattern.SQL_KEYWORD;
		// SQL关键字按从长到短排序，防止出现 Or优先于Order被替换掉，剩余 der
		final ArrayList<String> skList = Lists.newArrayList(sqlKeyword);
		skList.sort(Comparator.comparing(String::length).reversed());
		String mn = methodName;
		for (final String sk : skList) {
			mn = mn.replaceAll(sk, EMTPY);
		}
		return mn.isEmpty();
	}

	private static List<String> getFieldNameArray(final String methodName,final Field[] fs) {
		final ArrayList<String> x = Lists.newArrayList();
		// FIXME 2024年5月18日 上午10:02:04 zhangzhen: debug 代码，记得删除
		if("findByNameIsNullAndByte1IsNullAndCreateTimeAndByte1".equals(methodName)) {
			final int x2 = 1;
		}

		final ArrayList<Field> fl = Lists.newArrayList(fs);
		final List<String> fnl = fl.stream().map(Field::getName)
				.map(n -> String.valueOf(n.charAt(0)).toUpperCase() + n.substring(1)).collect(Collectors.toList());

		final HashSet<String> sqlKeyword = SqlPattern.SQL_KEYWORD;
		// SQL关键字按从长到短排序，防止出现 Or优先于Order被替换掉，剩余 der
		final ArrayList<String> skList = Lists.newArrayList(sqlKeyword);
		skList.sort(Comparator.comparing(String::length).reversed());

		final AtomicReference<String> ffN = new AtomicReference<>(methodName);

		while (true) {
			if (isMethodNameAllSQLKeyword(ffN.get())) {
				break;
			}

			final List<String> ol = fnl.stream().filter(fn -> ffN.get().indexOf(fn) > 0).collect(Collectors.toList());
			if (ol.isEmpty()) {
				break;
			}
			ol.sort(Comparator.comparing(s -> ffN.get().indexOf(s)));
			final String firstFieldNameFromMethodName = ol.get(0);
			final String ffNNew = ffN.get().replaceFirst(firstFieldNameFromMethodName, EMTPY);
			ffN.set(ffNNew);
			x.add(firstFieldNameFromMethodName);
		}

		return x;
	}

	/**
	 * @param url
	 * @return
	 */
	public static DataSourceDTO findCatalog(final String url) {
		final String k = "findCatalog" + "@" + url;
		final Object d = C.get(k);
		if (d != null) {
			return (DataSourceDTO) d;
		}

		synchronized (k.intern()) {
			final DataSourceDTO d2 = findCatalog0(url);
			C.put(k, d2);
			return d2;
		}

	}

	private static DataSourceDTO findCatalog0(final String url) {
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

			return new DataSourceDTO(x, DBEnum.MYSQL);
		}
		if (url.toLowerCase().contains("postgresql")) {
			final String keyword = "/";
			final int lastIndexOf = url.lastIndexOf(keyword, url.length());

			final String substring = url.substring(lastIndexOf + keyword.length());

			return new DataSourceDTO(substring, DBEnum.POSTGRESQL);
		}

		if (url.toLowerCase().contains("sqlite")) {
			final String keyword = "/";
			final String k = ".db";
			final int i = url.lastIndexOf(k);
			if (i > -1) {
				final int is = url.lastIndexOf("\\", i);
				if (is > -1) {
					final String name = url.substring(is + 1, i);
					final int x = 1;
					return new DataSourceDTO(name, DBEnum.SQLITE);
				}
			}
			throw new IllegalArgumentException("sqlite 配置不支持：" + url);
		}

		throw new DBNotSupportException("JDBC 配置不支持：" + url);
	}

	private static void checkZRG(final String tType, final String idType, final Class zrSubClass) {

		try {
			final Class<?> tClass = Class.forName(tType);
			final Field[] fs = tClass.getDeclaredFields();
			final Optional<Field> idO = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZID.class)).findAny();
			if (!idO.isPresent()) {
				throw new IllegalArgumentException(ZEntity.class.getSimpleName() + " 类型 " + tClass.getSimpleName()
				+ " 必须有 " + ZID.class.getSimpleName() + " 字段");
			}
			final Field idField = idO.get();

			if (!idField.getType().getCanonicalName().equals(idType)) {
				final String m = ZRepository.class.getSimpleName() + " 的子接口 " + zrSubClass.getSimpleName()
				+ " 泛型参数<T,ID> 设置错误. "
				+ "泛型参数ID类型("+idType+")应和T("+tClass.getSimpleName()+")中的@" + ZID.class.getSimpleName()
				+ "标记的Field的类型("+idField.getType().getCanonicalName()+")保持一致";
				throw new IllegalArgumentException(m);
			}

		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static DBEnum getDB(final String dataSourceName) {
		final ZCPool pool = ZCPool.getInstance(dataSourceName);
		final DBEnum dbEnum = pool.getDbEnum(Mode.WRITE);
		return dbEnum;
	}

	private static StringJoiner getParameterNameFromMethod(final Method method) {
		final StringJoiner joiner = new StringJoiner(DELIMITER);
		for (final Parameter parameter : method.getParameters()) {
			joiner.add(parameter.getName());
		}
		return joiner;
	}

	private static List<Field> gNoZTransientFieldList(final Class typeClass) {
		final Field[] fs = typeClass.getDeclaredFields();
		final List<Field> fl = Arrays.stream(fs).filter(f -> !f.isAnnotationPresent(ZTransient.class))
				.collect(Collectors.toList());

		return fl;

	}

	private static void checkParameterNameAndFiledNameMethodNameOrderEquals(final Class zrSubclass,
			final Method method, final List<String> filedNameMethodNameOrderList) {

		final Parameter[] parameters = method.getParameters();

		if (parameters.length != filedNameMethodNameOrderList.size()) {
			throw new IllegalArgumentException("parameters.length(" + parameters.length
					+ ")必须和filedNameMethodNameOrderList.size(" + filedNameMethodNameOrderList.size() + ")一致");
		}

		for(int i=0;i<parameters.length;i++) {
			final Parameter p = parameters[i];
			final String fn = filedNameMethodNameOrderList.get(i);
			if(!p.getName().equals(ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fn)))) {
				throw new IllegalArgumentException(
						"\r\n\t"
								+ "[" + zrSubclass.getSimpleName() + "." + method.getName() + "]"
								+ "参数声明错误:"
								+ "\r\n\t"
								+ "参数名称[" + p.getName()
								+ "]必须和方法声明中的["
								+ fn + "]转为驼峰式命名的["
								+ ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fn)) + "]一致"
								+ "\r\n\t"
								+ "请检查代码:把参数声明改为(" + p.getType().getSimpleName() +  " " + ZFieldConverter.toJavaField(ZFieldConverter.toDbField(fn)) + ")"
								+ "\r\n\t"
						)
				;
			}
		}

	}

	/**
	 * 	校验声明错误的methodName，比如：存在一个Field:name
	 * 	声明方法：findByNaem(String name)
	 *
	 * 	上面方法名称写成了Naem拼写错了，本方法尽力匹配出可能的名称，
	 * 	如本例中的正确名称为findByName(String name)，本方法就干这个
	 *
	 * @param entityClass
	 * @param methodName
	 * @param methodRegex
	 * @return
	 */
	private static List<String> biwu(final Class<?> entityClass,final String methodName, final String methodRegex) {
		final Field[] fs = entityClass.getDeclaredFields();
		final List<Field> fl = Arrays.stream(fs).filter(f -> !f.isAnnotationPresent(ZTransient.class))
				.collect(Collectors.toList());

		final D d = findFieldName(entityClass, methodName);
		final String sk = d.getSqlKeyword();

		final AtomicReference<String> sk2 = new AtomicReference<>();
		sk2.set(sk);
		final HashSet<String> sqlKeyword = SqlPattern.SQL_KEYWORD;

		for (final String x : sqlKeyword) {
			sk2.set(sk2.get().replace(x, EMTPY));
		}

		switch (methodRegex) {
		case MethodRegex.findByXX:
			final Map<Field, Integer> sMap = fl.stream().collect(Collectors.toMap(f -> f, f -> S.apply(f.getName(), sk2.get())));
			final Set<Entry<Field, Integer>> entrySet = sMap.entrySet();
			final ArrayList<Entry<Field, Integer>> l = Lists.newArrayList(entrySet);

			final Optional<Entry<Field, Integer>> min = l.stream().min(Comparator.comparing(Entry::getValue));

			final List<Entry<Field, Integer>> ml = l.stream().filter(e -> e.getValue().equals(min.get().getValue()))
					.collect(Collectors.toList());

			final List<String> possibleML = ml.stream().map(
					e -> MethodRegex.findByXX.replaceAll("\\.\\+", ZFieldConverter.toMethodName(e.getKey().getName())))
					.collect(Collectors.toList());

			return possibleML;

			// FIXME 2024年6月10日 下午9:26:46 zhangzhen : TODO case 其他的继续写

		default:
			break;
		}

		return Collections.emptyList();
	}
}


