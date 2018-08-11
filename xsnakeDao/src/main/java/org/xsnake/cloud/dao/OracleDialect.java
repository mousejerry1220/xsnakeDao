package org.xsnake.cloud.dao;

public class OracleDialect implements Dialect{

	@Override
	public String getPageSql(String sql, int start, int end) {
        StringBuffer oracleSql = new StringBuffer();
        oracleSql.append("SELECT * FROM  ( SELECT A.*, ROWNUM RN FROM ( ")
                 .append(sql)
                 .append(" ) A WHERE ROWNUM <= ")
                 .append(end)
                 .append(" ) WHERE RN > ")
                 .append(start);
        return oracleSql.toString();
	}

	@Override
	public String getTestSql() {
		return " select 'Oracle connection OK!' from dual";
	}
	
}
