package com.ds.boot.atlas;

import com.haizhi.atlasgraph.AtlasGraph;
import com.haizhi.atlasgraph.AtlasGraphDatabase;
import com.haizhi.atlasgraph.AtlasGraphFactory;
import com.haizhi.atlasgraph.ConnectionConfiguration;

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
		AtlasGraphDatabase test = atlasGraph.openGraphDatabase("test");
	}
}
