package vo.zrepository.enums;

/**
 *
 * @ZID 的值的生成模式
 *
 * @author zhangzhen
 * @data 2024年5月17日 上午8:37:51
 *
 */
public enum ZGenerationType {

	/**
	 * 手动设置，使用此项，需要手动给主键字段设值
	 */
	MANUAL,

	/**
	 * DB自增，使用此项，无需给主键字段设值
	 */
	IDENTITY

}
