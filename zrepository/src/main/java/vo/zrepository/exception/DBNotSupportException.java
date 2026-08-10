package vo.zrepository.exception;

/**
 * DB不支持异常
 *
 * @author zhangzhen
 * @date 2024年6月14日 下午9:39:08
 *
 */
public class DBNotSupportException extends ZRepositoryException {

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\tDB不支持异常:\r\n\t";

	public DBNotSupportException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}

}
