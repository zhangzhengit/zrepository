package com.vo;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月21日 下午12:39:02
 *
 */
public abstract class ZDeleteAllHandler implements ZEntityHandler {

	@Override
	public SUA handle(final SUA sua) {
		return sua;
	}

	@Override
	public boolean condition(final Class<?> entityClass) {
		return false;
	}

}
