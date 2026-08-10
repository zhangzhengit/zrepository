package vo.zrepository.anno;


/**
 * 日期格式枚举
 *
 * @author zhangzhen
 * @date 2023年9月24日
 *
 */
public enum ZDateFormatEnum {

	// FIXME 2023年9月24日 下午3:41:59 zhanghen: TODO 添加各种格式
	YYYY_MM_DD_HH_MM_SS_SSSSSS("yyyy-MM-dd HH:mm:ss.SSSSSS"),

	YYYY_MM_DD_HH_MM_SS_SSSSS("yyyy-MM-dd HH:mm:ss.SSSSS"),

	YYYY_MM_DD_HH_MM_SS_SSSS("yyyy-MM-dd HH:mm:ss.SSSS"),

	YYYY_MM_DD_HH_MM_SS_SSS("yyyy-MM-dd HH:mm:ss.SSS"),

	YYYY_MM_DD_HH_MM_SS_SS("yyyy-MM-dd HH:mm:ss.SS"),

	YYYY_MM_DD_HH_MM_SS_S("yyyy-MM-dd HH:mm:ss.S"),
	YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),

	YYYY_MM_DD("yyyy-MM-dd"),

	HH_MM_SS("HH:mm:ss"),

	HH_MM_SS_S("HH:mm:ss.S"),

	HH_MM_SS_SS("HH:mm:ss.SS"),

	HH_MM_SS_SSS("HH:mm:ss.SSS"),

	HH_MM_SS_SSSS("HH:mm:ss.SSSS"),

	HH_MM_SS_SSSSS("HH:mm:ss.SSSSS"),

	HH_MM_SS_SSSSSS("HH:mm:ss.SSSSSS"),

	;

	private final String format;
	

	private ZDateFormatEnum(final String format) {
		this.format = format;
	}
	public String getFormat() {
		return format;
	}
	public static int getMysqlColumnSizeBYTIME(final ZDateFormatEnum dateFormatEnum) {

		if (HH_MM_SS == dateFormatEnum) {
			return 8;
		}
		if (HH_MM_SS_S == dateFormatEnum) {
			return 10;
		}
		if (HH_MM_SS_SS == dateFormatEnum) {
			return 11;
		}
		if (HH_MM_SS_SSS == dateFormatEnum) {
			return 12;
		}
		if (HH_MM_SS_SSSS == dateFormatEnum) {
			return 13;
		}
		if (HH_MM_SS_SSSSS == dateFormatEnum) {
			return 14;
		}
		if (HH_MM_SS_SSSSSS == dateFormatEnum) {
			return 15;
		}

		return 8;
	}
	public static ZDateFormatEnum getTIMEByMysqlColumnSize(final int columnSize) {
		if ((columnSize == 8)) {
			return HH_MM_SS;
		}
		if ((columnSize == 10)) {
			return HH_MM_SS_S;
		}
		if ((columnSize == 11)) {
			return HH_MM_SS_SS;
		}
		if ((columnSize == 12)) {
			return HH_MM_SS_SSS;
		}
		if ((columnSize == 13)) {
			return HH_MM_SS_SSSS;
		}
		if ((columnSize == 14)) {
			return HH_MM_SS_SSSSS;
		}
		if ((columnSize == 15)) {
			return HH_MM_SS_SSSSSS;
		}

		throw new IllegalArgumentException("mysql TIME columnSize 长度错误,columnSize = " + columnSize);
	}

	public static int getMysqlColumnSizeByLocalDateTimeFormat(final ZDateFormatEnum dateFormatEnum) {

		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS) {
			return 19;
		}
		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS_S) {
			return 21;
		}
		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS_SS) {
			return 22;
		}
		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS_SSS) {
			return 23;
		}
		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS_SSSS) {
			return 24;
		}
		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS_SSSSS) {
			return 25;
		}
		if (dateFormatEnum == YYYY_MM_DD_HH_MM_SS_SSSSSS) {
			return 26;
		}

		return 19;
	}

	public static ZDateFormatEnum getLocalDateTimeByMysqlColumnSize(final int columnSize) {
		if (columnSize == 19) {
			return YYYY_MM_DD_HH_MM_SS;
		}
		if (columnSize == 21) {
			return YYYY_MM_DD_HH_MM_SS_S;
		}
		if (columnSize == 22) {
			return YYYY_MM_DD_HH_MM_SS_SS;
		}
		if (columnSize == 23) {
			return YYYY_MM_DD_HH_MM_SS_SSS;
		}
		if (columnSize == 24) {
			return YYYY_MM_DD_HH_MM_SS_SSSS;
		}
		if ((columnSize == 25) || (columnSize == 26)) {
			return YYYY_MM_DD_HH_MM_SS_SSSSS;
		}

		throw new IllegalArgumentException("mysql DATETIME/TIMESTAMP columnSize 长度错误,columnSize = " + columnSize);
	}
}
