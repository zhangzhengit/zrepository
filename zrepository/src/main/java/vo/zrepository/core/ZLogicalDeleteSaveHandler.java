package vo.zrepository.core;

import java.lang.reflect.Field;

import vo.zrepository.anno.ZLogicalDelete;
import vo.zrepository.anno.ZOrder;

/**
 *
 * @ZLogicalDelete 字段 ZRepository.save 时的动作：如果字段值为null，则自动给一个初始值[ZLogicalDelete.undeleted()]
 *
 * @author zhangzhen
 * @date 2024年6月19日 下午5:44:32
 *
 */
@ZOrder(value = 1)
public class ZLogicalDeleteSaveHandler extends ZSaveHandler {

	@Override
	public SUA handle(final SUA sua) {

		final Field zldf = sua.isAnyFieldHasAnnotation(ZLogicalDelete.class);
		if (zldf == null) {
			return sua;
		}

		zldf.setAccessible(true);
		final int undeleted = zldf.getAnnotation(ZLogicalDelete.class).undeleted();
		try {
			zldf.set(sua.getEntityObject(), undeleted);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return sua;
	}

}
