package vo.zrepository.core;

import java.lang.reflect.Field;
import java.util.Date;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import vo.vortex.common.RU;
import vo.zrepository.anno.ZCreateTime;
import vo.zrepository.anno.ZOrder;

/**
 * @ZCreateTime 字段 ZRepository.save 时的动作：如果字段值为null，则自动给一个初始值[当前时间]
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午5:25:45
 *
 */
@ZOrder
public class ZCreateTimeHandler extends ZSaveHandler {

	public static final ImmutableSet<Class<?>> SUPPORTED_CLASS_SET = ImmutableSet.copyOf(Sets.newHashSet(java.util.Date.class));

	@Override
	public SUA handle(final SUA sua) {
		// XXX 2024年6月27日 下午9:46:08 zhangzhen : save 操作，就不取 @ZDateFormat 了，暂时还没发现有问题
		final java.util.Date now = new Date();

//		final Field[] fs = RU.getDeclaredFields(sua.getEntityClass());
		final Field[] fs = sua.getEntityClass().getDeclaredFields();

		for (final Field f : fs) {
			if (f.isAnnotationPresent(ZCreateTime.class)) {
				f.setAccessible(true);
				try {
					final Object zvv = f.get(sua.getEntityObject());
					if (zvv == null) {
						f.set(sua.getEntityObject(), now);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return sua;
	}
}
