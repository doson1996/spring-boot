package com.ds.boot.atlas.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AqlToCypherConverter {

	private static final Pattern FOR_PATTERN = Pattern.compile(
			"FOR\\s+(\\w+)\\s+IN\\s+(\\w+)", Pattern.CASE_INSENSITIVE
	);

	private static final Pattern FILTER_PATTERN = Pattern.compile(
			"FILTER\\s+(.+?)(?=\\s+(?:RETURN|SORT|LIMIT|FOR|LET|COLLECT|INSERT|UPDATE|REPLACE|REMOVE|UPSERT)|$)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
	);

	private static final Pattern RETURN_PATTERN = Pattern.compile(
			"RETURN\\s+(.+?)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
	);

	private static final Pattern SORT_PATTERN = Pattern.compile(
			"SORT\\s+(.+?)(?=\\s+(?:LIMIT|RETURN|FOR|FILTER)|$)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
	);

	private static final Pattern LIMIT_PATTERN = Pattern.compile(
			"LIMIT\\s+(\\d+)\\s*,?\\s*(\\d*)", Pattern.CASE_INSENSITIVE
	);

	private static final Pattern LET_PATTERN = Pattern.compile(
			"LET\\s+(\\w+)\\s*:=\\s*(.+?)(?=\\s+(?:FOR|FILTER|RETURN|SORT|LIMIT|LET)|$)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
	);

	public String convert(String aql) {
		if (aql == null || aql.trim().isEmpty()) {
			throw new IllegalArgumentException("AQL query cannot be empty");
		}

		String trimmedAql = aql.trim();

		Map<String, String> variables = new LinkedHashMap<>();
		List<String> filters = new ArrayList<>();
		String returnClause = null;
		String sortClause = null;
		String limitClause = null;
		List<String> letClauses = new ArrayList<>();

		extractLetClauses(trimmedAql, letClauses);
		extractForClauses(trimmedAql, variables);
		extractFilters(trimmedAql, filters);
		extractSort(trimmedAql, sortClause);
		extractLimit(trimmedAql, limitClause);
		extractReturn(trimmedAql, returnClause);

		StringBuilder cypher = new StringBuilder();

		convertLetClauses(letClauses, cypher);
		convertMatchClause(variables, cypher);
		convertWhereClause(filters, variables, cypher);
		convertOrderByClause(sortClause, variables, cypher);
		convertLimitClause(limitClause, cypher);
		convertReturnClause(returnClause, variables, cypher);

		return cypher.toString().trim();
	}

	private void extractLetClauses(String aql, List<String> letClauses) {
		Matcher matcher = LET_PATTERN.matcher(aql);
		while (matcher.find()) {
			letClauses.add(matcher.group());
		}
	}

	private void extractForClauses(String aql, Map<String, String> variables) {
		Matcher matcher = FOR_PATTERN.matcher(aql);
		while (matcher.find()) {
			String variable = matcher.group(1);
			String collection = matcher.group(2);
			variables.put(variable, collection);
		}
	}

	private void extractFilters(String aql, List<String> filters) {
		Matcher matcher = FILTER_PATTERN.matcher(aql);
		while (matcher.find()) {
			String filterCondition = matcher.group(1).trim();
			if (!filterCondition.isEmpty()) {
				filters.add(filterCondition);
			}
		}
	}

	private void extractSort(String aql, String sortClause) {
		Matcher matcher = SORT_PATTERN.matcher(aql);
		if (matcher.find()) {
			sortClause = matcher.group(1).trim();
		}
	}

	private void extractLimit(String aql, String limitClause) {
		Matcher matcher = LIMIT_PATTERN.matcher(aql);
		if (matcher.find()) {
			limitClause = matcher.group();
		}
	}

	private void extractReturn(String aql, String returnClause) {
		Matcher matcher = RETURN_PATTERN.matcher(aql);
		if (matcher.find()) {
			returnClause = matcher.group(1).trim();
		}
	}

	private void convertLetClauses(List<String> letClauses, StringBuilder cypher) {
		for (String letClause : letClauses) {
			Matcher matcher = LET_PATTERN.matcher(letClause);
			if (matcher.find()) {
				String variable = matcher.group(1);
				String expression = matcher.group(2).trim();

				String convertedExpr = convertExpression(expression, Collections.emptyMap());
				cypher.append("WITH ").append(convertedExpr).append(" AS ").append(variable).append("\n");
			}
		}
	}

	private void convertMatchClause(Map<String, String> variables, StringBuilder cypher) {
		if (variables.isEmpty()) {
			return;
		}

		List<String> matchPatterns = new ArrayList<>();
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String variable = entry.getKey();
			String collection = entry.getValue();

			String nodePattern = "(" + variable + ":" + collection + ")";
			matchPatterns.add(nodePattern);
		}

		cypher.append("MATCH ").append(String.join(", ", matchPatterns)).append("\n");
	}

	private void convertWhereClause(List<String> filters, Map<String, String> variables, StringBuilder cypher) {
		if (filters.isEmpty()) {
			return;
		}

		List<String> convertedFilters = new ArrayList<>();
		for (String filter : filters) {
			String converted = convertFilterCondition(filter, variables);
			convertedFilters.add(converted);
		}

		cypher.append("WHERE ").append(String.join(" AND ", convertedFilters)).append("\n");
	}

	private String convertFilterCondition(String condition, Map<String, String> variables) {
		String converted = condition;

		converted = converted.replaceAll("\\b(==)\\b", "=");
		converted = converted.replaceAll("\\b(!=)\\b", "<>");
		converted = converted.replaceAll("\\b&&\\b", "AND");
		converted = converted.replaceAll("\\b\\|\\|\\b", "OR");
		converted = converted.replaceAll("\\bNOT\\b", "NOT");

		converted = convertAttributeAccess(converted, variables);

		converted = convertFunctions(converted);

		return converted;
	}

	private String convertAttributeAccess(String condition, Map<String, String> variables) {
		Pattern pattern = Pattern.compile("(\\w+)\\.(\\w+)");
		Matcher matcher = pattern.matcher(condition);
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			String variable = matcher.group(1);
			String attribute = matcher.group(2);

			if (variables.containsKey(variable)) {
				String replacement = variable + "." + attribute;
				matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
			}
		}
		matcher.appendTail(result);

		return result.toString();
	}

	private String convertFunctions(String condition) {
		String converted = condition;
		converted = converted.replaceAll("\\bLIKE\\b", "CONTAINS");
		converted = converted.replaceAll("\\bIN\\b(?!\\s*\\()", "IN");
		converted = converted.replaceAll("\\bLENGTH\\(([^)]+)\\)", "size($1)");
		converted = converted.replaceAll("\\bTO_LOWER\\(([^)]+)\\)", "toLower($1)");
		converted = converted.replaceAll("\\bTO_UPPER\\(([^)]+)\\)", "toUpper($1)");
		converted = converted.replaceAll("\\bSUBSTRING\\(([^,]+),\\s*([^,]+),\\s*([^)]+)\\)",
				"substring($1, $2, $3)");
		converted = converted.replaceAll("\\bCONCAT\\(([^)]+)\\)", "$1");
		return converted;
	}

	private void convertOrderByClause(String sortClause, Map<String, String> variables, StringBuilder cypher) {
		if (sortClause == null || sortClause.isEmpty()) {
			return;
		}

		String[] sortParts = sortClause.split(",");
		List<String> orderItems = new ArrayList<>();

		for (String part : sortParts) {
			part = part.trim();
			String[] tokens = part.split("\\s+");

			String field = tokens[0];
			String direction = "ASC";

			if (tokens.length > 1) {
				String dir = tokens[1].toUpperCase();
				if ("DESC".equals(dir)) {
					direction = "DESC";
				}
			}

			String convertedField = convertAttributeAccess(field, variables);
			orderItems.add(convertedField + " " + direction);
		}

		cypher.append("ORDER BY ").append(String.join(", ", orderItems)).append("\n");
	}

	private void convertLimitClause(String limitClause, StringBuilder cypher) {
		if (limitClause == null || limitClause.isEmpty()) {
			return;
		}

		Matcher matcher = LIMIT_PATTERN.matcher(limitClause);
		if (matcher.find()) {
			String offset = matcher.group(1);
			String count = matcher.group(2);

			if (count != null && !count.isEmpty()) {
				cypher.append("SKIP ").append(offset).append(" LIMIT ").append(count).append("\n");
			} else {
				cypher.append("LIMIT ").append(offset).append("\n");
			}
		}
	}

	private void convertReturnClause(String returnClause, Map<String, String> variables, StringBuilder cypher) {
		if (returnClause == null || returnClause.isEmpty()) {
			return;
		}

		if ("true".equalsIgnoreCase(returnClause) || "false".equalsIgnoreCase(returnClause)) {
			cypher.append("RETURN ").append(returnClause.toLowerCase());
			return;
		}

		if (returnClause.matches("\\d+")) {
			cypher.append("RETURN ").append(returnClause);
			return;
		}

		String converted = convertExpression(returnClause, variables);
		cypher.append("RETURN ").append(converted);
	}

	private String convertExpression(String expression, Map<String, String> variables) {
		String converted = expression;

		if (converted.startsWith("[") && converted.endsWith("]")) {
			String inner = converted.substring(1, converted.length() - 1).trim();
			String convertedInner = convertExpression(inner, variables);
			return "collect(" + convertedInner + ")";
		}

		if (converted.startsWith("{") && converted.endsWith("}")) {
			return convertObjectLiteral(converted, variables);
		}

		converted = convertAttributeAccess(converted, variables);
		converted = convertFunctions(converted);

		return converted;
	}

	private String convertObjectLiteral(String objectLiteral, Map<String, String> variables) {
		String content = objectLiteral.substring(1, objectLiteral.length() - 1).trim();

		if (content.matches("\\w+")) {
			String converted = convertAttributeAccess(content, variables);
			return converted;
		}

		StringBuilder result = new StringBuilder("{");
		String[] pairs = content.split(",");
		List<String> convertedPairs = new ArrayList<>();

		for (String pair : pairs) {
			pair = pair.trim();
			String[] keyValue = pair.split(":", 2);
			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();

				String convertedKey = key.startsWith("\"") || key.startsWith("'") ? key : "\"" + key + "\"";
				String convertedValue = convertExpression(value, variables);

				convertedPairs.add(convertedKey + ": " + convertedValue);
			}
		}

		result.append(String.join(", ", convertedPairs));
		result.append("}");

		return result.toString();
	}

	public static void main(String[] args) {
		AqlToCypherConverter converter = new AqlToCypherConverter();

		String aql1 = "FOR user IN users FILTER user.age > 18 RETURN user";
		System.out.println("AQL: " + aql1);
		System.out.println("Cypher: " + converter.convert(aql1));
		System.out.println();

		String aql2 = "FOR u IN users FILTER u.age >= 18 && u.status == 'active' SORT u.name DESC LIMIT 10 RETURN u.name";
		System.out.println("AQL: " + aql2);
		System.out.println("Cypher: " + converter.convert(aql2));
		System.out.println();

		String aql3 = "FOR user IN users FILTER user.city == 'Beijing' RETURN {name: user.name, age: user.age}";
		System.out.println("AQL: " + aql3);
		System.out.println("Cypher: " + converter.convert(aql3));
	}
}
