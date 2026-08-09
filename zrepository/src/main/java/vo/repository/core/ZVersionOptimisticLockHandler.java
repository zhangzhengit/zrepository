package vo.repository.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import vo.repository.anno.ZCSourceEnum;
import vo.repository.anno.ZOrder;
import vo.repository.anno.ZVersion;

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

		if (zc.getSourceEnum() == ZCSourceEnum.ZTRANSACTION) {

			final Field[] fs = sua.getEntityClass().getDeclaredFields();
			final Optional<Field> zvf = Arrays.stream(fs).filter(f -> f.isAnnotationPresent(ZVersion.class)).findAny();
			if (zvf.isPresent()) {
				final Field vf = zvf.get();
				final Long oldVV = incrementZVersionValue(sua.getEntityObject(), vf);
				if (vf != null) {
					final String versionColumnName = ZFieldConverter.toDbField(vf.getName());
					final String version = versionColumnName + " = " + oldVV;
					final String replace = sua.getSql().replace(MethodRegex.WHERE,
							MethodRegex.WHERE + Sort.SPACE + version + Sort.SPACE + MethodRegex.AND);
					sua.setSql(replace);
				}
			}
		}

		return sua;
	}

	private static <T> void setZVersionValue(final T t, final Field vf, final Long versionValue) {
		vf.setAccessible(true);
		try {
			vf.set(t, versionValue);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static <T> Long incrementZVersionValue(final T t, final Field vf) {
		try {
			vf.setAccessible(true);
			final Object versionValue = vf.get(t);

			final Long nVV = (Long) versionValue + 1L;
			setZVersionValue(t, vf, nVV);
			return (Long) versionValue;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
