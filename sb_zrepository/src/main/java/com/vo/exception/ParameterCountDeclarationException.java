package com.vo.exception;

/**
 * 声明式方法-参数个数声明异常
 *
 * @author zhangzhen
 * @date 2024年6月9日 下午11:08:21
 *
 */
public class ParameterCountDeclarationException extends ZRepositoryException {

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\t参数个数声明异常:\r\n\t";

	public ParameterCountDeclarationException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}

}
