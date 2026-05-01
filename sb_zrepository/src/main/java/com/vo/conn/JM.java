package com.vo.conn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vo.core.ZMethod;
import com.vo.core.ZMethodArg;

/**
 * 为兼容janino而新增的几个方法
 *
 * @author zhangzhen
 * @date 2025年9月16日
 *
 */
class JM {

	public static ZMethod addSave(final String entityT) {

		final ZMethod method = new ZMethod();

		method.setName("save");
		method.setReturnType("Object");
		final List<ZMethodArg> mm = new ArrayList<>();
		mm.add(new ZMethodArg(Object.class, "x"));
		method.setMethodArgList(mm);
		method.setBody("return this.save((" + entityT + ")x);");
		method.setgReturn(false);

		return method;

	}

	public static ZMethod addDeleteById(final String pT) {

		final ZMethod method = new ZMethod();

		method.setName("deleteById");
		method.setReturnType("boolean");
		final List<ZMethodArg> mm = new ArrayList<>();
		mm.add(new ZMethodArg(Object.class, "x"));
		method.setMethodArgList(mm);

		method.setBody("return this.deleteById((" + pT + ")x);");
		method.setgReturn(false);

		return method;

	}

	public static ZMethod addExistByIdById(final String pT) {

		final ZMethod method = new ZMethod();

		method.setName("existById");
		method.setReturnType("boolean");
		final List<ZMethodArg> mm = new ArrayList<>();
		mm.add(new ZMethodArg(Object.class, "x"));
		method.setMethodArgList(mm);
		method.setBody("return this.existById((" + pT + ")x);");
		method.setgReturn(false);
		return method;

	}

	public static ZMethod addFindById(final String pT) {

		final ZMethod method = new ZMethod();

		method.setName("findById");
		method.setReturnType("Object");
		final List<ZMethodArg> mm = new ArrayList<>();
		mm.add(new ZMethodArg(Object.class, "x"));
		method.setMethodArgList(mm);
		method.setBody("return this.findById((" + pT + ")x);");
		method.setgReturn(false);

		return method;

	}
	public static ZMethod addFindOptionalById(final String pT) {

		final ZMethod method = new ZMethod();

		method.setName("findOptionalById");
		method.setReturnType(Optional.class.getCanonicalName());
		final List<ZMethodArg> mm = new ArrayList<>();
		mm.add(new ZMethodArg(Object.class, "x"));
		method.setMethodArgList(mm);
		method.setBody("return this.findOptionalById((" + pT + ")x);");
		method.setgReturn(false);

		return method;

	}

	public static ZMethod addUpdate(final String eT) {

		final ZMethod method = new ZMethod();

		method.setName("update");
		method.setReturnType("boolean");
		final List<ZMethodArg> mm = new ArrayList<>();
		mm.add(new ZMethodArg(Object.class, "x"));
		method.setMethodArgList(mm);
		method.setBody("return this.update((" + eT + ")x);");
		method.setgReturn(false);
		return method;

	}

}
