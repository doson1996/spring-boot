package com.ds.boot.atlas;

import java.util.Arrays;

import com.haizhi.atlasgraph.constant.FieldType;
import com.haizhi.atlasgraph.model.query.FieldFilter;
import com.haizhi.atlasgraph.model.query.LabelType;
import com.haizhi.atlasgraph.model.query.Operator;
import com.haizhi.atlasgraph.model.query.property.VertexPropertyQuery;

/**
 * @author ds
 * @date 2026/5/26
 * @description
 */
public class Demo02VQ {
	public static void main(String[] args) {
		VertexPropertyQuery vertexQuery1 = new VertexPropertyQuery();
		vertexQuery1.setLabel("Person");
		vertexQuery1.setProperty("object_key");
		vertexQuery1.setValues(Arrays.asList("Person/1"));
		System.out.println(vertexQuery1.queryString());


		// 点查询
		VertexPropertyQuery vertexQuery = new VertexPropertyQuery();
		vertexQuery.setLabel("Company");
		vertexQuery.setProperty("name");
		vertexQuery.setValues(Arrays.asList("重庆测试技术有限公司"));

		FieldFilter fieldFilter = new FieldFilter();
		fieldFilter.setOperator(Operator.EQ);
		fieldFilter.setLabel("Company");
		fieldFilter.setLabelType(LabelType.VERTEX);
		fieldFilter.setField("a");
		fieldFilter.setFieldType(FieldType.STRING);
		fieldFilter.setValue("重庆测试技术有限公司");

		vertexQuery.setFilters(fieldFilter);

		System.out.println(vertexQuery.queryString());
	}
}
