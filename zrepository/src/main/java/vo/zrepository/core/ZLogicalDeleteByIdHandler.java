package vo.zrepository.core;

import java.lang.reflect.Field;

import vo.zrepository.anno.ZEntity;
import vo.zrepository.anno.ZID;
import vo.zrepository.anno.ZLogicalDelete;
import vo.zrepository.anno.ZOrder;

/**
 * 在执行[ZRepository.deleteById]时，判断 @ZEntity 对象如果有 @ZLogicalDelete 字段，
 * 则改用逻辑删除，否则物理删除
 *
 * @author zhangzhen
 * @date 2024年6月16日 下午5:07:30
 *
 */
@ZOrder
public class ZLogicalDeleteByIdHandler extends ZDeleteByIdHandler {

	@Override
	public SUA handle(final SUA sua) {

		final Field zldF = sua.findAnyFieldHasAnnotation(ZLogicalDelete.class);
		if (zldF == null) {
			return sua;
		}

		final Field idf = sua.findAnyFieldHasAnnotation(ZID.class);
		final String idColumnName = ZFieldConverter.toDbField(idf.getName());

		final Class<?> cls = sua.getEntityClass();
		final String tableName = cls.getAnnotation(ZEntity.class).tableName();

		final String update =
				"UPDATE " + tableName + " SET "
						+ ZFieldConverter.toDbField(zldF.getName())
						+ " = "
						+ zldF.getAnnotation(ZLogicalDelete.class).deleted()
						+ " WHERE "
						+ idColumnName
						+ " in "
						+ "(?);"
						;

		sua.setSql(update);
		return sua;
	}

}
