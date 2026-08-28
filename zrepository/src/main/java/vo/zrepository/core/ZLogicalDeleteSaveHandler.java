package vo.zrepository.core;

import java.lang.reflect.Field;
import java.util.List;

import vo.vortex.common.RU;
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
		final List<Field> zldf = sua.findAllFieldHasAnnotation(ZLogicalDelete.class);
		for (final Field field : zldf) {
			final int undeleted = field.getAnnotation(ZLogicalDelete.class).undeleted();
			RU.setFiledValue(field, sua.getEntityObject(), undeleted);
		}
		return sua;
	}

}
