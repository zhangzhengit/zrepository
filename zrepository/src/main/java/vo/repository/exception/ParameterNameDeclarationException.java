package vo.repository.exception;

/**
 * 声明式方法-参数名称声明异常
 *
 * @author zhangzhen
 * @date 2024年6月9日 下午11:08:21
 *
 */
public class ParameterNameDeclarationException extends ZRepositoryException {

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\t参数名称声明异常:\r\n\t";

	public ParameterNameDeclarationException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}

}
