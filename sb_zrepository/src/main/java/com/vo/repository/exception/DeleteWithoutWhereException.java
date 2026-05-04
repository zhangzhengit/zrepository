package com.vo.repository.exception;

import com.vo.repository.core.MethodRegex;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午11:32:41
 *
 */
public class DeleteWithoutWhereException extends ZRepositoryException {

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\t[" + MethodRegex.DELETE + "]操作缺失条件异常:\r\n\t";

	public DeleteWithoutWhereException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}
}
