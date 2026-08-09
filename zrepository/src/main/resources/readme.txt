
# 使用说明：
# 一  springboot 项目和 sb_zframework
	直接引入 repository_starter 的maven依赖即可使用本工程的功能。
	
	1 新springboot项目 A ，引入
		
		<dependency>
			<groupId>com.vo</groupId>
			<artifactId>repository_starter</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
	
	2 resources目录下新建 zdatasource.properties 
	  配置详见文件 zdatasource.properties
	  支持的数据库有pgsql mysql sqlite
		
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
		ZRepository<T,ID> 两个泛型参数第一个是@ZEntity标记的类，第二个是类的@ZID的字段类型
	
		public interface UserRepository extends com.vo.ZRepository<UserEntity, Long> {
			
			// 支持声明式方法，如下：
			// 参数类型和名称需要 与 UserEntity 中匹配
			
			// 等同于 SELECT [UserEntity里非@ZTransient的所有字段] FROM user WHERE name = name参数值;
			List<UserEntity> findByName(String name);
			
			// 等同于 SELECT count(*) FROM user WHERE age = age参数值;
			Long countingByAge(Integer age);
			
			// 等同于 SELECT [UserEntity里非@ZTransient的所有字段] FROM user WHERE name LIKE %name参数值%;
			List<UserEntity> findByNameLike(String name);
			
			// 等同于 SELECT [UserEntity里非@ZTransient的所有字段] FROM user WHERE status = status参数值
				 ORDER BY id DESC LIMIT limit参数值 OFFSET offset参数值;
			List<UserEntity> findByStatusOrderByIdDescLimit(Long status, int limit, int offset);
			
			..........
			..........
		
		}
		
		到此，UserRepository 接口已继承 ZRepository 中固定的一些方法，
		支持的声明式方法详见：MethodRegex中的GROUP_开头的正则表达式.
		
		# 声明式方法findBy开头的，支持自定义返回对象，如：定义一个MyEntity，
		里面只有 id和name两个Field，则生成的SQL的select部分为 SELECT id,name
			可使用自定义类型来减少网络传输、利用索引覆盖、屏蔽敏感字段、让业务逻辑更清晰等等。 
		
		
	5 @Autowired UserRepository userRepository;
	  即可使用 UserRepository 中声明式方法和 ZRepository 中的固有方法
	  
	6 事务：注解 @ZTransaction
		在需要事务的方法上加上此注解接口实现事务，如：
		
		@ZTransaction
		public void versionTestById(final Integer id) {
			final BlobEntity e = this.bbbbbbbbbbbb.findById(id);
			e.setName("这是测试 version乐观锁而改的,updateTime = " + new Date());
			final boolean update = this.bbbbbbbbbbbb.update(e);
			if (!update) {
				// 下面两种选择一种即可
				// 1
				// ZTransactionAOP.rollback();
				// 2
				throw new NullPointerException("更新失败");
			}
		}
		
		执行出现异常会自动回滚事务。如上例子需要特殊判断是否回滚的，有两种方式可以回滚事务：
			1、ZTransactionAOP.rollback();
			2、抛出一个异常
		
	7 逻辑删除：@ZLogicalDelete 
		在 @ZEntity里的字段上使用本注解，如：
		
		@ZLogicalDelete
		private Integer isDelete;
		
		在ZR.deleteById时，变为 [UPDATE TABLE SET is_delete = [指定的表示删除的值] where id = ?]
		并且所有非@ZQuery的select操作，都会在where后面加入[is_delete = [指定的表示未删除的值] ]
		
	8 乐观锁：@ZVersion
		在 @ZEntity里的字段上使用本注解，如：
		
		@ZVersion
		private Integer version;
		
		则在[findByXX、修改数据、update]的过程中会自动使用version控制乐观锁，如上三步：
		@see [6 事务：注解 @ZTransaction]
		
		1、findByXX 		得到 version = 5, 
		2、setXX			修改了部分数据
		3、update 		会自动给update语句加入  [version = 5]的条件
		
	9 使用代码外的xml文件来配置SQL：
	
		@ZQuery 放在ZR子接口的方法上，不设置sql属性，即默认为读取resources/mapper下和ZRepository子接口同名的的xml文件
		中Method同名的<select>标签内容作为SQL
	
# 二 其他使用,调用  ZRepositoryStarter.startZRepository(扫描的包名)， 
	得到Map<Class,ZClass> 为Map<ZRepository子接口的Class，其代理类的ZClass>。自行处理
	
				
	  
	 # 注意：对于数据库特有的功能，比如pgsql的jsonb类型，需要使用@ZQuery来实现，对于非SQL标准的内容没有提供模板方法和声明式方法的支持，只支持了几个常见的SQL标准的内容。
  
	
	