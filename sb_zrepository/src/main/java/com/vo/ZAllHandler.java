package com.vo;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月19日 下午5:15:15
 *
 */
public abstract class ZAllHandler implements ZEntityHandler {

	@Override
	public SUA handle(final SUA sua) {
		return sua;
	}

	@Override
	public boolean condition(final Class<?> entityClass) {
		// TODO Auto-generated method stub
		return false;
	}

}
