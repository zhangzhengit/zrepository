package vo.repository.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * ZRepository.page 方法的排序条件对象
 *
 * @author zhangzhen
 * @data 2024年5月19日 下午4:40:49
 *
 */
public class Sort<T> {

	public static final String SPACE = " ";

	public static final String ASC = "ASC";

	public static final String DESC = "DESC";

	public static final String ORDER_BY = "ORDER BY";

	private final List<String> x = new ArrayList<>();

	public static <T> Sort<T> create(final Class<T> entityClass) {
		if (entityClass == null) {
			throw new NullPointerException("entityClass不能为空");
		}

		final Sort<T> sort = new Sort<>();
		return sort;
	}

	public Sort<T> ascendingBy(final SerializableFunction<T, Object> function) {
		final Field field = ReflectionUtil.getField(function);
		this.addOrderBy();
		final String name = field.getName();
		final String dbField = ZFieldConverter.toDbField(name);
		this.x.add(SPACE + dbField + SPACE + ASC);
		return this;
	}

	public Sort<T> descendingBy(final SerializableFunction<T, Object> function) {
		final Field field = ReflectionUtil.getField(function);
		this.addOrderBy();
		final String name = field.getName();
		final String dbField = ZFieldConverter.toDbField(name);
		this.x.add(SPACE + dbField + SPACE + DESC);
		return this;
	}

	public String done() {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < this.x.size(); i++) {
			builder.append(this.x.get(i));
			if ((i > 0) && (i < (this.x.size() - 1))) {
				builder.append(',');
			}
		}
		return builder.toString();
	}

	private void addOrderBy() {
		if (this.x.isEmpty()) {
			this.x.add(ORDER_BY);
		}
	}

}
