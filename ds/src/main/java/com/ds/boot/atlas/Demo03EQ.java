package com.ds.boot.atlas;

import java.util.Arrays;
import java.util.Collections;

import com.haizhi.atlasgraph.constant.FieldType;
import com.haizhi.atlasgraph.constant.LogicOperator;
import com.haizhi.atlasgraph.model.query.FieldFilter;
import com.haizhi.atlasgraph.model.query.Filters;
import com.haizhi.atlasgraph.model.query.LabelType;
import com.haizhi.atlasgraph.model.query.LogicFilter;
import com.haizhi.atlasgraph.model.query.Operator;
import com.haizhi.atlasgraph.model.query.graph.EdgeQuery;
import com.haizhi.atlasgraph.model.query.property.EdgePropertyQuery;
import com.haizhi.atlasgraph.model.query.property.EdgesQuery;
import com.haizhi.atlasgraph.model.query.property.VertexPropertyQuery;

/**
 * @author ds
 * @date 2026/5/26
 * @description
 */
public class Demo03EQ {
	public static void main(String[] args) {
		// 点查询
//		EdgePropertyQuery edgePropertyQuery = new EdgePropertyQuery();
//		edgePropertyQuery.setLabel("a");
//		edgePropertyQuery.setProperty("name");
//		edgePropertyQuery.setValues(Arrays.asList("重庆测试技术有限公司"));
//		System.out.println(edgePropertyQuery.queryString());

		EdgeQuery edgeQuery = new EdgeQuery();
		edgeQuery.setLabel("loan_pay");
//
		FieldFilter fieldFilter = new FieldFilter();
		fieldFilter.setOperator(Operator.EQ);
		fieldFilter.setLabel("loan_pay");
		fieldFilter.setLabelType(LabelType.EDGE);
		fieldFilter.setField("_to");
		fieldFilter.setFieldType(FieldType.STRING);
		fieldFilter.setValue("Company/1");

		FieldFilter fieldFilter1 = new FieldFilter();
		fieldFilter1.setOperator(Operator.EQ);
		fieldFilter1.setLabel("loan_pay");
		fieldFilter1.setLabelType(LabelType.EDGE);
		fieldFilter1.setField("_from");
		fieldFilter1.setFieldType(FieldType.STRING);
		fieldFilter1.setValue("Company/2");

		LogicFilter logicFilter = new LogicFilter(LogicOperator.AND);
		logicFilter.setFilters(Arrays.asList(fieldFilter, fieldFilter1));

		edgeQuery.setFilters(logicFilter);


		System.out.println(edgeQuery.queryString());
	}
}
