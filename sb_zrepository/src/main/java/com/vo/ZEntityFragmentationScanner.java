package com.vo;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年6月17日
 *
 */
public class ZEntityFragmentationScanner {

//	public static void main(final String[] args) {
//		test_1();
//	}

	public static void test_1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZEntityFragmentationScanner.test_1()");

//		final Set<Class<?>> cs = ClassUtil.scanPackage("com");
//		final Set<Class<?>> cs = ZRMain.scanPackage_COM();
//		for (final Class<?> c : cs) {
//			if (c.isAnnotationPresent(ZEntity.class) && c.isAnnotationPresent(ZEntityFragmentation.class)) {
//				System.out.println("一个分表的entity = " + c.getName());
//
//				final ZEntity ze = c.getAnnotation(ZEntity.class);
//				final ZEntityFragmentation zef = c.getAnnotation(ZEntityFragmentation.class);
//				System.out.println("zef = " + zef);
//				final int from = 1;
//				final int end = zef.end();
//				for(int i = from;i<=end;i++) {
//					final String tableName = ze.tableName() + "_" + i;
//					System.out.println(tableName);
//				}
//			}
//		}


	}

}
