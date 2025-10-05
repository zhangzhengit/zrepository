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

	private boolean showSql;

	private P write;

	private int datasourceReadUrlCount;

	private List<P> readList;

	public static class P {
		private String datasourceUrl;
		private String datasourceUsername;
		private String datasourcePassword;
		private String datasourceDriverClass;
		private int datasourceMinConnection;
		private int datasourceMaxConnection;
		public String getDatasourceUrl() {
			return this.datasourceUrl;
		} 
		public void setDatasourceUrl(final String datasourceUrl) {
			this.datasourceUrl = datasourceUrl;
		}
		public String getDatasourceUsername() {
			return this.datasourceUsername;
		}
		public void setDatasourceUsername(final String datasourceUsername) {
			this.datasourceUsername = datasourceUsername;
		}
		public String getDatasourcePassword() {
			return this.datasourcePassword;
		}
		public void setDatasourcePassword(final String datasourcePassword) {
			this.datasourcePassword = datasourcePassword;
		}
		public String getDatasourceDriverClass() {
			return this.datasourceDriverClass;
		}
		public void setDatasourceDriverClass(final String datasourceDriverClass) {
			this.datasourceDriverClass = datasourceDriverClass;
		}
		public int getDatasourceMinConnection() {
			return this.datasourceMinConnection;
		}
		public void setDatasourceMinConnection(final int datasourceMinConnection) {
			this.datasourceMinConnection = datasourceMinConnection;
		}
		public int getDatasourceMaxConnection() {
			return this.datasourceMaxConnection;
		}
		public void setDatasourceMaxConnection(final int datasourceMaxConnection) {
			this.datasourceMaxConnection = datasourceMaxConnection;
		}
		public P(final String datasourceUrl, final String datasourceUsername, final String datasourcePassword,
				final String datasourceDriverClass, final int datasourceMinConnection, final int datasourceMaxConnection) {
			this.datasourceUrl = datasourceUrl;
			this.datasourceUsername = datasourceUsername;
			this.datasourcePassword = datasourcePassword;
			this.datasourceDriverClass = datasourceDriverClass;
			this.datasourceMinConnection = datasourceMinConnection;
			this.datasourceMaxConnection = datasourceMaxConnection;
		}
		public P() {
		}
		
	}

	public boolean getShowSql() {
		return this.showSql;
	}

	public void setShowSql(final boolean showSql) {
		this.showSql = showSql;
	}

	public P getWrite() {
		return this.write;
	}

	public void setWrite(final P write) {
		this.write = write;
	}

	public int getDatasourceReadUrlCount() {
		return this.datasourceReadUrlCount;
	}

	public void setDatasourceReadUrlCount(final int datasourceReadUrlCount) {
		this.datasourceReadUrlCount = datasourceReadUrlCount;
	}

	public List<P> getReadList() {
		return this.readList;
	}

	public void setReadList(final List<P> readList) {
		this.readList = readList;
	}

	public ZDatasourceProperties(final boolean showSql, final P write, final int datasourceReadUrlCount, final List<P> readList) {
		this.showSql = showSql;
		this.write = write;
		this.datasourceReadUrlCount = datasourceReadUrlCount;
		this.readList = readList;
	}

	public ZDatasourceProperties() {
	}
	
}
