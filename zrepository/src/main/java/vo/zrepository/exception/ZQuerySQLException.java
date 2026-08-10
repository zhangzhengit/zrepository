package vo.zrepository.exception;

import java.sql.SQLException;


/**
 * @ZQuery 自定义SQL异常
 *
 * @author zhangzhen
 * @data 2024年5月10日 下午8:53:58
 *
 */
public class ZQuerySQLException extends SQLException {

	private static final long serialVersionUID = 1L;

	private String message;


	@Override
	public String getMessage() {
		return this.message;
	}

	public ZQuerySQLException(final String message) {
		this.message = message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

}
