package com.vo;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 *	Function相关
 *
 * @author zhangzhen
 * @date 2024年6月11日 下午10:23:13
 *
 */
public class ReflectionUtil {

	private static Map<Object, Field> cache = new WeakHashMap<>();

	public static <T, R> String getFieldName(final SerializableFunction<T, R> function) {
		final Field field = ReflectionUtil.getField(function);
		return field.getName();
	}

	public static <T, R> Field getField(final SerializableFunction<T, R> function) {
		final Field v = cache.get(function);
		if (v != null) {
			return v;
		}
		final Field v2 = findField(function);
		cache.put(function, v2);
		return v2;
	}

	public static <T, R> Field findField(final SerializableFunction<T, R> function) {
		Field field = null;
		String fieldName = null;
		try {
			final Method method = function.getClass().getDeclaredMethod("writeReplace");
			method.setAccessible(true);
			final SerializedLambda serializedLambda = (SerializedLambda) method.invoke(function);
			final String implMethodName = serializedLambda.getImplMethodName();
			if (implMethodName.startsWith("get") && (implMethodName.length() > 3)) {
				fieldName = Introspector.decapitalize(implMethodName.substring(3));

			} else if (implMethodName.startsWith("is") && (implMethodName.length() > 2)) {
				fieldName = Introspector.decapitalize(implMethodName.substring(2));
			}
			final String declaredClass = serializedLambda.getImplClass().replace("/", ".");
			final Class<?> aClass = Class.forName(declaredClass, false, ClassUtils.getDefaultClassLoader());

			field = ReflectionUtils.findField(aClass, fieldName);

		} catch (final Exception e) {
			e.printStackTrace();
		}
		if (field != null) {
			return field;
		}
		throw new NoSuchFieldError(fieldName);
	}
}
