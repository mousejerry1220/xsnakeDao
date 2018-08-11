package org.xsnake.cloud.dao;

public interface Dialect {

	String getPageSql(String sql, int start, int end);

	String getTestSql();
}
