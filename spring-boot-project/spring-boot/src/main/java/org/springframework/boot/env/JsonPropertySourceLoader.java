package org.springframework.boot.env;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ds
 * @date 2025/3/26
 * @description
 */
public class JsonPropertySourceLoader implements PropertySourceLoader {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String[] getFileExtensions() {
		return new String[]{"json"};
	}

	@Override
	public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
		Map<String, Object> properties = objectMapper.readValue(resource.getInputStream(), Map.class);
		MapPropertySource propertySource = new MapPropertySource(name, properties);
		return Collections.singletonList(propertySource);
	}

}
