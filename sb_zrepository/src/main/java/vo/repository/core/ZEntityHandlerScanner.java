package vo.repository.core;

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

import vo.repository.anno.ZEHEnum;
import vo.repository.anno.ZOrder;
import vo.repository.exception.ZRepositoryException;

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
		for (final String pn : pas) {
			for (final Class<?> cls : ClassMap.scanPackage(pn)) {
				final Class<?> ia = cls.getSuperclass();
				if (ia == null) {
					continue;
				}
				final boolean isZRSubclass = ia.equals(acls);
				if (isZRSubclass) {
					try {
						r.add((ZEntityHandler) cls.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return r;
	}

	private static final HashMap<ZEHEnum, Set<ZEntityHandler>> m = Maps.newHashMap();

	private static void set(final ZEHEnum zehEnum,final Set<ZEntityHandler> saveHS ) {

		if (saveHS.size() > 1) {
			final Optional<ZEntityHandler> noZORDER = saveHS.stream().filter(h -> !h.getClass().isAnnotationPresent(ZOrder.class))
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

		final Map<String, List<ZEntityHandler>> pMap = saveHS.stream()
				.collect(Collectors.groupingBy(h -> h.getClass().getSuperclass().getName()));

		final Set<Entry<String, List<ZEntityHandler>>> es = pMap.entrySet();
		for (final Entry<String, List<ZEntityHandler>> e : es) {

			final Set<Integer> os = Sets.newHashSet();
			for (final ZEntityHandler h : e.getValue()) {
				final ZOrder zo = h.getClass().getAnnotation(ZOrder.class);
				if (zo == null) {
					continue;
				}

				final int v = zo.value();
				final boolean add = os.add(h.getClass().getAnnotation(ZOrder.class).value());
				if (!add) {
					final String s = e.getKey() + " 子类"
							+ " ["
							+ h.getClass().getName()
							+ "] "
							+ "的 @" + ZOrder.class.getName()
							+ " 注解值 [" + v + "] 重复，请修改此值";
					throw new ZRepositoryException(s);
				}
			}
		}


		final List<ZEntityHandler> xl = Lists.newArrayList(saveHS);
		xl.sort(Comparator.comparing(h -> h.getClass().getAnnotation(ZOrder.class).value()));
		final LinkedHashSet<ZEntityHandler> vs = Sets.newLinkedHashSet(xl);

		m.put(zehEnum, vs);
	}

	public static Set<ZEntityHandler> get(final ZEHEnum zehEnum) {
		return m.get(zehEnum);
	}

}
