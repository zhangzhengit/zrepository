package vo.zrepository.core;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import vo.vortex.cache.ZRC;

/**
 *
 * 暂存扫描的结果
 *
 * @author zhangzhen
 * @date 2023年9月5日
 *
 */
public class ClassMap {

	public synchronized static Set<Class<?>> scanPackage(final String packageName, final Class<?> cls) {
		final Set<Class<?>> cs = scanPackage(packageName);
		final Set<Class<?>> rs = cs.stream().filter(c -> c.equals(cls)).collect(Collectors.toSet());
		return rs;
	}

	public synchronized static Set<Class<?>> scanPackage(final String packageName) {
		final Supplier<Set<Class<?>>> supplier = () -> vo.vortex.scanner.ClassMap.scanPackage(packageName);
		return ZRC.singleton().computeIfAbsent(packageName, supplier);
	}
}

