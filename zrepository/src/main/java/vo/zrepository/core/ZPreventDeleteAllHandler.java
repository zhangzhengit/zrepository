package vo.zrepository.core;

import java.lang.reflect.Field;
import java.util.Arrays;

import vo.zrepository.anno.ZEntity;
import vo.zrepository.anno.ZLogicalDelete;
import vo.zrepository.anno.ZOrder;
import vo.zrepository.anno.ZPreventDeleteAll;
import vo.zrepository.exception.DeleteWithoutWhereException;
import vo.zrepository.exception.UpdateWithoutWhereException;

/**
 * @ZPreventDelete 功能实现,阻止不带条件的[ZR.deleteAll]操作，包括物物理删除和逻辑删除
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午11:27:34
 *
 */
@ZOrder(value = 1)
public class ZPreventDeleteAllHandler extends ZDeleteAllHandler {

	@Override
	public SUA handle(final SUA sua) {

		if (!sua.getEntityClass().isAnnotationPresent(ZPreventDeleteAll.class)) {
			return sua;
		}

		final Field[] fs = sua.getEntityClass().getDeclaredFields();

		final String sql = sua.getSql();

		final String preventDeleteAllMessage = sql
				+ "\r\n\t"
				+ "[" + sua.getZrSubClassName() + "." + sua.getCallerMethodName() + "]"
				+ "\r\n\t"
				+ "本功能由 @" + ZPreventDeleteAll.class.getName()  + " 提供"
				+ "\r\n\t"
				+ "如果就是要删除 ["
				+ sua.getEntityClass().getAnnotation(ZEntity.class).tableName()
				+ "] 表中的全部数据, 请删除 @" + ZEntity.class.getSimpleName() +" 对象 ["
				+ sua.getEntityClass().getSimpleName() + "] 上的 [@" + ZPreventDeleteAll.class.getName() + "] 注解 "
				+ "\r\n\t";

		if (Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZLogicalDelete.class)).findAny().isPresent()) {
			// 逻辑删除 UPDATE @ZLogicalDelete = 删除值 这个sql没定义模板，就直接判断后面带没带WHERE 吧
			final String sqlUpper = sql.trim().toUpperCase();

			if (!sqlUpper.contains(MethodRegex.WHERE.toUpperCase()) || sqlUpper.replace(Sort.SPACE, "")
					.endsWith((MethodRegex.WHERE + ZRWrapper.ALWAYS_TRUE).replace(Sort.SPACE, ""))
					||

					sqlUpper.replace(Sort.SPACE, "")
					.endsWith((MethodRegex.WHERE + ZRWrapper.ALWAYS_TRUE).replace(Sort.SPACE, "") + ";")
					) {
				throw new UpdateWithoutWhereException(preventDeleteAllMessage);
			}

			return sua;
		}

		final String sqlUpper = sql.trim().toUpperCase();
		if (!sqlUpper.startsWith(MethodRegex.DELETE.toUpperCase())) {
			return sua;
		}


		if (!sqlUpper.contains(MethodRegex.WHERE)) {
			throw new DeleteWithoutWhereException(preventDeleteAllMessage);
		}

		final boolean endsWith = sqlUpper.replace(Sort.SPACE, "")
				.endsWith((MethodRegex.WHERE + ZRWrapper.ALWAYS_TRUE).replace(Sort.SPACE, ""));
		if (endsWith) {
			throw new DeleteWithoutWhereException(preventDeleteAllMessage
					);
		}

		return sua;
	}

}
