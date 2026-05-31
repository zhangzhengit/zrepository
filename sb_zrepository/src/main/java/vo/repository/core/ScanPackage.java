package vo.repository.core;

import java.util.Set;

/**
 * 存/取要扫描的包
 *
 * @author zhangzhen
 * @data 2024年5月4日 下午5:56:01
 *
 */
public class ScanPackage {
	private static final ThreadLocal<Set<String>> TL = new ThreadLocal<>();

	public static void set(final Set<String> scanPackageName) {
		TL.set(scanPackageName);
	}

	public static Set<String> get() {
		return TL.get();
	}
}
