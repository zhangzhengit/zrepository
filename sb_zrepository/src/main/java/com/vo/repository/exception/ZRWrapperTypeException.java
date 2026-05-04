package com.vo.repository.exception;

import com.vo.repository.core.ZRWrapper;

/**
 * @see ZRWrapper 参数类型声明异常
 *
 * @author zhangzhen
 * @date 2024年6月13日 下午8:40:00
 *
 */
public class ZRWrapperTypeException extends ZRepositoryException {

	private static final long serialVersionUID = 1L;

	private final static String PREFIX = "\r\n\t" + ZRWrapper.class.getSimpleName() + "参数类型声明异常:\r\n\t";

	public ZRWrapperTypeException(final String repositoryMessage) {
		super(PREFIX + repositoryMessage);
	}

}
