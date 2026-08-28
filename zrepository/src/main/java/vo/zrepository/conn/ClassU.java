package vo.zrepository.conn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import vo.vortex.common.ZHashBasedTable;
import vo.zrepository.anno.ZID;

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
	private static final ConcurrentHashMap<String, Field> isAnyFieldHasAnnotation_C = new ConcurrentHashMap<>(16, 1F);

	/**
	 * 获取带有 @ZID 的 Field
	 *
	 * @param cls
	 * @return
	 */
	public static Field getZIDField(final Class<?> cls) {
		return isAnyFieldHasAnnotation(cls, ZID.class);
	}

	public static Field isAnyFieldHasAnnotation(final Class<?> cls, final Class<? extends Annotation> annoClass) {

		final Field f = isAnyFieldHasAnnotation_C
				.computeIfAbsent(cls.getName() + '-' + annoClass.getName(), x -> Arrays
				.stream(getDeclaredFields(cls)).filter(f1 -> f1.isAnnotationPresent(annoClass)).findAny().orElse(null));
		return f;
	}

	public static Field[] getDeclaredFields(final Class<?> cls) {
		return CLASS_DF_CACHE.computeIfAbsent(cls, Class::getDeclaredFields);
	}

	public static Field getDeclaredField(final Class<?> cls, final String fieldName) {
		final Field field = table.get(cls, fieldName);
		if (field != null) {
			return field;
		}

		synchronized (table) {
			final Field df = getDeclaredField(fieldName, cls);
			table.put(cls, fieldName, df);
			return df;
		}
	}

	private static Field getDeclaredField(final String fieldName, final Class<?> cls) {
		try {
			return cls.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

}
