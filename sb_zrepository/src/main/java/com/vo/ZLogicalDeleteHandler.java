package com.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import com.vo.anno.ZEntity;

/**
 * 在执行[ZRepository.deleteById]时，判断 @ZEntity 对象如果有 @ZLogicalDelete 字段，
 * 则改用逻辑删除，否则物理删除
 *
 * @author zhangzhen
 * @date 2024年6月16日 下午5:07:30
 *
 */
public class ZLogicalDeleteHandler extends ZDeleteHandler {

	@Override
	public SUA handle(final SUA sua) {
		final Field[] fs = sua.getEntityClass().getDeclaredFields();
		final Optional<Field> zldo = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZLogicalDelete.class)).findFirst();
		if(!zldo.isPresent()) {
			return sua;
		}

		final Optional<Field> id = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZID.class)).findFirst();
		final Field idf = id.get();
		final String idColumnName = ZFieldConverter.toDbField(idf.getName());

		final Class<?> cls = sua.getEntityClass();
		final String tableName = cls.getAnnotation(ZEntity.class).tableName();

		final String update =
				"UPDATE " + tableName + " SET "
						+ ZFieldConverter.toDbField(zldo.get().getName())
						+ " = "
						+ zldo.get().getAnnotation(ZLogicalDelete.class).deleted()
						+ " WHERE "
						+ idColumnName
						+ " in "
						+ "(?);"
						;

		sua.setSql(update);
		return sua;
	}

}
