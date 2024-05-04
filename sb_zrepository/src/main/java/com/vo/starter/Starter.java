package com.vo.starter;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 *
 * spring.factories 中的配置启动类
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
@Component
@Import(value = {ZRepositoryStarter.class })
public class Starter {

}
