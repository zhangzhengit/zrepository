package com.vo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cn.hutool.core.util.StrUtil;

/**
 * ZRepository 方法命名规则的正则表达式
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
// FIXME 2023年6月15日 下午7:42:24 zhanghen: 参照 https://www.fcors.com/archives/1719 全完成
public class MethodRegex {

	public static final String SELECT = "SELECT";
	public static final String DELETE = "DELETE";
	public static final String UPDATE = "UPDATE";
	public static final String FROM = "FROM";
	public static final String WHERE = "WHERE";
	public static final String LIMIT = "LIMIT";
	public static final String OFFSET = "OFFSET";
	public static final String COLUMN = "COLUMN";
	public static final String COLUMNS = "COLUMNS";
	public static final String OR = "OR";
	public static final String AND = "AND";
	public static final String ZID = "ZID";
	public static final String COLUMN_VALUES = "COLUMN_VALUES";

	public static final String GROUP_findByXXOrderByXXLimit = "findBy.+OrderBy.+Limit";
	public static final String GROUP_findByXXOrderByXXDescLimit = "findBy.+OrderBy.+DescLimit";
	public static final String GROUP_count = "count";

	public static final String GROUP_page = "page";

	public static final String GROUP_CountingByXXX = "countingBy.+";
	public static final String GROUP_CountingByXXXNot = "countingBy.+Not";
	public static final String GROUP_EXISTBYId = "existById";
	public static final String GROUP_DeleteById = "deleteById";
	public static final String GROUP_DeleteByIdIn = "deleteByIdIn";
	public static final String GROUP_SAVEALL = "saveAll";
	public static final String GROUP_SAVE = "save";
	public static final String GROUP_QUERY = "query";
	public static final String GROUP_FIND = "find";

	public static final String GROUP_UPDATE = "update";
	public static final String GROUP_FINDALL = "findAll";

	public static final String GROUP_findByXX = "findBy.+";
	public static final String GROUP_findByxxNot = "findBy.+Not";
	public static final String GROUP_findByxxNotNull = "findBy.+NotNull";
	public static final String GROUP_findByxx_in = "findBy.+In";

	public static final String GROUP_findByXXXEndingWith= "findBy.+EndingWith";
	public static final String GROUP_findByXXXStartingWith= "findBy.+StartingWith";

	public static final String GROUP_findByXXGreaterThanEquals = "findBy.+GreaterThanEquals";

	public static final String GROUP_findByXXGreaterThan = "findBy.+GreaterThan";
	public static final String GROUP_findByXXLessThanEquals = "findBy.+LessThanEquals";
	public static final String GROUP_findByXXLessThan = "findBy.+LessThan";

	public static final String GROUP_findByXXLike = "findBy.+Like";


	// FIXME 2024年11月27日 下午9:46:29 zhangzhen : 开始支持findByXXLikeAndXX这种形式
	// FIXME 2024年11月27日 下午11:41:00 zhangzhen : 记得判断第一个参数必须是char/string
	public static final String GROUP_findByXXLikeAndXX = "findBy.+LikeAnd.+";
	public static final String GROUP_findByXXLikeAndXXAndXX = "findBy.+LikeAnd.+And.+";


	public static final String GROUP_findByXXNotLike = "findBy.+NotLike";

	// FIXME 2024年11月27日 下午10:59:35 zhangzhen : 写findByXXLikeAndXX时遇到了传值null/""的问题，
	// 此时我在考虑是否提供出对于String/char的isEmpty方法，并且所有方法都按方法名理解，用户如需兼顾
	// null/""/正常值，则自己组合这些方法来实现？

	// FIXME 2024年11月27日 下午11:41:19 zhangzhen : 记得所有isEmpty的参数都判断必须是char/string
	public static final String GROUP_findByXXIsEmptyAndXXAndXX = "findBy.+IsEmptyAnd.+And.+";
	public static final String GROUP_findByXXIsEmptyAndXX = "findBy.+IsEmptyAnd.+";
	public static final String GROUP_findByXXIsEmpty = "findBy.+IsEmpty";

	public static final String GROUP_findByXXIsNull = "findBy.+IsNull";
	public static final String GROUP_findByXXIsNullAndXX = "findBy.+IsNullAnd.+";
	public static final String GROUP_findByXXIsNullAndXXAndXX = "findBy.+IsNullAnd.+And.+";
	public static final String GROUP_findByXXIsNullAndXXAndXXAndXX = "findBy.+IsNullAnd.+And.+And.+";

	public static final String GROUP_findByXXIsNullAndXXIsNullAndXX = "findBy.+IsNullAnd.+IsNullAnd.+";
	public static final String GROUP_findByXXIsNullAndXXIsNullAndXXAndXX = "findBy.+IsNullAnd.+IsNullAnd.+And.+";

	public static final String saveAll = GROUP_SAVEALL;
	public static final String save = GROUP_SAVE;
	public static final String findAll = GROUP_FINDALL;
	public static final String findByXX = GROUP_findByXX;

	public static final String GROUP_findByXXAndXX = "findBy.+(?=And.).+";
	public static final String findByXXAndYY = "findBy.+(?=And.).+";
	public static final String findByXXAndYYAndYY = "findBy.+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";
	public static final String findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY = "findBy.+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+(?=And.).+";

	public static final String findByXXXEndingWith = GROUP_findByXXXEndingWith;
	public static final String findByXXXStartingWith = GROUP_findByXXXStartingWith;
	public static final String findByXXNot = GROUP_findByxxNot;
	public static final String findByXXIn = GROUP_findByxx_in;
	public static final String findByXXInAndYYIn = "findBy.+(?=InAnd.).+In";
	public static final String findByXXInAndYYInAndYYIn = "findBy.+(?=InAnd.).+(?=InAnd.).+In";
	public static final String findByXXInAndYYInAndYYInAndYYIn = "findBy.+(?=InAnd.).+(?=InAnd.).+(?=InAnd.).+In";

	// FIXME 2024年5月27日 下午8:13:34 zhangzhen: 正则表达式都要改，.+ 是0个也可以的，显然不符合要求
	public static final String findByXXOrYY = "findBy.+(?=Or.).+";
	public static final String findByXXOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+";
	public static final String findByXXOrYYOrYYOrYY = "findBy.+Or.+Or.+Or.+";
	public static final String findByXXOrYYOrYYOrYYOrYY = "findBy.+Or.+Or.+Or.+Or.+";
	public static final String findByXXOrYYOrYYOrYYOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+";
	public static final String findByXXOrYYOrYYOrYYOrYYOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+";
	public static final String findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+";
	public static final String findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+";
	public static final String findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+";
	public static final String findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY = "findBy.+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+(?=Or.).+";

	public static final String findByXXBetween = "findBy.+Between";
	public static final String findByXXNotBetween = "findBy.+NotBetween";

	public static final String findByXXGreaterThanEquals = GROUP_findByXXGreaterThanEquals;
	public static final String findByXXGreaterThan = GROUP_findByXXGreaterThan;
	public static final String findByXXLessThanEquals = GROUP_findByXXLessThanEquals;
	public static final String findByXXLessThan = GROUP_findByXXLessThan;

	public static final String findByXXOrderByXXDescLimit = GROUP_findByXXOrderByXXDescLimit;
	public static final String findByXXAndXXOrderByXXDescLimit = "findBy.+And.+OrderBy.+DescLimit";
	public static final String findByXXAndXXAndXXOrderByXXDescLimit = "findBy.+And.+And.+OrderBy.+DescLimit";
	public static final String findByXXAndXXAndXXAndXXOrderByXXDescLimit = "findBy.+And.+And.+And.+OrderBy.+DescLimit";
	public static final String findByXXAndXXAndXXAndXXAndXXOrderByXXDescLimit = "findBy.+And.+And.+And.+And.+OrderBy.+DescLimit";
	public static final String findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXDescLimit = "findBy.+And.+And.+And.+And.+And.+OrderBy.+DescLimit";
	public static final String findByXXOrderByXXLimit = GROUP_findByXXOrderByXXLimit;
	public static final String findByXXAndXXOrderByXXLimit = "findBy.+And.+OrderBy.+Limit";
	public static final String findByXXAndXXAndXXOrderByXXLimit = "findBy.+And.+And.+OrderBy.+Limit";
	public static final String findByXXAndXXAndXXAndXXOrderByXXLimit = "findBy.+And.+And.+And.+OrderBy.+Limit";
	public static final String findByXXAndXXAndXXAndXXAndXXOrderByXXLimit = "findBy.+And.+And.+And.+And.+OrderBy.+Limit";
	public static final String findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXLimit = "findBy.+And.+And.+And.+And.+And.+OrderBy.+Limit";
	public static final String count = GROUP_count;
	public static final String page = GROUP_page;
	public static final String countingByXXX = GROUP_CountingByXXX;
	public static final String countingByXXXAndXX = "countingBy.+And.+";
	public static final String countingByXXXAndXXAndXX = "countingBy.+And.+And.+";
	public static final String countingByXXXAndXXAndXXAndXX = "countingBy.+And.+And.+And.+";

	public static final String countingByXXXNot = GROUP_CountingByXXXNot;
	public static final String countingByXXXNotAndXXXNot = "countingBy.+NotAnd.+Not";
	public static final String countingByXXXNotAndXXXNotAndXXXNot = "countingBy.+NotAnd.+NotAnd.+Not";
	public static final String countingByXXXNotAndXXXNotAndXXXNotAndXXXNot = "countingBy.+NotAnd.+NotAnd.+NotAnd.+Not";

	public static final String existById = GROUP_EXISTBYId;
	public static final String existByIdIn = "existByIdIn";
	public static final String deleteById = GROUP_DeleteById;
	public static final String deleteByIdIn = GROUP_DeleteByIdIn;
	public static final String deleteAll = "deleteAll";

	public static final String findByXXLike = GROUP_findByXXLike;
	// IsNull
	public static final String findByXXIsNull = GROUP_findByXXIsNull;

	public static ArrayList<String> regexList = Lists.newArrayList();

	public final static HashMap<String, HashMap<String, String>> R_M = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXOrderByXXDescLimit = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXOrderByXXLimit = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_Count = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_GROUP_pageByXX_orderByXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_PAGE = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_CountingByXXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_CountingByXXXNot = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXNotBetween = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXBetween = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXOrYY = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_EXISTBYIDIN = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_EXISTBYID = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_DELETEBYID = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_SAVEALL = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_FIND = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_QUERY = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_SAVE = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_UPDATE = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_FINDBYXXAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_FINDBYXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXIsEmpty = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXIsNull = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXIsNullAndXXAndXXAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXIsNullAndXXIsNullAndXXAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXIsNullAndXXIsNullAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXIsNullAndXXAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXIsNullAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXNotLike = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXLikeAndXX = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXLike = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXXEndingWith = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_StartingWith = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXNot = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_findByXXNotNull = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_FINDBYXXIN = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_GreaterThanEquals = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_GreaterThan = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_LessThanEquals = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_LessThan = new LinkedHashMap<>();
	public final static HashMap<String, String> REGEX_MAP_FINDALL = new LinkedHashMap<>();

	public final static HashMap<String, String> REGEX_MAP_BETWEEN = new LinkedHashMap<>();

	static {

		// findByXXOrderByXXLimit
		REGEX_MAP_findByXXOrderByXXLimit.put(findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXLimit, SELECT + " * " + FROM
				+ " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?) ORDER BY @ ASC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXLimit.put(findByXXAndXXAndXXAndXXAndXXOrderByXXLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?) ORDER BY @ ASC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXLimit.put(findByXXAndXXAndXXAndXXOrderByXXLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ?) ORDER BY @ ASC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXLimit.put(findByXXAndXXAndXXOrderByXXLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ?) ORDER BY @ ASC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXLimit.put(findByXXAndXXOrderByXXLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ?) ORDER BY @ ASC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXLimit.put(findByXXOrderByXXLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ = ? ORDER BY @ ASC LIMIT ? OFFSET ?");

		// findByXXOrderByXXDescLimit
		REGEX_MAP_findByXXOrderByXXDescLimit.put(findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXDescLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?) ORDER BY @ DESC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXDescLimit.put(findByXXAndXXAndXXAndXXAndXXOrderByXXDescLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?) ORDER BY @ DESC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXDescLimit.put(findByXXAndXXAndXXAndXXOrderByXXDescLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ?) ORDER BY @ DESC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXDescLimit.put(findByXXAndXXAndXXOrderByXXDescLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ?) ORDER BY @ DESC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXDescLimit.put(findByXXAndXXOrderByXXDescLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ?) ORDER BY @ DESC LIMIT ? OFFSET ?");
		REGEX_MAP_findByXXOrderByXXDescLimit.put(findByXXOrderByXXDescLimit, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ = ? ORDER BY @ DESC LIMIT ? OFFSET ?");

		// page
		// FIXME 2024年5月14日 下午10:18:45 zhangzhen: page 暂时还只支持 ZR.page 方法，继续支持
		REGEX_MAP_PAGE.put(page, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " " + COLUMN + " LIMIT ? OFFSET ?");

		// count
		REGEX_MAP_Count.put(count, SELECT + " count(*) " + FROM + " TABLE_NAME" + " " + WHERE + ZRWrapper.ALWAYS_TRUE);

		// findByXXNotBetween
		REGEX_MAP_findByXXNotBetween.put(findByXXNotBetween, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ NOT BETWEEN ? AND ?)");
		// findByXXBetween
		REGEX_MAP_findByXXBetween.put(findByXXBetween, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ BETWEEN ? AND ?)");

		// countingByXXXNot
		REGEX_MAP_CountingByXXXNot.put(countingByXXXNotAndXXXNotAndXXXNotAndXXXNot, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " (@ <> ? AND @ <> ? AND @ <> ? AND @ <> ?)");
		REGEX_MAP_CountingByXXXNot.put(countingByXXXNotAndXXXNotAndXXXNot, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " (@ <> ? AND @ <> ? AND @ <> ?)");
		REGEX_MAP_CountingByXXXNot.put(countingByXXXNotAndXXXNot, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " (@ <> ? AND @ <> ?)");
		REGEX_MAP_CountingByXXXNot.put(countingByXXXNot, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " @ <> ?");

		// countingByXXX
		REGEX_MAP_CountingByXXX.put(countingByXXXAndXXAndXXAndXX, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_CountingByXXX.put(countingByXXXAndXXAndXX, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_CountingByXXX.put(countingByXXXAndXX, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ?)");
		REGEX_MAP_CountingByXXX.put(countingByXXX, SELECT + " count(*) " + FROM + " TABLE_NAME " + WHERE + " @ = ?");
		// findByXXOrXX 支持2个到11个条件的
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYYOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ? OR @ = ?)");
		REGEX_MAP_findByXXOrYY.put(findByXXOrYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? OR @ = ?)");

		// existByIdIn
		REGEX_MAP_EXISTBYIDIN.put(existByIdIn,
				SELECT + " " + ZID + ",COUNT(*) " + FROM + " TABLE_NAME " + WHERE + ZRWrapper.ALWAYS_TRUE + " AND @ IN (?) GROUP BY " + ZID + "");
		// existById
		REGEX_MAP_EXISTBYID.put(existById, SELECT + " COUNT(*) " + FROM + " TABLE_NAME " + WHERE + " @ = ?");

		// deleteById
		REGEX_MAP_DELETEBYID.put(deleteById, "DELETE " + FROM + " TABLE_NAME " + WHERE + " @ = ?");
		REGEX_MAP_DELETEBYID.put(deleteAll, "DELETE " + FROM + " TABLE_NAME"  + " " + WHERE + ZRWrapper.ALWAYS_TRUE);

		// deleteByIdIn
		REGEX_MAP_DELETEBYID.put(deleteByIdIn, "DELETE " + FROM + " TABLE_NAME " + WHERE + " @ IN (?)");

		// saveAll
		REGEX_MAP_SAVEALL.put(saveAll, "INSERT INTO TABLE_NAME (" + COLUMNS + ") VALUES (" + COLUMN_VALUES + ")");
		// query
		REGEX_MAP_QUERY.put(GROUP_QUERY, "QUERY");
		// save
		REGEX_MAP_SAVE.put(save, "INSERT INTO TABLE_NAME (" + COLUMNS + ") VALUES (" + COLUMN_VALUES + ")");
		// find
		REGEX_MAP_FIND.put(GROUP_FIND, SELECT + " * " + FROM + " TABLE_NAME");

		// update
		REGEX_MAP_UPDATE.put(GROUP_UPDATE, "UPDATE TABLE_NAME SET " + COLUMN + " " + WHERE + " id = ?");

		// findAll
		REGEX_MAP_FINDALL.put(findAll, SELECT + " * " + FROM + " TABLE_NAME" + " " + WHERE  + ZRWrapper.ALWAYS_TRUE);

		// findByXXIsNullAndXXIsNullAndXXAndXX
		REGEX_MAP_findByXXIsNullAndXXIsNullAndXXAndXX.put(GROUP_findByXXIsNullAndXXIsNullAndXXAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IS NULL AND @ IS NULL AND @ = ? AND @ = ?)");
		// findByXXIsNullAndXXIsNullAndXX
		REGEX_MAP_findByXXIsNullAndXXIsNullAndXX.put(GROUP_findByXXIsNullAndXXIsNullAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IS NULL AND @ IS NULL AND @ = ?)");
		// findByXXIsNullAndXXAndXXAndXX
		REGEX_MAP_findByXXXIsNullAndXXAndXXAndXX.put(GROUP_findByXXIsNullAndXXAndXXAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IS NULL AND @ = ? AND @ = ? AND @ = ?)");
		// findByXXIsNullAndXXAndXX
		REGEX_MAP_findByXXXIsNullAndXXAndXX.put(GROUP_findByXXIsNullAndXXAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IS NULL AND @ = ? AND @ = ?)");
		// findByXXIsNullAndXX
		REGEX_MAP_findByXXXIsNullAndXX.put(GROUP_findByXXIsNullAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IS NULL AND @ = ?)");
		// findByXXIsEmpty
		REGEX_MAP_findByXXXIsEmpty.put(GROUP_findByXXIsEmptyAndXXAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = '' AND @ = ? AND @ = ?)");
		REGEX_MAP_findByXXXIsEmpty.put(GROUP_findByXXIsEmptyAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = '' AND @ = ?)");
		REGEX_MAP_findByXXXIsEmpty.put(GROUP_findByXXIsEmpty, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ = ''");
		// findByXXIsNull
		REGEX_MAP_findByXXXIsNull.put(findByXXIsNull, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ IS NULL");

		// findByXXXNotLike
		REGEX_MAP_findByXXXNotLike.put(GROUP_findByXXNotLike, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ NOT LIKE ?)");
		// findByXXXLikeAndXX
		REGEX_MAP_findByXXXLikeAndXX.put(GROUP_findByXXLikeAndXXAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ LIKE ? AND @ = ? AND @ = ?)");
		REGEX_MAP_findByXXXLikeAndXX.put(GROUP_findByXXLikeAndXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ LIKE ? AND @ = ?)");

		// findByXXXLike
		REGEX_MAP_findByXXXLike.put(findByXXLike, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ LIKE ?");

		// finByXX
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYYAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXXAndYY, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ = ? AND @ = ?)");
		REGEX_MAP_FINDBYXX.put(findByXX, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ = ?");
		// findByXXXEndingWith
		REGEX_MAP_findByXXXEndingWith.put(findByXXXEndingWith, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ LIKE ?");
		// findByXXXStartingWith
		REGEX_MAP_StartingWith.put(findByXXXStartingWith, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ LIKE ?");


		// findByXXNot
		REGEX_MAP_findByXXNot.put(findByXXNot, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ <> ?");

		// findByXXNotNull
		REGEX_MAP_findByXXNotNull.put(GROUP_findByxxNotNull, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ IS NOT NULL");

		// findByXXIn
		REGEX_MAP_FINDBYXXIN.put(findByXXInAndYYInAndYYInAndYYIn, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IN (?) AND @ IN (?) AND @ IN (?) AND @ IN (?))");
		REGEX_MAP_FINDBYXXIN.put(findByXXInAndYYInAndYYIn, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IN (?) AND @ IN (?) AND @ IN (?))");
		REGEX_MAP_FINDBYXXIN.put(findByXXInAndYYIn, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ IN (?) AND @ IN (?))");
		REGEX_MAP_FINDBYXXIN.put(findByXXIn, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " @ IN (?)");

		//  findByXXGreaterThanEquals
		REGEX_MAP_GreaterThanEquals.put(findByXXGreaterThanEquals, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ >= ?)");
		//  findByXXGreaterThan
		REGEX_MAP_GreaterThan.put(findByXXGreaterThan, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ > ?)");
		//  findByXXLessThan
		REGEX_MAP_LessThanEquals.put(findByXXLessThanEquals, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ <= ?)");
		// lessThan
		REGEX_MAP_LessThan.put(findByXXLessThan, SELECT + " * " + FROM + " TABLE_NAME " + WHERE + " (@ < ?)");

		// Between
		// FIXME 2023年6月16日 下午5:37:32 zhanghen: between先不做
		//		REGEX_MAP_BETWEEN.put(findByXXBetween, SELECT + " * from TABLE_NAME where @ in (?)");

		R_M.put(GROUP_findByXXGreaterThanEquals, REGEX_MAP_GreaterThanEquals);
		R_M.put(GROUP_findByXXGreaterThan, REGEX_MAP_GreaterThan);
		R_M.put(GROUP_findByXXLessThanEquals, REGEX_MAP_LessThanEquals);
		R_M.put(GROUP_findByXXOrderByXXDescLimit, REGEX_MAP_findByXXOrderByXXDescLimit);
		R_M.put(findByXXAndXXOrderByXXLimit, REGEX_MAP_findByXXOrderByXXLimit);
		R_M.put(GROUP_findByXXOrderByXXLimit, REGEX_MAP_findByXXOrderByXXLimit);
		R_M.put(GROUP_findByXXXEndingWith, REGEX_MAP_findByXXXEndingWith);
		R_M.put(GROUP_findByXXXStartingWith, REGEX_MAP_StartingWith);
		R_M.put(GROUP_findByXXIsNullAndXXIsNullAndXXAndXX, REGEX_MAP_findByXXIsNullAndXXIsNullAndXXAndXX);
		R_M.put(GROUP_findByXXIsNullAndXXIsNullAndXX, REGEX_MAP_findByXXIsNullAndXXIsNullAndXX);
		R_M.put(GROUP_findByXXIsNullAndXXAndXXAndXX, REGEX_MAP_findByXXXIsNullAndXXAndXXAndXX);
		R_M.put(GROUP_findByXXIsNullAndXXAndXX, REGEX_MAP_findByXXXIsNullAndXXAndXX);
		R_M.put(GROUP_findByXXIsNullAndXX, REGEX_MAP_findByXXXIsNullAndXX);
		R_M.put(GROUP_findByXXIsEmpty, REGEX_MAP_findByXXXIsEmpty);
		R_M.put(GROUP_findByXXIsNullAndXX, REGEX_MAP_findByXXXIsNullAndXX);
		R_M.put(GROUP_findByXXIsNull, REGEX_MAP_findByXXXIsNull);
		R_M.put(GROUP_findByXXNotLike, REGEX_MAP_findByXXXNotLike);
		R_M.put(GROUP_findByXXLikeAndXX, REGEX_MAP_findByXXXLikeAndXX);
		R_M.put(GROUP_findByXXLike, REGEX_MAP_findByXXXLike);
		R_M.put(GROUP_findByXXLessThan, REGEX_MAP_LessThan);
		R_M.put(GROUP_findByxx_in, REGEX_MAP_FINDBYXXIN);
		R_M.put(findByXXNotBetween, REGEX_MAP_findByXXNotBetween);
		R_M.put(findByXXBetween, REGEX_MAP_findByXXBetween);
		R_M.put(GROUP_findByxxNotNull, REGEX_MAP_findByXXNotNull);
		R_M.put(GROUP_findByxxNot, REGEX_MAP_findByXXNot);
		R_M.put(findByXXOrYY, REGEX_MAP_findByXXOrYY);
		R_M.put(GROUP_findByXXAndXX, REGEX_MAP_FINDBYXX);
		R_M.put(GROUP_findByXX, REGEX_MAP_FINDBYXX);
		R_M.put(GROUP_FINDALL, REGEX_MAP_FINDALL);
		R_M.put(GROUP_SAVEALL, REGEX_MAP_SAVEALL);
		R_M.put(GROUP_QUERY, REGEX_MAP_QUERY);
		R_M.put(GROUP_SAVE, REGEX_MAP_SAVE);
		R_M.put(GROUP_FIND, REGEX_MAP_FIND);
		R_M.put(GROUP_UPDATE, REGEX_MAP_UPDATE);
		R_M.put(GROUP_DeleteById, REGEX_MAP_DELETEBYID);
		R_M.put(existByIdIn, REGEX_MAP_EXISTBYIDIN);
		R_M.put(GROUP_EXISTBYId, REGEX_MAP_EXISTBYID);
		R_M.put(GROUP_CountingByXXXNot, REGEX_MAP_CountingByXXXNot);
		R_M.put(GROUP_CountingByXXX, REGEX_MAP_CountingByXXX);
		R_M.put(GROUP_count, REGEX_MAP_Count);

		R_M.put(GROUP_page, REGEX_MAP_PAGE);
	}


	public static MethodSQL check(final String methodName, final Method method) {

		final ZQuery zQuery = method.getAnnotation(ZQuery.class);
		if (zQuery != null) {
			final String sql = zQuery.sql();
			if (StrUtil.isBlank(sql)) {
				final String m = "@" + ZQuery.class.getSimpleName() + "方法 [" + methodName
						+ "] "
						+ "sql属性必须设置，当前未设置，当前值为 ["
						+ sql + "]"
						;
				throw new IllegalArgumentException(m);
			}

			return new MethodSQL(true,methodName, zQuery.sql());
		}

		final Collection<HashMap<String, String>> values = R_M.values();



		for (final HashMap<String, String> hashMap : values) {

			final Set<Entry<String, String>> es = hashMap.entrySet();
			for (final Entry<String, String> entry : es) {
				if (methodName.matches(entry.getKey())) {
					return new MethodSQL(false,entry.getKey(), entry.getValue());
				}
			}
		}

		// FIXME 2024年5月5日 下午10:14:22 zhangzhen: 抛异常，提示详细一点，先随手写一下
		throw new IllegalArgumentException("方式声明不支持 : " + methodName);
	}

	public static ArrayList<String> getFieldFromMethodname(final String methdoName) {
		return SqlPattern.sp(methdoName);

	}

	public static boolean isMethod_ANALYSIS_BY_METHOD_PARAMETERS(final Method method) {
		for (final HashMap<String, String> hashMap : R_M.values()) {
			final Set<Entry<String, String>> es = hashMap.entrySet();
			for (final Entry<String, String> entry : es) {
				if (method.getName().matches(entry.getKey())) {
					return ANALYSIS_BY_METHOD_PARAMETERS.contains(entry.getKey());
				}
			}
		}

		return false;
	}

	public static boolean isMethod_ANALYSIS_BY_ZENTITY_FIELD(final Method method) {
		for (final HashMap<String, String> hashMap : R_M.values()) {
			final Set<Entry<String, String>> es = hashMap.entrySet();
			for (final Entry<String, String> entry : es) {
				if (method.getName().matches(entry.getKey())) {
					return ANALYSIS_BY_ZENTITY_FIELD.contains(entry.getKey());
				}
			}
		}

		return false;
	}

	private final static Set<String> ANALYSIS_BY_METHOD_PARAMETERS = Sets.newConcurrentHashSet();
	private final static Set<String> ANALYSIS_BY_ZENTITY_FIELD =  Sets.newConcurrentHashSet();
	static {
		// FIXME 2024年5月14日 下午9:04:36 zhangzhen: 规则分两个组；一个是根据method.getps 的个数、类型、名称来解析的比如简单的findByXX
		// 一个是 根据 ZEntity.field来解析的比如findByXXOrderByXXLimit

		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYYOrYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXOrYY);

		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYYAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXAndYY);
		ANALYSIS_BY_METHOD_PARAMETERS.add(deleteById);
		ANALYSIS_BY_METHOD_PARAMETERS.add(existById);

		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByXXGreaterThan);
		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByXXAndXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByxx_in);
		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByXX);

		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXNotAndXXXNotAndXXXNotAndXXXNot);
		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXNotAndXXXNotAndXXXNot);
		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXNotAndXXXNot);
		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXNot);

		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXAndXXAndXXAndXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXAndXXAndXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXXAndXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(countingByXXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByXXNotLike);
		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByXXLikeAndXXAndXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(GROUP_findByXXLikeAndXX);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXLike);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXLessThanEquals);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXXStartingWith);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXNot);
		ANALYSIS_BY_METHOD_PARAMETERS.add(findByXXXEndingWith);


		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXLessThan);

		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsNullAndXXIsNullAndXXAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsNullAndXXIsNullAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsNullAndXXAndXXAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsNullAndXXAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsNullAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXNotBetween);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXBetween);

		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXDescLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXAndXXAndXXOrderByXXDescLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXAndXXOrderByXXDescLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXOrderByXXDescLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXOrderByXXDescLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXOrderByXXDescLimit);

		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByxxNotNull);

		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXLessThanEquals);

		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXGreaterThanEquals);

		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXAndXXAndXXAndXXOrderByXXLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXAndXXAndXXOrderByXXLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXAndXXOrderByXXLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXAndXXOrderByXXLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXAndXXOrderByXXLimit);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXOrderByXXLimit);

		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByxx_in);

		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsEmptyAndXXAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsEmptyAndXX);
		ANALYSIS_BY_ZENTITY_FIELD.add(GROUP_findByXXIsEmpty);

		ANALYSIS_BY_ZENTITY_FIELD.add(findByXXIsNull);

	}

}
