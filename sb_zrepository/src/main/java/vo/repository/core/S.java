package vo.repository.core;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 *
 * 字符串相关
 *
 * @author zhangzhen
 * @date 2024年6月10日 下午9:06:47
 *
 */
public class S {

	/**
	 * 就是两个字符串的相似度
	 *
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static int apply(final String s1, final String s2) {
		final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
		final int distance = levenshteinDistance.apply(s1, s2);
		return distance;
	}

}
