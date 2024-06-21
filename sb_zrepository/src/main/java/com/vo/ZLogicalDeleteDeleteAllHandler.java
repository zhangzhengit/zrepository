package com.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import com.vo.anno.ZEntity;

/**
 * 在执行[ZRepository.deleteAll]时，判断 @ZEntity 对象如果有 @ZLogicalDelete 字段，
 * 则改用逻辑删除，否则物理删除
 *
 * @author zhangzhen
 * @date 2024年6月21日 下午12:43:36
 *
 */
@ZOrder
public class ZLogicalDeleteDeleteAllHandler extends ZDeleteAllHandler {

	@Override
	public SUA handle(final SUA sua) {
		final Field[] fs = sua.getEntityClass().getDeclaredFields();
		final Optional<Field> zldo = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZLogicalDelete.class))
				.findFirst();
		if (!zldo.isPresent()) {
			return sua;
		}

		final Optional<Field> id = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZID.class)).findFirst();
		final Field idf = id.get();

		final Class<?> cls = sua.getEntityClass();
		final String tableName = cls.getAnnotation(ZEntity.class).tableName();

		final String update =
				"UPDATE " + tableName + " SET "
						+ ZFieldConverter.toDbField(zldo.get().getName())
						+ " = "
						+ zldo.get().getAnnotation(ZLogicalDelete.class).deleted()
						;

		sua.setSql(update);
		return sua;
	}

}
