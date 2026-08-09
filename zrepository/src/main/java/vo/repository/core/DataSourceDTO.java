package vo.repository.core;

import java.util.Objects;

import vo.repository.enums.DBEnum;

/**
 * 
 *
 * @author zhangzhen
 * @date 2024年6月1日 上午2:53:28
 * 
 */
public class DataSourceDTO {

	private String catalog;

	private DBEnum dbEnum;

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(final String catalog) {
		this.catalog = catalog;
	}

	public DBEnum getDbEnum() {
		return dbEnum;
	}

	public void setDbEnum(final DBEnum dbEnum) {
		this.dbEnum = dbEnum;
	}

	public DataSourceDTO(final String catalog, final DBEnum dbEnum) {
		super();
		this.catalog = catalog;
		this.dbEnum = dbEnum;
	}

	public DataSourceDTO() {
		super();
	}

	@Override
	public int hashCode() {
		return Objects.hash(catalog, dbEnum);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DataSourceDTO other = (DataSourceDTO) obj;
		return Objects.equals(catalog, other.catalog) && dbEnum == other.dbEnum;
	}
	
}
