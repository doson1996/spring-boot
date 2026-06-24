package com.ds.boot.atlas;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import shaded.atlas.sdk.com.google.common.collect.Sets;

import com.haizhi.atlasgraph.model.query.Node;
import com.haizhi.atlasgraph.model.query.graph.GraphQuery;
import com.haizhi.atlasgraph.model.query.graph.KExpandQuery;
import com.haizhi.atlasgraph.model.query.graph.ShortestPathQuery;

/**
 * @author ds
 * @date 2026/3/25
 * @description
 */
public class Demo01 {
	public static void main(String[] args) {
		Node start = new Node("Company", "object_key", "A12327868DASDJ");
//		Node start = new Node("Person", "object_key", "Company/A12327868DASDJ");
		KExpandQuery kExpandQuery = new KExpandQuery();
		kExpandQuery.setStartVertices(Sets.newHashSet(start));
		kExpandQuery.setVertexLabels(new HashSet<>(Arrays.asList("Company", "Person")));
//		kExpandQuery.setVertexLabels(new HashSet<>(Arrays.asList("Person")));
		kExpandQuery.setEdgeLabels(new HashSet<>(Arrays.asList("loan_pay")));
		kExpandQuery.setMinDepth(1);
		kExpandQuery.setMaxDepth(1);
		kExpandQuery.setDirection(GraphQuery.DIRECTION_IN);
//		kExpandQuery.setOffset(0);
//		kExpandQuery.setSize(0);


//		Node start = new Node("Company", "object_key", "2");
//		KExpandQuery kExpandQuery = new KExpandQuery();
//		kExpandQuery.setStartVertices(Sets.newHashSet(start));
//		kExpandQuery.setVertexLabels(new HashSet<>(Collections.singletonList("Company")));
//		kExpandQuery.setEdgeLabels(new HashSet<>(Collections.singletonList("invest")));
//		kExpandQuery.setMinDepth(3);
//		kExpandQuery.setMaxDepth(3);
//		kExpandQuery.setDirection(GraphQuery.DIRECTION_IN);
//		FieldFilter fieldFilter = new FieldFilter();
//		fieldFilter.setLabel("personA");
//		fieldFilter.setValue("5");
//		fieldFilter.setFieldType(FieldType.STRING);
//		fieldFilter.setField("object_key");
//		fieldFilter.setLabelType(LabelType.VERTEX);
//		fieldFilter.setOperator(Operator.GT);
//		kExpandQuery.setFilters(fieldFilter);

		System.out.println(kExpandQuery.queryString());


		// 全路径查询
//		FullPathQuery fullPathQuery = new FullPathQuery();
//		fullPathQuery.setDirection(GraphQuery.DIRECTION_IN);
//		Node start = new Node("Company", "object_key", "2");
//		Node end = new Node("Company", "object_key", "1");
//
//		fullPathQuery.setStartVertices(Sets.newHashSet(start));
//		fullPathQuery.setEndVertices(Sets.newHashSet(end));
//		fullPathQuery.setVertexLabels(new HashSet<>(Arrays.asList("Company")));
//		fullPathQuery.setEdgeLabels(new HashSet<>(Arrays.asList("invest")));
//		fullPathQuery.setMaxDepth(3);
//		fullPathQuery.setMinDepth(3);
//		// 分页
//		fullPathQuery.setSize(10);
//		fullPathQuery.setOffset(1);
//		System.out.println(fullPathQuery.queryString());
//        GraphPathResult result = atlasGraphDatabase.graphQuery(fullPathQuery);
//        System.out.println(JsonUtils.toJsonString(result));

		Node start1 = new Node("Company", "object_key", "2");
		Node end1 = new Node("Company", "object_key", "1");
		ShortestPathQuery shortestPathQuery = new ShortestPathQuery();
		shortestPathQuery.setStartVertices(Sets.newHashSet(start1));
		shortestPathQuery.setEndVertices(Sets.newHashSet(end1));
		shortestPathQuery.setMaxDepth(3);
		shortestPathQuery.setMinDepth(1);
		shortestPathQuery.setDirection("IN");
		shortestPathQuery.setEdgeLabels(Sets.newHashSet("invest"));
		shortestPathQuery.setVertexLabels(Sets.newHashSet("Company"));
		System.out.println(shortestPathQuery.queryString());
	}
}
