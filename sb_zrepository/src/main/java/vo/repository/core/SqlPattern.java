package vo.repository.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 根据方法名拼出sql
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
public class SqlPattern {

	public static ArrayList<String> sp(final String methodName) {
		final ArrayList<String> pl = Lists.newArrayList();
		if (!xiaoxie.contains(methodName.charAt(0))) {
			throw new IllegalArgumentException("方法名[" + methodName + "]必须以小写字母开始");
		}

		final char[] ca = methodName.toCharArray();

		for (int i = 0; i < ca.length;) {
			final char c = ca[i];
			P.add(c);

			if (daxie.contains(c)) {
				P.poll();
				final String string = P.get();
				pl.add(string);

				P.add(c);
			}
			if ((i + 1) >= ca.length) {
				final String s2 = P.get();
				pl.add(s2);
				//				System.out.println("一个关键词 = " + s2);
			}
			i++;

		}

		return pl;

	}

	/**
	 * ZRepository 声明式方法的关键字
	 */
	public static final HashSet<String> SQL_KEYWORD = Sets.newHashSet();

	public static HashSet<String> inOrLikeAnd = Sets.newHashSet("And", "In", "Or", "Like");
	public static HashSet<String> keyword = Sets.newHashSet("query", "find", "delete", "update", "save", "exist", "page", "By",
			"In", "Or", "Like");
	public static final HashSet<String> methodPrefix = Sets.newHashSet("find", "delete", "save", "exist", "page");

	public static final HashSet<Character> shuzi = Sets.newHashSet('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
	public static final  HashSet<Character> xiaoxie = Sets.newHashSet('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
			'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

	public static final HashSet<Character> daxie = Sets.newHashSet('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
			'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

	static {
		SQL_KEYWORD.addAll(inOrLikeAnd);
		SQL_KEYWORD.addAll(keyword);
		SQL_KEYWORD.addAll(methodPrefix);

		SQL_KEYWORD.add("Optional");

		SQL_KEYWORD.add("Between");
		SQL_KEYWORD.add("All");
		SQL_KEYWORD.add("Less");
		SQL_KEYWORD.add("Than");

		SQL_KEYWORD.add("is");
		SQL_KEYWORD.add("Is");
		SQL_KEYWORD.add("Empty");
		SQL_KEYWORD.add("Null");
		SQL_KEYWORD.add("counting");
		SQL_KEYWORD.add("count");

		SQL_KEYWORD.add("Limit");
		SQL_KEYWORD.add("Order");
		SQL_KEYWORD.add("Desc");
		SQL_KEYWORD.add("Asc");
		SQL_KEYWORD.add("Equals");
		SQL_KEYWORD.add("Equal");
		SQL_KEYWORD.add("Greater");

		SQL_KEYWORD.add("Starting");
		SQL_KEYWORD.add("Ending");
		SQL_KEYWORD.add("With");
		SQL_KEYWORD.add("Not");

	}

	public static class P {

		static ArrayList<Character> cl = Lists.newArrayList();

		public static void add(final char c) {
			cl.add(c);
		}

		public static void poll() {
			cl.remove(cl.size() - 1);

		}

		public static String get() {
			final char[] ca = new char[cl.size()];
			for (int i = 0; i < cl.size(); i++) {
				ca[i] = cl.get(i);
			}

			cl.clear();

			final String r = new String(ca);
			return r;
		}

	}

	public static boolean isSqlKeyword(final String name) {
		return SQL_KEYWORD.contains(name);
	}

}
