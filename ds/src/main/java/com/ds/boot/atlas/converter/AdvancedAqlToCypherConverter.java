package com.ds.boot.atlas.converter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedAqlToCypherConverter {

    private final Map<String, String> variableMappings = new HashMap<>();
    private final List<String> withClauses = new ArrayList<>();
    private final List<String> matchClauses = new ArrayList<>();
    private final List<String> whereClauses = new ArrayList<>();
    private final List<String> unwindClauses = new ArrayList<>();

    public String convert(String aql) {
        reset();
        
        if (aql == null || aql.trim().isEmpty()) {
            throw new IllegalArgumentException("AQL query cannot be empty");
        }
        String normalizedAql = normalizeAql(aql);
        parseAndConvert(normalizedAql);
        return buildCypher();
    }

    private void reset() {
        variableMappings.clear();
        withClauses.clear();
        matchClauses.clear();
        whereClauses.clear();
        unwindClauses.clear();
    }

    private String normalizeAql(String aql) {
        return aql.replaceAll("\\s+", " ").trim();
    }

    private void parseAndConvert(String aql) {
        if (aql.toUpperCase().startsWith("INSERT") || 
            aql.toUpperCase().startsWith("UPDATE") ||
            aql.toUpperCase().startsWith("REPLACE") ||
            aql.toUpperCase().startsWith("REMOVE") ||
            aql.toUpperCase().startsWith("UPSERT")) {
            convertMutationQuery(aql);
        } else {
            convertSelectQuery(aql);
        }
    }

    private void convertSelectQuery(String aql) {
        extractLetStatements(aql);
        extractForLoops(aql);
        extractUnwindOperations(aql);
        extractFilters(aql);
        extractCollectOperations(aql);
        extractSortAndLimit(aql);
        extractReturnStatement(aql);
    }

    private void convertMutationQuery(String aql) {
        if (aql.toUpperCase().startsWith("INSERT")) {
            convertInsertQuery(aql);
        } else if (aql.toUpperCase().startsWith("UPDATE")) {
            convertUpdateQuery(aql);
        } else if (aql.toUpperCase().startsWith("REPLACE")) {
            convertReplaceQuery(aql);
        } else if (aql.toUpperCase().startsWith("REMOVE")) {
            convertRemoveQuery(aql);
        }
    }

    private void extractLetStatements(String aql) {
        Pattern pattern = Pattern.compile("LET\\s+(\\w+)\\s*:=\\s*(.+?)(?=\\s+(?:FOR|FILTER|RETURN|SORT|LIMIT|LET|COLLECT)|$)", 
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String expression = matcher.group(2).trim();
            
            String convertedExpr = convertSubquery(expression);
            withClauses.add(convertedExpr + " AS " + varName);
        }
    }

    private void extractForLoops(String aql) {
        Pattern pattern = Pattern.compile("FOR\\s+(\\w+)\\s+IN\\s+(?:OUTBOUND|INBOUND|ANY)?\\s*(.+?)", 
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String source = matcher.group(2).trim();
            
            if (source.toUpperCase().contains("EDGES") || source.toUpperCase().contains("TRAVERSAL")) {
                convertTraversalLoop(varName, source);
            } else {
                variableMappings.put(varName, source);
                matchClauses.add("(" + varName + ":" + source + ")");
            }
        }
    }

    private void extractUnwindOperations(String aql) {
        Pattern pattern = Pattern.compile("FOR\\s+(\\w+)\\s+IN\\s+(\\w+)\\.([\\w\\[\\]]+)", 
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        while (matcher.find()) {
            String itemVar = matcher.group(1);
            String arrayOwner = matcher.group(2);
            String arrayPath = matcher.group(3);
            
            unwindClauses.add(arrayOwner + "." + arrayPath + " AS " + itemVar);
        }
    }

    private void extractFilters(String aql) {
        Pattern pattern = Pattern.compile("FILTER\\s+(.+?)(?=\\s+(?:RETURN|SORT|LIMIT|FOR|LET|COLLECT|INSERT|UPDATE|REPLACE|REMOVE|UPSERT)|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(aql);
        
        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String convertedCondition = convertCondition(condition);
            whereClauses.add(convertedCondition);
        }
    }

    private void extractCollectOperations(String aql) {
        Pattern pattern = Pattern.compile("COLLECT\\s+(.+?)(?=\\s+(?:INTO|WITH|RETURN|SORT|LIMIT)|$)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        if (matcher.find()) {
            String collectClause = matcher.group(1).trim();
            convertCollectClause(collectClause);
        }
    }

    private void extractSortAndLimit(String aql) {
        Pattern sortPattern = Pattern.compile("SORT\\s+(.+?)(?=\\s+(?:LIMIT|RETURN|FOR|FILTER|COLLECT)|$)",
            Pattern.CASE_INSENSITIVE);
        Matcher sortMatcher = sortPattern.matcher(aql);
        
        if (sortMatcher.find()) {
            String sortClause = sortMatcher.group(1).trim();
            String convertedSort = convertSortClause(sortClause);
            withClauses.add("ORDER BY " + convertedSort);
        }

        Pattern limitPattern = Pattern.compile("LIMIT\\s+(\\d+)\\s*,?\\s*(\\d*)", Pattern.CASE_INSENSITIVE);
        Matcher limitMatcher = limitPattern.matcher(aql);
        
        if (limitMatcher.find()) {
            String offset = limitMatcher.group(1);
            String count = limitMatcher.group(2);
            
            if (count != null && !count.isEmpty()) {
                withClauses.add("SKIP " + offset + " LIMIT " + count);
            } else {
                withClauses.add("LIMIT " + offset);
            }
        }
    }

    private void extractReturnStatement(String aql) {
        Pattern pattern = Pattern.compile("RETURN\\s+(DISTINCT\\s+)?(.+?)$", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(aql);
        
        if (matcher.find()) {
            boolean distinct = matcher.group(1) != null;
            String returnExpr = matcher.group(2).trim();
            
            String convertedReturn = convertReturnExpression(returnExpr);
            
            StringBuilder returnClause = new StringBuilder("RETURN ");
            if (distinct) {
                returnClause.append("DISTINCT ");
            }
            returnClause.append(convertedReturn);
            
            withClauses.add(returnClause.toString());
        }
    }

    private String convertCondition(String condition) {
        String converted = condition;

        converted = converted.replaceAll("\\b==\\b", "=");
        converted = converted.replaceAll("\\b!=\\b", "<>");
        converted = converted.replaceAll("\\b&&\\b", "AND");
        converted = converted.replaceAll("\\b\\|\\|\\b", "OR");
        converted = converted.replaceAll("\\b!\\b", "NOT ");

        converted = convertInOperator(converted);
        converted = convertLikeOperator(converted);
        converted = convertNullChecks(converted);
        converted = convertArrayAccess(converted);
        converted = convertFunctions(converted);
        converted = convertAttributeAccess(converted);

        return converted;
    }

    private String convertInOperator(String condition) {
        Pattern pattern = Pattern.compile("(\\w+(?:\\.\\w+)*)\\s+IN\\s+\\[(.*?)\\]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(condition);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String field = matcher.group(1);
            String values = matcher.group(2);
            
            String[] valueArray = values.split(",");
            List<String> conditions = new ArrayList<>();
            for (String value : valueArray) {
                conditions.add(field + " = " + value.trim());
            }
            
            String replacement = "(" + String.join(" OR ", conditions) + ")";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String convertLikeOperator(String condition) {
        Pattern pattern = Pattern.compile("(\\w+(?:\\.\\w+)*)\\s+LIKE\\s+'([^']*?)'", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(condition);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String field = matcher.group(1);
            String p = matcher.group(2);
            
            String regex = p.replace("%", ".*");
            String replacement = field + " =~ \".*" + regex + ".*\"";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String convertNullChecks(String condition) {
        condition = condition.replaceAll("(\\w+(?:\\.\\w+)*)\\s+==\\s+null", "$1 IS NULL");
        condition = condition.replaceAll("null\\s+==\\s+(\\w+(?:\\.\\w+)*)", "$1 IS NULL");
        condition = condition.replaceAll("(\\w+(?:\\.\\w+)*)\\s+!=\\s+null", "$1 IS NOT NULL");
        condition = condition.replaceAll("null\\s+!=\\s+(\\w+(?:\\.\\w+)*)", "$1 IS NOT NULL");
        
        return condition;
    }

    private String convertArrayAccess(String condition) {
        Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(condition);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String array = matcher.group(1);
            String index = matcher.group(2);
            String replacement = array + "[" + index + "]";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String convertFunctions(String condition) {
        String converted = condition;

        converted = converted.replaceAll("\\bLENGTH\\(([^)]+)\\)", "size($1)");
        converted = converted.replaceAll("\\bTO_LOWER\\(([^)]+)\\)", "toLower($1)");
        converted = converted.replaceAll("\\bTO_UPPER\\(([^)]+)\\)", "toUpper($1)");
        converted = converted.replaceAll("\\bSUBSTRING\\(([^,]+),\\s*([^,]+),\\s*([^)]+)\\)", 
            "substring($1, $2, $3)");
        converted = converted.replaceAll("\\bCONCAT\\(([^)]+)\\)", "$1");
        converted = converted.replaceAll("\\bNOW\\(\\)", "timestamp()");
        converted = converted.replaceAll("\\bDATE_NOW\\(\\)", "datetime()");
        converted = converted.replaceAll("\\bCOUNT\\(([^)]+)\\)", "count($1)");
        converted = converted.replaceAll("\\bMIN\\(([^)]+)\\)", "min($1)");
        converted = converted.replaceAll("\\bMAX\\(([^)]+)\\)", "max($1)");
        converted = converted.replaceAll("\\bAVG\\(([^)]+)\\)", "avg($1)");
        converted = converted.replaceAll("\\bSUM\\(([^)]+)\\)", "sum($1)");

        return converted;
    }

    private String convertAttributeAccess(String condition) {
        Pattern pattern = Pattern.compile("(\\w+)\\.(\\w+)");
        Matcher matcher = pattern.matcher(condition);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variable = matcher.group(1);
            String attribute = matcher.group(2);

            if (variableMappings.containsKey(variable)) {
                String replacement = variable + "." + attribute;
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String convertSortClause(String sortClause) {
        String[] parts = sortClause.split(",");
        List<String> convertedParts = new ArrayList<>();
        
        for (String part : parts) {
            part = part.trim();
            String[] tokens = part.split("\\s+");
            
            String field = convertAttributeAccess(tokens[0]);
            String direction = tokens.length > 1 && tokens[1].toUpperCase().equals("DESC") ? "DESC" : "ASC";
            
            convertedParts.add(field + " " + direction);
        }
        
        return String.join(", ", convertedParts);
    }

    private String convertReturnExpression(String expression) {
        if (expression.equalsIgnoreCase("true") || expression.equalsIgnoreCase("false")) {
            return expression.toLowerCase();
        }

        if (expression.matches("\\d+\\.?\\d*")) {
            return expression;
        }

        if (expression.startsWith("[") && expression.endsWith("]")) {
            String inner = expression.substring(1, expression.length() - 1).trim();
            return "collect(" + convertReturnExpression(inner) + ")";
        }

        if (expression.startsWith("{") && expression.endsWith("}")) {
            return convertObjectLiteral(expression);
        }

        return convertAttributeAccess(expression);
    }

    private String convertObjectLiteral(String objectLiteral) {
        String content = objectLiteral.substring(1, objectLiteral.length() - 1).trim();
        
        if (!content.contains(":")) {
            return convertAttributeAccess(content);
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

                String formattedKey = key.startsWith("\"") || key.startsWith("'") ? key : "\"" + key + "\"";
                String convertedValue = convertReturnExpression(value);

                convertedPairs.add(formattedKey + ": " + convertedValue);
            }
        }

        result.append(String.join(", ", convertedPairs));
        result.append("}");

        return result.toString();
    }

    private String convertSubquery(String expression) {
        if (expression.toUpperCase().startsWith("FOR")) {
            return convertNestedQuery(expression);
        }
        
        return convertReturnExpression(expression);
    }

    private String convertNestedQuery(String subquery) {
        AdvancedAqlToCypherConverter nestedConverter = new AdvancedAqlToCypherConverter();
        return nestedConverter.convert(subquery);
    }

    private void convertTraversalLoop(String varName, String source) {
        Pattern pattern = Pattern.compile("(OUTBOUND|INBOUND|ANY)\\s+(.+?)\\s+(\\w+)\\s+(\\w+)", 
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(source);
        
        if (matcher.find()) {
            String direction = matcher.group(1).toUpperCase();
            String startNode = matcher.group(2).trim();
            String edgeCollection = matcher.group(3);
            String targetCollection = matcher.group(4);
            
            String relationship = edgeCollection.toLowerCase();
            String cypherDirection = "";
            
            switch (direction) {
                case "OUTBOUND":
                    cypherDirection = "->";
                    break;
                case "INBOUND":
                    cypherDirection = "<-";
                    break;
                case "ANY":
                    cypherDirection = "-";
                    break;
            }
            
            String matchPattern = "(" + startNode + ")-[" + varName + ":" + relationship + "]" + 
                                cypherDirection + "(" + varName + "_target:" + targetCollection + ")";
            matchClauses.add(matchPattern);
        }
    }

    private void convertCollectClause(String collectClause) {
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(collectClause);
        
        List<String> groupByFields = new ArrayList<>();
        while (matcher.find()) {
            String alias = matcher.group(1);
            String field = matcher.group(2);
            groupByFields.add(field);
        }
        
        if (!groupByFields.isEmpty()) {
            String withClause = "WITH " + String.join(", ", groupByFields);
            withClauses.add(withClause);
        }
    }

    private void convertInsertQuery(String aql) {
        Pattern pattern = Pattern.compile("INSERT\\s+(.+?)\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        if (matcher.find()) {
            String document = matcher.group(1).trim();
            String collection = matcher.group(2);
            
            String convertedDoc = convertObjectLiteral(document.startsWith("{") ? document : "{" + document + "}");
            matchClauses.add("CREATE (:" + collection + " " + convertedDoc + ")");
        }
    }

    private void convertUpdateQuery(String aql) {
        Pattern pattern = Pattern.compile("UPDATE\\s+(.+?)\\s+WITH\\s+(.+?)\\s+IN\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        if (matcher.find()) {
            String key = matcher.group(1).trim();
            String updates = matcher.group(2).trim();
            String collection = matcher.group(3);
            
            matchClauses.add("MATCH (node:" + collection + " {id: " + key + "})");
            String convertedUpdates = convertObjectLiteral(updates.startsWith("{") ? updates : "{" + updates + "}");
            withClauses.add("SET node += " + convertedUpdates);
            withClauses.add("RETURN node");
        }
    }

    private void convertReplaceQuery(String aql) {
        Pattern pattern = Pattern.compile("REPLACE\\s+(.+?)\\s+WITH\\s+(.+?)\\s+IN\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        if (matcher.find()) {
            String key = matcher.group(1).trim();
            String document = matcher.group(2).trim();
            String collection = matcher.group(3);
            
            matchClauses.add("MATCH (node:" + collection + " {id: " + key + "})");
            String convertedDoc = convertObjectLiteral(document.startsWith("{") ? document : "{" + document + "}");
            withClauses.add("SET node = " + convertedDoc);
            withClauses.add("RETURN node");
        }
    }

    private void convertRemoveQuery(String aql) {
        Pattern pattern = Pattern.compile("REMOVE\\s+(.+?)\\s+IN\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aql);
        
        if (matcher.find()) {
            String key = matcher.group(1).trim();
            String collection = matcher.group(2);
            
            matchClauses.add("MATCH (node:" + collection + " {id: " + key + "})");
            withClauses.add("DELETE node");
        }
    }

    private String buildCypher() {
        StringBuilder cypher = new StringBuilder();

        if (!unwindClauses.isEmpty()) {
            for (String unwind : unwindClauses) {
                cypher.append("UNWIND ").append(unwind).append("\n");
            }
        }

        if (!matchClauses.isEmpty()) {
            cypher.append("MATCH ").append(String.join(", ", matchClauses)).append("\n");
        }

        if (!withClauses.isEmpty() && !matchClauses.isEmpty()) {
            cypher.append("WITH *").append("\n");
        }

        if (!whereClauses.isEmpty()) {
            cypher.append("WHERE ").append(String.join(" AND ", whereClauses)).append("\n");
        }

        for (String clause : withClauses) {
            if (clause.startsWith("ORDER BY") || clause.startsWith("SKIP") || 
                clause.startsWith("LIMIT") || clause.startsWith("SET") || 
                clause.startsWith("DELETE") || clause.startsWith("RETURN")) {
                cypher.append(clause).append("\n");
            }
        }

        return cypher.toString().trim();
    }

    public static void main(String[] args) {
        AdvancedAqlToCypherConverter converter = new AdvancedAqlToCypherConverter();

        String[] testQueries = {
				"FOR user IN users FILTER user.age > 18 RETURN user",
				"FOR u IN users FILTER u.age >= 18 && u.status == 'active' SORT u.name DESC LIMIT 10 RETURN u.name",
				"FOR user IN users FILTER user.city == 'Beijing' RETURN {name: user.name, age: user.age}",
				"FOR user IN users FILTER user.tags IN ['developer', 'manager'] RETURN user",
				"FOR user IN users FILTER user.name LIKE '%john%' RETURN user",
				"INSERT {name: 'John', age: 30} INTO users",
				"UPDATE user WITH {age: 31} IN users",
				"FOR user IN users COLLECT city = user.city RETURN city",
				"FOR user IN users COLLECT city = user.city WITH COUNT INTO count RETURN {city: city, count: count}",
				"FOR user IN users COLLECT city = user.city AGGREGATE avgAge = AVG(user.age), maxAge = MAX(user.age) RETURN {city: city, avgAge: avgAge, maxAge: maxAge}",
				"FOR user IN users COLLECT INTO usersList RETURN usersList"
        };

        for (String aql : testQueries) {
            System.out.println("AQL: " + aql);
            System.out.println("Cypher: " + converter.convert(aql));
            System.out.println("---");
        }
    }
}
