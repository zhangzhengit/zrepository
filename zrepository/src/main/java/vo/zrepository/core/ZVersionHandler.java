package vo.zrepository.core;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import vo.vortex.common.RU;
import vo.zrepository.anno.ZOrder;
import vo.zrepository.anno.ZVersion;

/**
 * @ZVersion save 时的动作：如果带 @ZVersion 的字段值为null，则自动给一个初始值
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:48:06
 *
 */
@ZOrder(value = 2)
public class ZVersionHandler extends ZSaveHandler {

	public static final ImmutableSet<Class<?>> SUPPORTED_CLASS_SET = ImmutableSet.copyOf(Sets.newHashSet(Long.class));

	public static final long ZVERSION_INITIAL_VALUE = 0L;

	@Override
	public SUA handle(final SUA sua) {

		final List<Field> zvfL = sua.findAllFieldHasAnnotation(ZVersion.class);
		for (final Field field : zvfL) {
			RU.setFiledValue(field, sua.getEntityObject(), ZVERSION_INITIAL_VALUE);
		}

		return sua;
	}

	@Override
	public boolean condition(final Class<?> entityClass) {
		return false;
	}

}
