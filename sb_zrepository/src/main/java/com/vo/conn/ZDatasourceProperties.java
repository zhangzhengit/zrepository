package com.vo.conn;

import java.util.List;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年6月17日
 *
 */
public class ZDatasourceProperties {

	private Boolean showSql;

	private P write;

	private Integer datasourceReadUrlCount;

	private List<P> readList;

	public static class P {
		private String datasourceUrl;
		private String datasourceUsername;
		private String datasourcePassword;
		private String datasourceDriverClass;
		private Integer datasourceMinConnection;
		private Integer datasourceMaxConnection;
		public String getDatasourceUrl() {
			return datasourceUrl;
		}
		public void setDatasourceUrl(final String datasourceUrl) {
			this.datasourceUrl = datasourceUrl;
		}
		public String getDatasourceUsername() {
			return datasourceUsername;
		}
		public void setDatasourceUsername(final String datasourceUsername) {
			this.datasourceUsername = datasourceUsername;
		}
		public String getDatasourcePassword() {
			return datasourcePassword;
		}
		public void setDatasourcePassword(final String datasourcePassword) {
			this.datasourcePassword = datasourcePassword;
		}
		public String getDatasourceDriverClass() {
			return datasourceDriverClass;
		}
		public void setDatasourceDriverClass(final String datasourceDriverClass) {
			this.datasourceDriverClass = datasourceDriverClass;
		}
		public Integer getDatasourceMinConnection() {
			return datasourceMinConnection;
		}
		public void setDatasourceMinConnection(final Integer datasourceMinConnection) {
			this.datasourceMinConnection = datasourceMinConnection;
		}
		public Integer getDatasourceMaxConnection() {
			return datasourceMaxConnection;
		}
		public void setDatasourceMaxConnection(final Integer datasourceMaxConnection) {
			this.datasourceMaxConnection = datasourceMaxConnection;
		}
		public P(final String datasourceUrl, final String datasourceUsername, final String datasourcePassword,
				final String datasourceDriverClass, final Integer datasourceMinConnection, final Integer datasourceMaxConnection) {
			super();
			this.datasourceUrl = datasourceUrl;
			this.datasourceUsername = datasourceUsername;
			this.datasourcePassword = datasourcePassword;
			this.datasourceDriverClass = datasourceDriverClass;
			this.datasourceMinConnection = datasourceMinConnection;
			this.datasourceMaxConnection = datasourceMaxConnection;
		}
		public P() {
			super();
		}
		
	}

	public Boolean getShowSql() {
		return showSql;
	}

	public void setShowSql(final Boolean showSql) {
		this.showSql = showSql;
	}

	public P getWrite() {
		return write;
	}

	public void setWrite(final P write) {
		this.write = write;
	}

	public Integer getDatasourceReadUrlCount() {
		return datasourceReadUrlCount;
	}

	public void setDatasourceReadUrlCount(final Integer datasourceReadUrlCount) {
		this.datasourceReadUrlCount = datasourceReadUrlCount;
	}

	public List<P> getReadList() {
		return readList;
	}

	public void setReadList(final List<P> readList) {
		this.readList = readList;
	}

	public ZDatasourceProperties(final Boolean showSql, final P write, final Integer datasourceReadUrlCount, final List<P> readList) {
		super();
		this.showSql = showSql;
		this.write = write;
		this.datasourceReadUrlCount = datasourceReadUrlCount;
		this.readList = readList;
	}

	public ZDatasourceProperties() {
		super();
	}
	
}
