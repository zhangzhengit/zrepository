package com.vo.exception;

import lombok.Getter;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月9日 下午11:08:11
 *
 */
public class ZRepositoryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String repositoryMessage;

	public ZRepositoryException(final String repositoryMessage) {
		super(repositoryMessage);
		this.repositoryMessage = repositoryMessage;
	}

}
