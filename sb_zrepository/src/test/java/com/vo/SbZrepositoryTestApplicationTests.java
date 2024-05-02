package com.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vo.core.Page;
import com.votool.ze.AbstractZETask;
import com.votool.ze.ZE;
import com.votool.ze.ZES;
import com.votool.ze.ZETaskResult;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.UUID;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年8月21日
 *
 */
@SpringBootTest
class SbZrepositoryTestApplicationTests {

	@Autowired
	Number2ZRepository mmmmmm;

	@Autowired
	NumberZRepository nnnnnnnnn;

	final ZE ze = ZES.newZE(40, "ZR-TEST-THREAD-");
//	final ZE ze = ZES.newZE(Runtime.getRuntime().availableProcessors() * 10, "ZR-TEST-THREAD-");


	@Test
	void findByNameIn1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.findByNameIn1()");

		final int n = 1;

		final HashSet<String> set = Sets.newHashSet();
		for(int i = 1;i<=n;i++) {
			final String name = UUID.randomUUID().toString();
			final NumberEntity entity = new NumberEntity();
			entity.setName(name);
			final NumberEntity save = this.nnnnnnnnn.save(entity);
			set.add(save.getName());
		}

		final List<NumberEntity> findByNemeIn = this.nnnnnnnnn.findByNameIn(Lists.newArrayList(set));

		assertThat(findByNemeIn.size() == n);

		for (final NumberEntity numberEntity : findByNemeIn) {
			assertThat(set.contains(numberEntity.getName()));
		}

	}

	@Test
	void ZFieldConverterTest1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.update_N3()");

		final String[] na = {
				"jobId",
				"targetNodeId",
				"createTime",
				"orderStatus",
				"nickName",
				"name",
				"id",
				"aaaBBc1DV23X",
				"vLKJKLMCLVMJ23LKVJljLKJ23923okjLKJLKJLKwepwlkfjk222KKk",
				"abcdLjlskjdflLKJLNLKJOIlknLmlxmcllikwlkjLKJLSDXKjNVLKJs"

		};
		for (final String name : na) {
			final String dbField = ZFieldConverter.toDbField(name);
			final String javaField = ZFieldConverter.toJavaField(dbField);

			assertThat(name.equals(javaField));

		}
	}

	public static void gJavaFieldName() {

	}

	@Test
	void update_N3() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.update_N3()");

		final int k = 12;

		final int n = 100;

		final Set<Integer> idSet = new ConcurrentHashSet<>();
		final AtomicInteger w = new AtomicInteger();

		for (int x = 1; x <= k; x++) {
			this.ze.executeInQueue(() -> {

				final ArrayList<NumberEntity> sl = Lists.newArrayList();
				for (int i = 1; i <= n; i++) {
					final NumberEntity entity = new NumberEntity();
					entity.setAge(i);
					sl.add(entity);
				}
				final List<Integer> saveAll = this.nnnnnnnnn.saveAll(sl);
				idSet.addAll(saveAll);

				w.incrementAndGet();
			});
		}
		while(w.get() < k) {

		}
		System.out.println("k * n = " + (k * n));
		System.out.println("idSets.size = " + idSet.size());
		assertThat(idSet.size() == (k * n));
		System.out.println("udpate 开始");


		final AtomicInteger u = new AtomicInteger();

		for (final Integer id : idSet) {
			this.ze.executeInQueue(() -> {

				final NumberEntity f = this.nnnnnnnnn.findById(id);
				final String name = UUID.randomUUID().toString();
				f.setName(name);

				final NumberEntity update = this.nnnnnnnnn.update(f);
				final NumberEntity f2 = this.nnnnnnnnn.findById(update.getId());

				// FIXME 2023年9月24日 下午3:49:06 zhanghen: debug 并发，下面断言错误
//				assertThat(name.equals(f2.getName()));
				if(!name.equals(f2.getName())) {
					final int x =230;
				}

				u.incrementAndGet();
			});
		}

		while (u.get() < idSet.size()) {

		}
	}

	@Test
	void update_N2() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.update_N2()");

		final int n = 1000;

		final ArrayList<NumberEntity> sl = Lists.newArrayList();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(i);
			sl.add(entity);
		}

		final List<Integer> saveAll = this.nnnnnnnnn.saveAll(sl);
		assertThat(saveAll.size() == n);

		final List<NumberEntity> findByIdIn = this.nnnnnnnnn.findByIdIn(saveAll);
		assertThat(findByIdIn.size() == n);

		final AtomicInteger w = new AtomicInteger();
		for (final NumberEntity numberEntity : findByIdIn) {

			this.ze.executeInQueue(() -> {

				numberEntity.setName("UUU" + numberEntity.getId());
				final NumberEntity update = this.nnnnnnnnn.update(numberEntity);
				final NumberEntity f = this.nnnnnnnnn.findById(numberEntity.getId());
				assertThat(("UUU" + numberEntity.getId()).equals(f.getName()));
				assertThat(f.getId().equals(numberEntity.getId()));

				w.incrementAndGet();
			});
		}

		while (w.get() < n) {

		}
		System.out.println("OK");

	}

	@Test
	void saveAll_N1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.save_N1()");

		final NumberEntity entity = new NumberEntity();
		entity.setBigint1(2000L);
		entity.setOrderCount(1);
		entity.setUserId(200);
		entity.setName("lisi");
		entity.setId(1);
		entity.setStatus(1);
		entity.setSmallint1(3);
		entity.setAge(665);
		entity.setId(23434340);
		final Date now = new Date();
		entity.setCreateTime(now);

		this.nnnnnnnnn.deleteById(entity.getId());

		final List<Integer> saveAll = this.nnnnnnnnn.saveAll(Lists.newArrayList(entity));

		assertThat(saveAll.size() == 1);
		assertThat(!saveAll.get(0).equals(entity.getId()));

		final NumberEntity f = this.nnnnnnnnn.findById(saveAll.get(0));
