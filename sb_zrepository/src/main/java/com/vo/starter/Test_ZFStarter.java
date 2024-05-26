package com.vo.starter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import com.vo.ScanPackage;
import com.vo.configuration.ZProperties;
import com.vo.core.ZClass;
import com.vo.core.ZContext;
import com.vo.core.ZLog2;

import cn.hutool.core.util.StrUtil;

/**
 * 测试 通过 zframework.factories 指定的启动类
 *
 * @author zhangzhen
 * @date 2024年2月17日
 *
 */
public class Test_ZFStarter implements ZStarter {

	private static final ZLog2 LOG = ZLog2.getInstance();

	@Override
	public void start() {

		final String scanPackageName = String
				.valueOf(ZProperties.getInstance().getProperty("zrepository.scanPackageName"));

		if (StrUtil.isEmpty(scanPackageName)) {
			throw new IllegalArgumentException("zrepository.scanPackageName 未配置！");
		}

		ScanPackage.set(Sets.newHashSet(scanPackageName));

		final Map<Class, ZClass> clsMap = ZRepositoryStarter.startZRepository(scanPackageName);
		final Set<Entry<Class, ZClass>> es = clsMap.entrySet();
		for (final Entry<Class, ZClass> entry : es) {
			LOG.info("开始注入实现类[{}]", entry.getValue().getName());
			ZContext.addBean(entry.getKey().getClass(), entry.getValue().newInstance());
			LOG.info("注入实现类[{}]成功", entry.getValue().getName());
		}

	}

}
