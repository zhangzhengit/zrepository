package com.vo.repository.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.ResourceUtils;

import com.vo.repository.exception.ZRepositoryException;
import com.vo.zframework.cache.CU;
import com.vo.zframework.cache.STU;

/**
 * 读取xml文件
 *
 * @author zhangzhen
 * @date 2024年7月1日 下午5:01:53
 *
 */
public class ZXML {

	private static final String CLASSPATH = "classpath:";
	private static final String ID = "id";
	private static final String SELECT = "select";

	public static String read(final String xmlFileName, final String methodName) {
		File file = null;
		try {
			file = ResourceUtils.getFile(CLASSPATH + xmlFileName);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			throw new ZRepositoryException("读取xml文件失败：文件[" + xmlFileName + "]不存在 ");
		}

		try {
			final SAXReader reader = new SAXReader();
			final Document document = reader.read(file);
			final Element root = document.getRootElement();

			final List<Element> selectList = root.elements(SELECT);
			if (CU.isEmpty(selectList)) {
				throw new ZRepositoryException("读取xml文件失败：文件[" + xmlFileName + "]不存在任何[select]内容");
			}

			for (final Element selectElement : selectList) {
				final Attribute id = selectElement.attribute(ID);
				if (id == null) {
					throw new ZRepositoryException("读取xml文件失败：文件[" + xmlFileName + "]请确保所有[select]标签都有[id]属性");
				}

				final String idValue = id.getValue();
				if (STU.isEmpty(idValue)) {
					throw new ZRepositoryException("读取xml文件失败：文件[" + xmlFileName + "]所有[select]标签的[id]属性都不能为空");
				}

				final String sql = selectElement.getTextTrim();
				if (STU.isEmpty(sql)) {
					throw new ZRepositoryException("读取xml文件失败：文件[" + xmlFileName + "]的[select]标签里的内容不能为空");
				}
				if (methodName.equals(idValue)) {
					return sql;
				}

			}
		} catch (final DocumentException e1) {
			e1.printStackTrace();
		}

		throw new ZRepositoryException("读取xml文件失败：文件[" + xmlFileName + "]不存在[id]等于[" + methodName + "]的标签");
	}
}