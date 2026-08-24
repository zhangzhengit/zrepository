package vo.zrepository.conn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
	private static final ConcurrentHashMap<Class<?>, Field> CLASS_ZID_FIELD_CACHE = new ConcurrentHashMap<>(16, 1F);

	/**
	 * 获取带有 @ZID 的 Field
	 *
	 * @param cls
	 * @return
	 */
	public static Field getZIDField(final Class<?> cls) {
		final Field zidF = CLASS_ZID_FIELD_CACHE.computeIfAbsent(cls, c -> {

			for (final Field field : getDeclaredFields(c)) {
				if (field.isAnnotationPresent(ZID.class)) {
					return field;
				}
			}
			return null;
		});

		return zidF;
	}


	public static Field isAnyFieldHasAnnotation(final Class<?> cls, final Class<? extends Annotation> annoClass) {
		final Field[] fs = getDeclaredFields(cls);
		for (final Field field : fs) {
			if (field.isAnnotationPresent(annoClass)) {
				return field;
			}
		}

		return null;
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
