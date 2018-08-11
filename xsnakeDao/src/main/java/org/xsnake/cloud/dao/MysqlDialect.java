package org.xsnake.cloud.dao;

public class MysqlDialect implements Dialect {

	@Override
	public String getPageSql(String sql, int start, int end) {
		StringBuffer mySql = new StringBuffer();
		mySql.append("SELECT * FROM  (  ").append(sql).append(" ) _A LIMIT ").append(start).append(" , ").append(end);
		return mySql.toString();
	}

	@Override
	public String getTestSql() {
		return "select 'mysql connection OK'";
	}

}
