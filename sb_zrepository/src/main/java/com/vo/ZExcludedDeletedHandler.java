package com.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import com.vo.core.Sort;

/**
 * 如果有 @ZLogicalDelete 字段，则[select]操作加入where条件：字段 = 未删除值
 *
 * @author zhangzhen
 * @date 2024年6月19日 下午5:03:25
 *
 */
@ZOrder
public class ZExcludedDeletedHandler extends ZAllHandler {

	private static final String LIMIT = MethodRegex.LIMIT;

	private static final String ORDER_BY = Sort.ORDER_BY;

	@Override
	public SUA handle(final SUA sua) {

		final Optional<Field> zld = Arrays.stream(sua.getEntityClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(ZLogicalDelete.class)).findAny();
		if(!zld.isPresent()) {
			return sua;
		}

		final Field zldf = zld.get();

		final String v =
				Sort.SPACE + MethodRegex.AND + Sort.SPACE
				+ ZFieldConverter.toDbField(zldf.getName())
				+ " = "
				+ zldf.getAnnotation(ZLogicalDelete.class).undeleted()
				;
		sua.setWhere(v);

		if (sua.getSql().toUpperCase().contains(ORDER_BY)) {

			final String sqlZED = sua.getSql().replace(ORDER_BY, v + Sort.SPACE + ORDER_BY);
			sua.setSql(sqlZED);
			return sua;
		}

		if (sua.getSql().toUpperCase().contains(LIMIT)) {

			final String sqlZED = sua.getSql().replace(LIMIT, v + Sort.SPACE + LIMIT);

			sua.setSql(sqlZED);
			return sua;
		}

		if (sua.getSql().toUpperCase().contains(ZRWrapper.ALWAYS_TRUE)) {

			final String sqlZED =
					sua.getSql().replace(ZRWrapper.ALWAYS_TRUE, ZRWrapper.ALWAYS_TRUE + v);

			sua.setSql(sqlZED);
			return sua;
		}


		final String sqlZED = sua.getSql() + v;
		sua.setSql(sqlZED);
		return sua;
	}

}
