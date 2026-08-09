package vo.repository.core;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

/**
 *	存放生成的ZRepository的子类的对象
 *
 * @author zhangzhen	
 * @date 2023年6月15日
 *
 */
public class ZRMap {

	static ConcurrentMap<String, ZRepository> map = Maps.newConcurrentMap();

	public static void put(final String name, final ZRepository zRepository) {
		map.put(name, zRepository);

	}

	public static ZRepository get(final String name) {
		return map.get(name);
	}
}
