package vo.repository.core;

/**
 * ZRepository.update 执行的用到的处理器，如需在 ZRepository.update 执行流程中自定义处理某些字段，扩展本类即可
 *
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:49:42
 *
 */
public abstract class ZUpdateHandler implements ZEntityHandler {
	// FIXME 2024年6月16日 上午5:35:22 zhangzhen :继续提供扩展接口：在sql执行前的动作，
	//	如：1、给所有的select 操作都加入一个条件[where is_delete = 0]
	//		2、@ZVersion update时动态生成where条件
	// 		等等

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
