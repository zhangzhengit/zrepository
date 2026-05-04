package com.vo.repository.exception;

import com.vo.repository.core.MethodRegex;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午11:46:53
 *
 */
public class UpdateWithoutWhereException extends ZRepositoryException{

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\t[" + MethodRegex.UPDATE + "]操作缺失条件异常:\r\n\t";

	public UpdateWithoutWhereException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}
}
