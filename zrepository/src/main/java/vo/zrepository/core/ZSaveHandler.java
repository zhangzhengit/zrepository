package vo.zrepository.core;

/**
 * ZRepository.save 执行的用到的处理器，如需在 ZRepository.save 执行流程中自定义处理某些字段，扩展本类即可
 *
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:49:42
 *
 */
public abstract class ZSaveHandler implements ZEntityHandler {

	@Override
	public SUA handle(final SUA sua) {
		return sua;
	}

	@Override
	public boolean condition(final Class<?> entityClass) {
		return false;
	}

}
