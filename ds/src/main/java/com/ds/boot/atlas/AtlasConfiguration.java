package com.ds.boot.atlas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.haizhi.atlasgraph.AtlasGraph;
import com.haizhi.atlasgraph.AtlasGraphDatabase;
import com.haizhi.atlasgraph.AtlasGraphFactory;
import com.haizhi.atlasgraph.ConnectionConfiguration;

/**
 * @author ds
 * @date 2026/5/20
 * @description
 */
@Configuration
public class AtlasConfiguration {

	@Value("${iap.atlas.username: root}")
	private String username;

	@Value("${iap.atlas.password: 123456}")
	private String password;

	@Value("${iap.atlas.url: 127.0.0.1:8080}")
	private String url;

	@Value("${iap.atlas.db: glfx}")
	private String db;

	@Bean
	public AtlasGraph atlasGraph() {
		ConnectionConfiguration configuration = new ConnectionConfiguration(url, username, password.toCharArray());
		return AtlasGraphFactory.getInstance().newInstance(configuration);
	}

	@Bean
	public AtlasGraphDatabase atlasGraphDatabase(AtlasGraph atlasGraph) {
		return atlasGraph.openGraphDatabase(db);
	}

}
