package vo.zrepository.core;

import java.lang.reflect.Field;
import java.util.List;

import vo.vortex.common.RU;
import vo.zrepository.anno.ZOrder;
import vo.zrepository.anno.ZVersion;
import vo.zrepository.transaction.ZTransactionAOP;

/**
 * @ZVersion 字段 ZRepository.update 时的动作：实现乐观锁
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午10:40:26
 *
 */
@ZOrder(value = 1)
public class ZVersionOptimisticLockHandler extends ZUpdateHandler {

	@Override
	public SUA handle(final SUA sua) {

		final ZC2 zc = sua.getZc2();

		ZTransactionAOP.setZConnection(zc);

		final List<Field> zvfL = sua.findAllFieldHasAnnotation(ZVersion.class);

		for (final Field field : zvfL) {
			final Long oldVV = incrementZVersionValue(sua.getEntityObject(), field);
			final String versionColumnName = ZFieldConverter.toDbField(field.getName());
			final String version = versionColumnName + " = " + oldVV;
			final String replace = sua.getSql().replace(MethodRegex.WHERE,
					MethodRegex.WHERE + Sort.SPACE + version + Sort.SPACE + MethodRegex.AND);
			sua.setSql(replace);
		}

		return sua;
	}

	private static <T> Long incrementZVersionValue(final T t, final Field vf) {
		final Object versionValue = RU.getFiledValue(t, vf);
		final Long nVV = (Long) versionValue + 1L;
		RU.setFiledValue(vf, t, nVV);
		return (Long) versionValue;
	}

}
