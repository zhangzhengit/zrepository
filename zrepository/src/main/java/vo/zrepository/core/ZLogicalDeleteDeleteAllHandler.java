package vo.zrepository.core;

import java.lang.reflect.Field;
import java.util.List;

import vo.zrepository.anno.ZEntity;
import vo.zrepository.anno.ZLogicalDelete;
import vo.zrepository.anno.ZOrder;

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

		final Field zldF = sua.findAnyFieldHasAnnotation(ZLogicalDelete.class);
		if (zldF == null) {
			return sua;
		}

		final Class<?> cls = sua.getEntityClass();
		final String tableName = cls.getAnnotation(ZEntity.class).tableName();

		final String update =
						"UPDATE "
						+ tableName
						+ " SET "
						+ ZFieldConverter.toDbField(zldF.getName())
						+ " = "
						+ zldF.getAnnotation(ZLogicalDelete.class).deleted()
						;

		sua.setSql(update);
		return sua;
	}

}
