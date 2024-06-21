package com.vo;

/**
 * ZRepository.deleteById 执行的用到的处理器，如需在 ZRepository.deleteById 执行流程中自定义处理某些字段，扩展本类即可
 *
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:49:42
 *
 */
public abstract class ZDeleteByIdHandler implements ZEntityHandler {

	@Override
	public SUA handle(final SUA sua) {
		return sua;
	}

	@Override
	public boolean condition(final Class<?> entityClass) {
		return false;
	}

}
