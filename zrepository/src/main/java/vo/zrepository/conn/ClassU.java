package vo.zrepository.conn;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import vo.vortex.common.ZHashBasedTable;

/**
 * 缓存Class的信息
 *
 * @author zhangzhen
 * @date 2026年8月22日 01:24:13
 */
public class ClassU {

	/**
	 * <Class,Field名,Field>
	 */
	private static final ZHashBasedTable<Class<?>, String, Field> table = new ZHashBasedTable<>();

	private static final ConcurrentHashMap<Class<?>, Field[]> CLASS_DF_CACHE = new ConcurrentHashMap<>(16, 1F);

	public static Field[] getDeclaredFields(final Class<?> cls) {
		return CLASS_DF_CACHE.computeIfAbsent(cls, Class::getDeclaredFields);
	}

	public static Field getField(final Class<?> cls, final String fieldName) {
		final Field field = table.get(cls, fieldName);
		if (field != null) {
			return field;
		}

		synchronized (table) {
			final Field df = getDF(fieldName, cls);
			table.put(cls, fieldName, df);
			return df;
		}
	}

	private static Field getDF(final String fieldName, final Class<?> cls) {
		try {
			return cls.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

}
