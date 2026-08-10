package vo.zrepository.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import vo.zrepository.anno.ZEHEnum;
import vo.zrepository.anno.ZOrder;
import vo.zrepository.exception.ZRepositoryException;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:56:47
 *
 */
public class ZEntityHandlerScanner {

	public static void scan(final String... pas) {
		final Set<ZEntityHandler> saveHS = getZEntityHandlerSubClass(ZSaveHandler.class, pas);
		set(ZEHEnum.SAVE, saveHS);

		final Set<ZEntityHandler> updateHS = getZEntityHandlerSubClass(ZUpdateHandler.class, pas);
		set(ZEHEnum.UPDATE, updateHS);


		final Set<ZEntityHandler> DELETEDHS = getZEntityHandlerSubClass(ZDeleteByIdHandler.class, pas);
		set(ZEHEnum.DELETE_Logical, DELETEDHS);

		final Set<ZEntityHandler> EXCLUDED_DELETEDHS = getZEntityHandlerSubClass(ZAllHandler.class, pas);
		set(ZEHEnum.SELECT_EXCLUDED_DELETED, EXCLUDED_DELETEDHS);
		// FIXME 2024年6月16日 上午5:07:55 zhangzhen : 继续写

		final Set<ZEntityHandler> deleteHS = getZEntityHandlerSubClass(ZDeleteAllHandler.class, pas);
		set(ZEHEnum.DELETE_ALL, deleteHS);

	}

	private static Set<ZEntityHandler> getZEntityHandlerSubClass(final Class<?> acls, final String... pas) {
		final Set<ZEntityHandler> r = Sets.newHashSet();
		final Set<Class<?>> cs = Sets.newHashSet();
		for (final String pn : pas) {
			for (final Class<?> cls : ClassMap.scanPackage(pn)) {
				final Class<?> ia = cls.getSuperclass();
				if (ia == null) {
					continue;
				}

				if (ia.equals(acls)) {
					try {
						if (cs.add(cls)) {
							r.add((ZEntityHandler) cls.getDeclaredConstructor().newInstance());
						}
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return r;
	}

	private static final HashMap<ZEHEnum, Set<ZEntityHandler>> m = Maps.newHashMap();

	private static void set(final ZEHEnum zehEnum,final Set<ZEntityHandler> ehs ) {

		if (ehs.size() > 1) {
			final Optional<ZEntityHandler> noZORDER = ehs.stream().filter(h -> !h.getClass().isAnnotationPresent(ZOrder.class))
					.findAny();

			if (noZORDER.isPresent()) {

				final String s = ZEntityHandler.class.getName() + " " + "对象"
						+ " ["
						+ noZORDER.get().getClass().getName()
						+ "] "

						+ "缺失 @" + ZOrder.class.getName()
						+ " 注解，请加入此注解";
				throw new ZRepositoryException(s);
			}
		}

		final Map<String, List<ZEntityHandler>> pMap = ehs.stream()
				.collect(Collectors.groupingBy(h -> h.getClass().getSuperclass().getName()));

		final Set<Integer> os = Sets.newHashSet();

		final Set<Entry<String, List<ZEntityHandler>>> es = pMap.entrySet();
		for (final Entry<String, List<ZEntityHandler>> e : es) {

			for (final ZEntityHandler h : e.getValue()) {
				final ZOrder zo = h.getClass().getAnnotation(ZOrder.class);
				if (zo == null) {
					continue;
				}

				final int zOrderValue = zo.value();
				if (!os.add(zOrderValue)) {
					final String s = e.getKey() + " 子类"
							+ " ["
							+ h.getClass().getName()
							+ "] "
							+ "的 @" + ZOrder.class.getName()
							+ " 注解值 [" + zOrderValue + "] 重复，请修改此值";
					throw new ZRepositoryException(s);
				}
			}
		}


		final List<ZEntityHandler> xl = Lists.newArrayList(ehs);
		xl.sort(Comparator.comparing(h -> h.getClass().getAnnotation(ZOrder.class).value()));
		final LinkedHashSet<ZEntityHandler> vs = Sets.newLinkedHashSet(xl);

		m.put(zehEnum, vs);
	}

	public static Set<ZEntityHandler> get(final ZEHEnum zehEnum) {
		return m.get(zehEnum);
	}

}
