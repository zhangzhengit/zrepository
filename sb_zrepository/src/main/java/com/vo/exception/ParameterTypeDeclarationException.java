package com.vo.exception;

/**
 * 声明式方法-参数类型声明异常
 *
 * @author zhangzhen
 * @date 2024年6月9日 下午11:08:21
 *
 */
public class ParameterTypeDeclarationException extends ZRepositoryException {

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\t参数类型声明异常:\r\n\t";

	public ParameterTypeDeclarationException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}

}
