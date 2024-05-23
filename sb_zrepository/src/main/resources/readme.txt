
# 使用说明：
# 一  springboot 项目引入maven依赖后可以直接使用，已封装为starter形式
	 sb_zframework 项目引入maven依赖后可以直接使用，已封装为starter形式
	
	1 新springboot项目 A ，引入
		
			<dependency>
				<groupId>com.vo</groupId>
				<artifactId>sb_zrepository</artifactId>
				<version>0.0.1-SNAPSHOT</version>
			</dependency>
	
	2 resources目录下新建 zdatasource.properties 
	  配置详见文件 zdatasource.properties
	  	
	  	目前支持：mysql和postgresql，只测试了ubuntu上的mysql-8.0 percona-mysql-5.7 postgresql-14和11版本
		
	3 声明一个Entity，如：
	
		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		@ZEntity(tableName = "user")
		public class UserEntity {
		
			@ZID
			private Long id;
		
			private Integer orderCount;
		
			private String name;
		
			private Integer age;
		
			private Integer status;
		
		}
		
		# @ZEntity 里支持的注解：
		
		# @ZID 
			表示一个Field是table的主键
		# @ZTransient 
			表示一个字段不与table中的column对应，只在java代码里使用
	
	4 声明 UserEntity 对应的 UserRepository，此接口需要继承ZRepository接口,
	
		public interface UserRepository extends com.vo.ZRepository<UserEntity, Long> {
			
			// 支持声明式方法，如下：
			// 参数类型和名称需要 与 UserEntity 中匹配
			
			// 等同于 select UserEntity所有字段 from user where name = name参数值;
			List<UserEntity> findByName(String name);
			
			// 等同于 select count(*) from user where age = age参数值;
			Long countingByAge(Integer age);
			
			// 等同于 select UserEntity所有字段 from user where name like %name参数值%;
			List<UserEntity> findByNameLike(String name);
			
			// 等同于 select UserEntity所有字段 from user where status = status参数值 order by id desc limit limit参数值 offset offset参数值;
			List<UserEntity> findByStatusOrderByIdDescLimit(Long status, int limit, int offset);
			
			..........
			..........
		
		}
		
		# ZRepository 的两个泛型参数：
			第一个是子接口对应的@ZEntity类，本例中是 UserEntity类。
			第二个是@ZEntity类的@ZID字段的类型，本例中是Long
		
		到此，UserRepository 接口已继承 ZRepository 中固定的一些方法，
		支持的声明式方法详见：MethodRegex中的GROUP_开头的正则表达式.
		
		# 声明式方法findBy开头的，支持自定义返回对象，如：定义一个MyEntity，
		里面只有 id和name两个Field，则生成的SQL的select部分为 select id,name
			可使用自定义类型来减少网络传输、利用索引覆盖、屏蔽敏感字段、让业务逻辑更清晰等等。 
		
		
	5 @Autowired  UserRepository userRepository;
	  即可使用 UserRepository 中声明式方法和 ZRepository 中的固有方法
	  
	6 事务：注解 @ZTransaction
		在需要事务的方法上加上此注解接口实现事务
		
# 二 其他使用,调用  ZRepositoryStarter.startZRepository(扫描的包名)， 
	得到Map<Class,ZClass> 为Map<ZRepository子接口的Class，其代理类的ZClass>。自行处理
	
				
	  
  
	
	