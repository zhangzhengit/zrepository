package vo.zrepository.core;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

/**
 *	存放生成的ZRepository的子类的包装类的对象
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
public class ZRWMap {

	static ConcurrentMap<String, Object> map = Maps.newConcurrentMap();

	public static void put(final String name, final Object zRepository) {
		map.put(name, zRepository);

	}

	public static Object get(final String name) {
		return map.get(name);
	}

	public static Set<Entry<String, Object>> entrySet() {
		final Set<Entry<String, Object>> entrySet = map.entrySet();
		return entrySet;

	}
}
