package com.ds.boot.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shaded.atlas.sdk.com.google.common.collect.Sets;

import com.haizhi.atlasgraph.AtlasGraph;
import com.haizhi.atlasgraph.AtlasGraphDatabase;
import com.haizhi.atlasgraph.AtlasGraphFactory;
import com.haizhi.atlasgraph.ConnectionConfiguration;
import com.haizhi.atlasgraph.constant.FieldType;
import com.haizhi.atlasgraph.model.graph.Edge;
import com.haizhi.atlasgraph.model.graph.Vertex;
import com.haizhi.atlasgraph.model.query.FieldFilter;
import com.haizhi.atlasgraph.model.query.LabelType;
import com.haizhi.atlasgraph.model.query.Node;
import com.haizhi.atlasgraph.model.query.Operator;
import com.haizhi.atlasgraph.model.query.graph.FullPathQuery;
import com.haizhi.atlasgraph.model.query.graph.GraphQuery;
import com.haizhi.atlasgraph.model.query.property.EdgePropertyQuery;
import com.haizhi.atlasgraph.model.query.property.VertexPropertyQuery;
import com.haizhi.atlasgraph.model.result.graph.GraphPathResult;
import com.haizhi.atlasgraph.util.JsonUtils;

/**
 * @author ds
 * @date 2026/3/25
 * @description
 */
public class Demo {
	public static void main(String[] args) {
		String username = "username";
		String password = "password";
		String url = "ip:port";
		ConnectionConfiguration configuration = new ConnectionConfiguration(url, username, password.toCharArray());
		AtlasGraph atlasGraph = AtlasGraphFactory.getInstance().newInstance(configuration);
		AtlasGraphDatabase atlasGraphDatabase = atlasGraph.openGraphDatabase("test");
        // 点查询
        VertexPropertyQuery vertexQuery = new VertexPropertyQuery();
        vertexQuery.setLabel("Company");
        vertexQuery.setProperty("name");
        vertexQuery.setValues(Arrays.asList("重庆测试技术有限公司"));

        Set<Vertex> vertices = atlasGraphDatabase.vertexPropertyQuery(vertexQuery);
        System.out.println(JsonUtils.toJsonString(vertices));

        // 带条件点查询
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setLabel("personA");
        fieldFilter.setValue("5");
        fieldFilter.setFieldType(FieldType.STRING);
        fieldFilter.setField("object_key");
        fieldFilter.setLabelType(LabelType.VERTEX);
        fieldFilter.setOperator(Operator.GT);
        vertexQuery.setFilters(fieldFilter);

        // 边查询
        EdgePropertyQuery edgePropertyQuery = new EdgePropertyQuery();
        edgePropertyQuery.setLabel("sale");
        edgePropertyQuery.setProperty("object_key");
        edgePropertyQuery.setValues(Arrays.asList("4", "5", "6", "7", "8"));
        List<EdgePropertyQuery> queries = new ArrayList<>();
        queries.add(edgePropertyQuery);
        Set<Edge> edges = atlasGraphDatabase.edgesQuery(queries);
        System.out.println(JsonUtils.toJsonString(edges));


        // 图查询
        FullPathQuery fullPathQuery = new FullPathQuery();
        fullPathQuery.setDirection(GraphQuery.DIRECTION_OUT);
        Node a = new Node("book", "object_key", "3");
        Node b = new Node("personA", "object_key", "4");
        fullPathQuery.setStartVertices(Sets.newHashSet(a, b));
        fullPathQuery.setEndVertices(Sets.newHashSet(a, b));
        fullPathQuery.setVertexLabels(new HashSet<>(Arrays.asList("book", "personA")));
        fullPathQuery.setEdgeLabels(new HashSet<>(Arrays.asList("sale")));
        System.out.println(fullPathQuery.queryString());
        GraphPathResult result = atlasGraphDatabase.graphQuery(fullPathQuery);
        System.out.println(JsonUtils.toJsonString(result));

	}
}
