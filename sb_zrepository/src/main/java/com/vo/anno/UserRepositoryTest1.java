package com.vo.anno;

import java.lang.reflect.Type;
import java.util.Set;

import com.vo.ZRepositoryMain;
import com.vo.ZRepository;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年6月23日
 *
 */
public class UserRepositoryTest1 {

	public static String[] findZRSubclassFanxing(final Class<?> cls) {
		final Set<Class<?>> classSet = ZRepositoryMain.scanPackage_COM();
		for (final Class<?> class1 : classSet) {
			if (!cls.getCanonicalName().equals(class1.getCanonicalName())) {
				continue;
			}

			for (final Class<?> cls2 : class1.getInterfaces()) {
				if(cls2.getCanonicalName().equals(ZRepository.class.getCanonicalName())) {
					final Type[] genericInterfaces = class1.getGenericInterfaces();
					for (final Type type : genericInterfaces) {
						final String typeName = type.getTypeName();
						final int i = typeName.indexOf("<");
						if (i > -1) {
							final int i2 = typeName.lastIndexOf(">");
							if (i2 > i) {
								final String t = typeName.substring(i + 1, i2);
								final String[] array = t.split(",");
								final String[] array2 = {array[0].trim(),array[1].trim()};
								return array2;
							}
						}
					}
				}
			}
		}

		return null;
	}
}