//		assertThat(f.getCreateTime().equals(now));

		System.out.println("f.getCreateTime() = " + f.getCreateTime());
		System.out.println("now = " + now);
	}


	@Test
	void update_N1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.update_N1()");
		final NumberEntity entity = new NumberEntity();
		entity.setBigint1(2000L);
		entity.setOrderCount(1);
		entity.setUserId(200);
		entity.setName("lisi");
		entity.setId(1);
		entity.setStatus(1);
		entity.setSmallint1(3);
		entity.setAge(665);
		final NumberEntity s = this.nnnnnnnnn.save(entity);

		s.setName("这是save以后set的属性，测试update后是否存在于数据库，存在就对了");

//		s.setId(null);
		final NumberEntity update = this.nnnnnnnnn.update(s);

		assertThat(update.getName().endsWith("这是save以后set的属性，测试update后是否存在于数据库，存在就对了"));
	}


	@Test
	void save_N1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.save_N1()");

		final NumberEntity entity = new NumberEntity();
		entity.setBigint1(2000L);
		entity.setOrderCount(1);
		entity.setUserId(200);
		entity.setName("lisi");
		entity.setId(1);
		entity.setStatus(1);
		entity.setSmallint1(3);
		entity.setAge(665);
		final NumberEntity s = this.nnnnnnnnn.save(entity);

		assertThat(s.getAge().equals(entity.getAge()));
		assertThat(s.getName().equals(entity.getName()));
		assertThat(s.getBigint1().equals(entity.getBigint1()));
		assertThat(s.getOrderCount().equals(entity.getOrderCount()));
		assertThat(s.getUserId().equals(entity.getUserId()));
		assertThat(s.getStatus().equals(entity.getStatus()));
		assertThat(s.getSmallint1().equals(entity.getSmallint1()));


		final NumberEntity s2 = this.nnnnnnnnn.save(s);

	}

	@Test
	void NN_MMM2() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.NN_MMM2()");

		final int n = 100;

		final AtomicInteger w = new AtomicInteger(0);
		final Set<Object> ssss = Sets.newConcurrentHashSet();

		final Thread thread1 = new Thread(() -> {
			for (int i = 1; i <= n; i++) {
				final NumberEntity entity = new NumberEntity();
				entity.setAge(i);
				final NumberEntity save = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.save(entity);
				ssss.add(save);
			}
		});
		thread1.setName("AAAAAAAAAAA");


		final Thread thread2 = new Thread(() -> {
			for (int i = 1; i <= n; i++) {
				final Number2Entity entity2 = new Number2Entity();
				entity2.setAge(666);
				final Number2Entity save2 = SbZrepositoryTestApplicationTests.this.mmmmmm.save(entity2);
				ssss.add(save2);
			}
		});
		thread2.setName("BBBBBBBBBBBBB");

		thread1.start();
		thread2.start();

		while(ssss.size() < (n * 2)) {

		}
		System.out.println("OK");


	}
	@Test
	void NN_MMM() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.NN_MMM()");

		final int n = 100;

		final AtomicInteger w = new AtomicInteger(0);

		final Set<Object> ssss = Sets.newConcurrentHashSet();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(i);

			final boolean executeInQueue = this.ze.executeInQueue(() -> {
				final NumberEntity save = this.nnnnnnnnn.save(entity);
				ssss.add(save);

				w.incrementAndGet();
			});
			final boolean executeInQueu2e = this.ze.executeInQueue(() -> {

				final Number2Entity entity2 = new Number2Entity();
				entity2.setAge(666);
				final Number2Entity save2 = this.mmmmmm.save(entity2);
				ssss.add(save2);

				w.incrementAndGet();
			});
		}

		while(w.get() < (n * 2)) {

		}
		System.out.println("OK");
		System.out.println("sss.size = " + ssss.size());


	}


	@Test
	void page_1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.page_1()");

		System.out.println();

		final int n = 200;
		final AtomicInteger w = new AtomicInteger();

		for(int i = 1;i<=n;i++) {

			this.ze.executeInQueue(() -> {

				final NumberEntity entity = new NumberEntity();
//		entity.setId(47797);
				entity.setAge(33333333);
				entity.setStatus(1);
//		entity.setName("zhangsan");

				final Page<NumberEntity> page = this.nnnnnnnnn.page(entity, 1, 3333);

				System.out.println();

				System.out.println("当前页 = " + page.getPage());
				System.out.println("页条数 = " + page.getSize());
				System.out.println("总页数 = " + page.getTotalPage());
				System.out.println("总条数 = " + page.getTotalCount());
				System.out.println("内容数 = " + page.getList().size());
				System.out.println("有下页 = " + page.hasNextPage());
				System.out.println("有上页 = " + page.hasPreviousPage());
				 System.out.println("有内容 = " + page.hasContent());

				 w.incrementAndGet();
			});
		}

		while (w.get() < n) {

		}
		System.out.println("OK");

	}

	@Test
	void findByAgeOrderByIdDescLimit() {
//		this.nnnnnnnnn.deleteAll();

		final int status = 1;

		final List<NumberEntity> list = this.nnnnnnnnn.findByStatusOrderByIdDescLimit(status, 0,13200);
		System.out.println("list.size = " + list.size());
		System.out.println("list = " + list);
	}


	@Test
	void findByIdLessThanEquals() {

		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.findByIdLessThanEquals()");

		final List<NumberEntity> findByIdLessThanEquals = this.nnnnnnnnn.findByAgeLessThanEquals(200000);
		System.out.println("findByIdLessThanEquals.size = " + findByIdLessThanEquals.size());
	}


	@Test
	void save_b() {

		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.save_b()");

		final int n = 100;

		final AtomicInteger w = new AtomicInteger(0);

		final Set<NumberEntity> ssss = Sets.newConcurrentHashSet();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(i);

			final boolean executeInQueue = this.ze.executeInQueue(() -> {
				final NumberEntity save = this.nnnnnnnnn.save(entity);
				ssss.add(save);
				w.incrementAndGet();
			});
		}

		while (w.get() < n) {

		}
		System.out.println("OK");
		assertThat(w.get() == n);

		final List<Integer> idl = ssss.stream().map(NumberEntity::getId).collect(Collectors.toList());
		assertThat(idl.size() == n);

	}


	@Test
	void findByIdIN_B() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.findByIdIN_B()");

		final int n = 100;
		final ArrayList<NumberEntity> sl = Lists.newArrayList();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(12345);

			sl.add(entity);
		}

		final List<Integer> saveAll = this.nnnnnnnnn.saveAll(sl);
		assertThat(saveAll.size() == n);

		final ArrayList<AbstractZETask<NumberEntity>> taskList = Lists.newArrayList();
		for (final Integer id : saveAll) {

			final AbstractZETask<NumberEntity> task = new AbstractZETask<NumberEntity>() {

				@Override
				public NumberEntity call() {
//					System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
//							+ "SbZrepositoryTestApplicationTests.findByIdIN_B().new AbstractZETask() {...}.call()");
					final NumberEntity e = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.findById(id);
					e.setName(Thread.currentThread().getName());
					return e;
				}
			};
			taskList.add(task);
		}


		final List<ZETaskResult<NumberEntity>> rl = this.ze.submitInQueue(taskList);
		int okC=0;
		for (final ZETaskResult<NumberEntity> r : rl) {
			final NumberEntity numberEntity = r.get();
			System.out.println("r = " + numberEntity);

			okC++;
		}
		assertThat(okC == n);

		final List<NumberEntity> f2 = this.nnnnnnnnn.findByIdIn(saveAll);
		assertThat(f2.size() == saveAll.size());

		final boolean deleteByIdIn = this.nnnnnnnnn.deleteByIdIn(saveAll);
		assertThat(deleteByIdIn);

		final List<NumberEntity> fD2 = this.nnnnnnnnn.findByIdIn(saveAll);
		assertThat(fD2.size() == 0);

	}

	@Test
	void findByXXIn() {
		this.nnnnnnnnn.deleteAll();


		final NumberEntity entity = new NumberEntity();
		entity.setAge(12345);

		this.nnnnnnnnn.save(entity);

		final List<NumberEntity> findByAgeIn = this.nnnnnnnnn.findByAgeIn(Lists.newArrayList(1, 21, 33, 42323, 5, 12345));
		System.out.println("findByAgeIn = " + findByAgeIn);

		assertThat(findByAgeIn.size() == 1);


	}

	@Test
	void save_BING() {
		final int n = 250;

//		final ArrayList<NumberEntity> sl = Lists.newArrayList();

		final ArrayList<AbstractZETask<NumberEntity>> taskList = Lists.newArrayList();

		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(12345);


			final AbstractZETask<NumberEntity> task = new AbstractZETask<NumberEntity>() {

				@Override
				public NumberEntity call() {
					final NumberEntity e = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.save(entity);
					return e;
				}
			};
			taskList.add(task);
		}


		final ZE ze = ZES.newZE();
		final List<ZETaskResult<NumberEntity>> sxl = ze.submitInQueue(taskList);

		final HashSet<Object> idSet = Sets.newHashSet();
		for (final ZETaskResult<NumberEntity> r : sxl) {
			System.out.println("r = " + r.get());
			idSet.add(r.get().getId());
		}

		assertThat(idSet.size() == n);
	}


	@Test
	void deleteByIdIn2() {

		final int n = 450;

		final ArrayList<NumberEntity> sl = Lists.newArrayList();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(12345);
			sl.add(entity);

		}

		final List<Integer> saveAll = this.nnnnnnnnn.saveAll(sl);
		System.out.println("saveAll.size = " + saveAll.size());
		assertThat(saveAll.size() == n);

		final ArrayList<AbstractZETask<Boolean>> taskList = Lists.newArrayList();
		for (final Integer id : saveAll) {

			final AbstractZETask<Boolean> task = new AbstractZETask<Boolean>() {

				@Override
				public Boolean call() {
					final boolean deleteById = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.deleteById(id);
					return deleteById;
				}
			};
			taskList.add(task);
		}

		final ZE ze = ZES.newZE();
		final List<ZETaskResult<Boolean>> sxl = ze.submitInQueue(taskList);

		int okC = 0;
		for (final ZETaskResult<Boolean> result : sxl) {
			System.out.println("result = " + result.get());
			if(result.get()) {
				okC++;
			}
		}

		assertThat(okC == saveAll.size());
		assertThat(okC == n);

		final List<NumberEntity> f2 = this.nnnnnnnnn.findByIdIn(saveAll);
		assertThat(f2.size() == 0);

		System.out.println("OK");

		this.nnnnnnnnn.deleteAll();
	}


	@Test
	void deleteByIdIn1() {

		final int n = 12220;

		final ArrayList<NumberEntity> sl = Lists.newArrayList();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(12345);
			sl.add(entity);

		}

		final List<Integer> saveAll = this.nnnnnnnnn.saveAll(sl);
		System.out.println("saveAll.size = " + saveAll.size());
		final boolean deleteByIdIn = this.nnnnnnnnn.deleteByIdIn(saveAll);

		final List<NumberEntity> findByIdIn = this.nnnnnnnnn.findByIdIn(saveAll);
		assertThat(findByIdIn.size() == 0);
	}

	@Test
	void update2() {

		this.nnnnnnnnn.deleteAll();

		final int n = 120;

		final ArrayList<NumberEntity> sl = Lists.newArrayList();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(12345);
			final NumberEntity save = this.nnnnnnnnn.save(entity);
			sl.add(save);
//			if(save == null) {
//				System.out.println("saveIsNUll,save = " + save);
//			}
//			if (save.getId() == null) {
//				System.out.println("saveIdIsNUll,save = " + save);
//			}
		}

		assertThat(sl.size() == n);



		final List<Integer> idF = sl.stream().map(NumberEntity::getId).collect(Collectors.toList());
		final List<NumberEntity> findByIdIn = this.nnnnnnnnn.findByIdIn(idF);
		assertThat(findByIdIn.size() == n);

		for (final NumberEntity e1 : findByIdIn) {
			final boolean anyMatch = sl.stream().anyMatch(e -> e.getId().equals(e1.getId()));
			assertThat(anyMatch);
		}


		for (final NumberEntity e1 : sl) {
			e1.setAge(9999999);
			final NumberEntity update = this.nnnnnnnnn.update(e1);
			assertThat(update.getAge().equals(9999999));

			final NumberEntity findById = this.nnnnnnnnn.findById(e1.getId());
			assertThat(findById.getAge().equals(9999999));

		}


		final List<NumberEntity> findByAge = this.nnnnnnnnn.findByAge(9999999);

		final Long countingByAge = this.nnnnnnnnn.countingByAge(9999999);
		assertThat(findByAge.size() >= sl.size());
		assertThat(findByAge.size() == countingByAge.intValue());

		for (final NumberEntity e1 : sl) {
			this.nnnnnnnnn.deleteById(e1.getId());
		}

		final Long countingByAgeDDDD = this.nnnnnnnnn.countingByAge(9999999);
		assertThat(countingByAgeDDDD.intValue() >= 0);

		for (final NumberEntity numberEntity : findByAge) {
			this.nnnnnnnnn.deleteById(numberEntity.getId());
		}


		final Long countingByAgeDDDDxxx = this.nnnnnnnnn.countingByAge(9999999);
		assertThat(countingByAgeDDDDxxx.intValue() == 0);

	}

	@Test
	void update1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.update1()");

		this.nnnnnnnnn.deleteAll();

		final NumberEntity entity = new NumberEntity();
		entity.setAge(12345);
		final NumberEntity save = this.nnnnnnnnn.save(entity);

		assertThat(save.getAge().equals(12345));

		save.setAge(55555);
		final NumberEntity update = this.nnnnnnnnn.update(save);

		assertThat(update.getAge().equals(55555));

	}

	@Test
	void findByAgeNot() {
		this.nnnnnnnnn.deleteAll();

		final List<NumberEntity> not200ageList = this.nnnnnnnnn.findByAgeNot(200);
		System.out.println("not200ageList.size = " + not200ageList.size());
		System.out.println("not200ageList = " + not200ageList);

		final List<NumberEntity> e200ageList = this.nnnnnnnnn.findByAge(200);
		System.out.println("e200ageList.size = " + e200ageList.size());
		System.out.println("e200ageList = " + e200ageList);
		final List<NumberEntity> nullList = this.nnnnnnnnn.findByAgeIsNull(200);
		System.out.println("nullList.size = " + nullList.size());
		System.out.println("nullList = " + nullList);

		final Long count = this.nnnnnnnnn.count();
		assertThat(count.intValue() == (e200ageList.size() + not200ageList.size()  + nullList.size()));
	}

	@Test
	void deleteById() {
		this.nnnnnnnnn.deleteAll();
		final boolean deleteById = this.nnnnnnnnn.deleteById(1232323);
		System.out.println("deleteById = " + deleteById);
	}

	@Test
	void existById() {
		this.nnnnnnnnn.deleteAll();
		final boolean existById = this.nnnnnnnnn.existById(1);
		System.out.println("existById = " + existById);
	}

	@Test
	void test_countingBy() {

		final Long countingBy = this.nnnnnnnnn.countingByAge(200);
		System.out.println("countingBy = " + countingBy);
	}

	@Test
	void test_findByNameEndingWith() {

		final List<NumberEntity> findByNameEndingWith = this.nnnnnnnnn.findByNameEndingWith("a");
		System.out.println("findByNameEndingWith.size = " + findByNameEndingWith.size());
		System.out.println("findByNameEndingWith = " + findByNameEndingWith);
	}

	@Test
	void test_count1() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.test_count1()");


		final List<NumberEntity> findAll = this.nnnnnnnnn.findAll();
		System.out.println("findAll.size = " + findAll.size());

		final Long count = this.nnnnnnnnn.count();
		assertThat(count.intValue() == findAll.size());

		final NumberEntity entity = new NumberEntity();
		entity.setAge(2023023);
		this.nnnnnnnnn.save(entity);

		final Long c2 = this.nnnnnnnnn.count();
		assertThat(c2 == (count + 1L));

		this.nnnnnnnnn.deleteAll();
	}


	@Test
	void test_findAll1() {

		final List<NumberEntity> findAll = this.nnnnnnnnn.findAll();
		System.out.println("findAll.size = " + findAll.size());
		System.out.println("findAll = " + findAll);

		this.nnnnnnnnn.deleteAll();
	}

	@Test
	void test_findByXXLike_2() {
		this.nnnnnnnnn.deleteAll();

		final List<NumberEntity> list = this.nnnnnnnnn.findByNameLike("a");
		System.out.println("list.size = " + list.size());
		System.out.println("list = " + list);

		this.nnnnnnnnn.deleteAll();
	}

	@Test
	void test_findByXXLike_1() {
		this.nnnnnnnnn.deleteAll();

		final List<NumberEntity> list = this.nnnnnnnnn.findByNameStartingWith("1a");
		System.out.println("list.size = " + list.size());
		System.out.println("list = " + list);

		this.nnnnnnnnn.deleteAll();
	}

	@Test
	void test_findByUserIdAndName2() {

		this.nnnnnnnnn.deleteAll();

		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.test_findByUserIdAndName2()");
		final NumberEntity e = new NumberEntity();
		e.setName(UUID.randomUUID().toString());
		e.setAge(200);


		final List<NumberEntity> l2 = this.nnnnnnnnn.findByAgeAndName(e.getAge(), e.getName());
		assertThat(l2.size() == 0);

		this.nnnnnnnnn.save(e);

		final List<NumberEntity> l2x = this.nnnnnnnnn.findByAgeAndName(e.getAge(), e.getName());
		assertThat(l2x.size() == 1);
		System.out.println("l2x = " + l2x);

		this.nnnnnnnnn.deleteAll();


	}
	@Test
	void test_findByUserIdAndName() {

		this.nnnnnnnnn.deleteAll();

		final NumberEntity e = new NumberEntity();
		e.setName(UUID.randomUUID().toString());

		final NumberEntity save = this.nnnnnnnnn.save(e);

		System.out.println("save.id = " + save.getId());
		assertThat(save.getName().equals(e.getName()));

		this.nnnnnnnnn.deleteAll();

	}


	@Test
	void test_save4() {

		this.nnnnnnnnn.deleteAll();

		final int id = 1;
		final NumberEntity entity = this.nnnnnnnnn.findById(id);
		assertThat((entity == null) || (entity.getId() == id));

		if (entity != null) {
			entity.setAge(232323);
			entity.setName("aa");
			this.nnnnnnnnn.save(entity);

			final NumberEntity s2 = this.nnnnnnnnn.findById(id);
			assertThat(s2.getId().intValue() == id);
			assertThat("aa".equals(s2.getName()));
			assertThat(s2.getAge().intValue() == entity.getAge().intValue());

		}

		this.nnnnnnnnn.deleteAll();

	}

	/**
	 *
	 * 根据id查，结果必须是id等于参数id的那一条
	 *
	 */
	@Test
	void test_save3() {

		this.nnnnnnnnn.deleteAll();

		final int n = 200;
		final AtomicInteger saveA = new AtomicInteger();
		for (int id = 1; id <= n; id++) {

			final NumberEntity entity = this.nnnnnnnnn.findById(id);
			assertThat((entity == null) || (entity.getId() == id));
		}

		this.nnnnnnnnn.deleteAll();

	}

	/**
	 * save N 此，Db中必须有save的N条，全删了才删，必须剩余0条
	 *
	 */
	@Test
	void test_save2() {

		this.nnnnnnnnn.deleteAll();

		final int n = 400;
		final AtomicInteger saveA = new AtomicInteger();
		final Set<Integer> saveId = Sets.newConcurrentHashSet();
		for (int i = 1; i <= n; i++) {

			final Integer id = i;
			this.ze.executeInQueue(() -> {

				final NumberEntity numberEntity = new NumberEntity();
				numberEntity.setId(id);
				numberEntity.setUserId(200 + id);
				final NumberEntity save = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.save(numberEntity);
				saveId.add(save.getId());
				saveA.incrementAndGet();
			});
		}

		while (saveA.get() < n) {

		}

		System.out.println("OK");

		final ArrayList<Integer> idList = Lists.newArrayList(saveId);
		final List<NumberEntity> ll = this.nnnnnnnnn.findByIdIn(idList);
		assertThat(ll.size() == n);

		this.nnnnnnnnn.deleteAll();
	}

	/**
	 * save 一个对象，然后修改其中一个字段，然后select出来，字段值必须是最后一次save的值
	 *
	 */
	@Test
	void test_save1() {

		this.nnnnnnnnn.deleteAll();

		final NumberEntity numberEntity = new NumberEntity();
//		numberEntity.setId(1);
		numberEntity.setUserId(200);
		numberEntity.setSmallint1(2);
		numberEntity.setBigint1(300000L);
		final NumberEntity save = this.nnnnnnnnn.save(numberEntity);

		numberEntity.setUserId(400);
		final NumberEntity save2 = this.nnnnnnnnn.save(numberEntity);

		assertThat((save.getId() + 1) == save2.getId());

		final NumberEntity ue = this.nnnnnnnnn.findById(save2.getId());
		System.out.println("ue = " + ue);
		if (ue.getUserId() != 400) {
			// FIXME 2023年9月6日 上午2:23:17 zhanghen: debug
			final int xn = 230;
		}
		System.out.println("ue.getUserId() = " + ue.getUserId());
		assertThat(ue.getUserId() == 400);


		this.nnnnnnnnn.deleteAll();

	}

	public static void assertThat(final boolean actual) {
		if (!actual) {
			throw new IllegalArgumentException("断言错误");
		}
	}

	@Test
	void saveAll2() {

		this.nnnnnnnnn.deleteAll();

		final ZE ze = ZES.newZE(11);

		final ArrayList<AbstractZETask<List<Integer>>> taskList = Lists.newArrayList();

		final Set<Integer> idSet = Sets.newConcurrentHashSet();

		final int x = 10;
		final int n = 10;
		for (int k = 1; k <= x; k++) {
			final AbstractZETask<List<Integer>> task = new AbstractZETask<List<Integer>>() {

				@Override
				public List<Integer> call() {
					final ArrayList<NumberEntity> sl = Lists.newArrayList();
					for (int i = 1; i <= n; i++) {
						final NumberEntity entity = new NumberEntity();
						entity.setAge(33333333);
						sl.add(entity);
					}

					final long t1 = System.currentTimeMillis();
					final List<Integer> saveAll = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.saveAll(sl);
					idSet.addAll(saveAll);
					final long t2 = System.currentTimeMillis();
					System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
							+ "saveAll-ms = " + (t2 - t1));
					// TODO Auto-generated method stub
					return saveAll;
				}
			};

			taskList.add(task);

		}

		final List<ZETaskResult<List<Integer>>> submitInQueue = ze.submitInQueue(taskList);

		final long t1 = System.currentTimeMillis();
		for (final ZETaskResult<List<Integer>> zeTaskResult : submitInQueue) {
			final List<Integer> v = zeTaskResult.get();
			System.out.println("v = " + v);
		}
		final long t2 = System.currentTimeMillis();

		assertThat(idSet.size() == (x * n));
		System.out.println("idSet.size() == x * n - OK");
		System.out.println("submitInQueue-ms = " + (t2 - t1));


		final ArrayList<AbstractZETask<Boolean>> taskDDDList = Lists.newArrayList();


		for (final Integer id : idSet) {
			final AbstractZETask<Boolean> task = new AbstractZETask<Boolean>() {

				@Override
				public Boolean call() {
					final boolean deleteById = SbZrepositoryTestApplicationTests.this.nnnnnnnnn.deleteById(id);
					return deleteById;
				}
			};
			taskDDDList.add(task);
		}

		final List<ZETaskResult<Boolean>> submitInQueue2 = ze.submitInQueue(taskDDDList);
		int okC = 0;
		for (final ZETaskResult<Boolean> zeTaskResult : submitInQueue2) {
			final Boolean v = zeTaskResult.get();
			System.out.println("delete-v = " + v);
			if(v) {
				okC++;
			}
		}

		final List<NumberEntity> findByIdIn = this.nnnnnnnnn.findByIdIn(Lists.newArrayList(idSet));
		System.out.println("idSet = " + idSet);
		System.out.println("idSet.size = " + idSet.size());
		System.out.println("okC = " + okC);
		System.out.println("findByIdIn.size = " + findByIdIn.size());
		assertThat(findByIdIn.size() == 0);

		this.nnnnnnnnn.deleteAll();


	}

	@Test
	void saveAll() {

		this.nnnnnnnnn.deleteAll();

		final int n = 2120;

		final ArrayList<NumberEntity> sl = Lists.newArrayList();
		for (int i = 1; i <= n; i++) {
			final NumberEntity entity = new NumberEntity();
			entity.setAge(33333333);
			sl.add(entity);
		}

		final List<Integer> saveAll = this.nnnnnnnnn.saveAll(sl);
		System.out.println("saveAll = " + saveAll);

		final AtomicInteger w = new
				AtomicInteger();

		for (final Integer id : saveAll) {
			this.ze.executeInQueue(() -> {
				final NumberEntity e = this.nnnnnnnnn.findById(id);
				assertThat(e.getId().equals(id));

				w.incrementAndGet();
			});

		}

		while (w.get() < n) {

		}

		final List<NumberEntity> fl = this.nnnnnnnnn.findByIdIn(saveAll);
		final boolean allMatch = fl.stream().allMatch(e ->e.getAge().equals(33333333));
		assertThat(allMatch);


		this.nnnnnnnnn.deleteAll();

	}

	@Test
	void saveAll3() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "SbZrepositoryTestApplicationTests.saveAll3()");

//		this.nnnnnnnnn.deleteAll();

		final int k = 100;
		final int n = 10;

		final AtomicInteger wanch = new AtomicInteger();

//		final ZE ze = ZES.newZE(8);
		for (int x = 1; x <= k; x++) {

			this.ze.executeInQueue(() -> {
				// TODO Auto-generated method stub

				final ArrayList<NumberEntity> sl = Lists.newArrayList();
				for (int i = 1; i <= n; i++) {
					final NumberEntity entity = new NumberEntity();
					entity.setAge(33333333);entity.setStatus(1);
					sl.add(entity);
				}
				SbZrepositoryTestApplicationTests.this.nnnnnnnnn.saveAll(sl);

				wanch.incrementAndGet();

			});
		}

		while (wanch.get() < k) {

		}

		System.out.println("OK");

		final Long count = this.nnnnnnnnn.count();
		assertThat(count.intValue() == (k * n));

	}

}
