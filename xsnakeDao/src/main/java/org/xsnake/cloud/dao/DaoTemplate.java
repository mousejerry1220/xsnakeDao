package org.xsnake.cloud.dao;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * 
 * @author Jerry.Zhao
 * 
 * 查询的方式：
 *   queryObject            :  查询返回单条记录，没有找到数据或者返回多个数据都报错
 *   queryObjectIgnoreError :  查询返回单条记录，没有查询到数据则返回null，返回多个结果的错误不处理
 *   queryList              :  查询返回List集合，可以是0到多条记录
 *   queryPage              :  查询返回分页对象
 * 查询的返回值：
 *   查询得到结果集后可以把结果集封装为客户端想得到的类型，主要分为三种类型：
 *   1、如果查询只得到单个列，所有支持的类见：isSingleColumnClass方法,该方法参照：
 *   org.springframework.jdbc.support.JdbcUtils.getResultSetValue(ResultSet rs, int index, Class<?> requiredType)
 *   2、用户可以根据返回的列名，创建自定义类。
 *   3、返回一个键值对的Map对象存放单条数据，一般如果泛型的Class<T>未传值，默认为第三种Map的方式
 * 
 *
 */
@Configuration
public class DaoTemplate {

	private static final Log logger = LogFactory.getLog(DaoTemplate.class);

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	Dialect dialect;

	public int execute(String sql) {
		return execute(sql,null);
	}

	public int execute(String sql, Object[] args) {
		if(logger.isDebugEnabled()){
			logger.debug("执行的SQL语句 ：[ " + sql + "] 参数为： [" + args == null ? "null" : Arrays.toString(args) + "] " );
		}
		return jdbcTemplate.update(sql, args);
	}
	
	public int[] batchExecute(String sql,List<Object[]> args) {
		if(logger.isDebugEnabled()){
			logger.debug("批量执行的SQL语句 ：[ " + sql + "] ");
		}
		return jdbcTemplate.batchUpdate(sql,args);
	}

	public List<Object> executeCall(final String sql, final ProcedureParam[] args) {
		CallableStatementCallback<List<Object>> action = new CallableStatementCallback<List<Object>>() {
			@Override
			public List<Object> doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
				final List<Object> result = new ArrayList<Object>();
				for(int i=1;i<args.length+1;i++){
					ProcedureParam p = args[(i-1)];
					if(p instanceof InProcedureParam){
						cs.setObject(i, p.value);
					}else if(p instanceof OutProcedureParam){
						cs.registerOutParameter(i, p.type);
					}
				}
				cs.execute();
				for(int i=1;i<args.length+1;i++){
					ProcedureParam p = args[(i-1)];
					if(p instanceof OutProcedureParam){
						result.add(cs.getString(i));
					}else{
						result.add(null);
					}
				}
				return result;
			}
		};
		return jdbcTemplate.execute(sql,action);
	}
	
	public static abstract class ProcedureParam{
		public ProcedureParam (int type,Object value){
			this.type = type;
			this.value = value;
		}
		int type;
		Object value;
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	}
	
	public static class InProcedureParam extends ProcedureParam{
		public InProcedureParam(Object value){
			super(-1,value);
		}
	}
	
	public static class OutProcedureParam extends ProcedureParam{
		public OutProcedureParam(int type){
			super(type,null);
		}
	}

	private String getPageSQL(String sql, int start, int end){
		return dialect.getPageSql(sql, start, end);
	}
	
	public <T> List<T> queryList(String sql,Class<T> clazz){
		return queryListForClass(sql,null,clazz);
	}
	
	public List<Map<String, Object>> queryList(String sql){
		return queryListForMap(sql,null);
	}
	
	public <T> List<T> queryList(String sql, Object[] args , Class<T> clazz){
		return queryListForClass(sql,args,clazz);
	}
	
	public List<Map<String, Object>> queryList(String sql, Object[] args){
		return queryListForMap(sql,args);
	}
	
	public <T> List<T> queryList(String sql,int firstResult, int maxResults,Class<T> clazz){
		 return queryList(sql,null,firstResult,maxResults,clazz);
	}
	
	public List<Map<String, Object>> queryList(String sql,int firstResult, int maxResults){
		return queryList(sql,null,firstResult,maxResults);
	}

	public <T> List<T> queryList(String sql, Object[] args, int firstResult, int maxResults,Class<T> clazz) {
		return queryListForClass(getPageSQL(sql, firstResult, firstResult + maxResults),args,clazz);
	}
	
	public List<Map<String, Object>> queryList(String sql, Object[] args, int firstResult, int maxResults) {
		return queryListForMap(getPageSQL(sql, firstResult, firstResult + maxResults),args);
	}
	
	public Map<String,Object> queryMap(String sql,Object[] args){
		if(logger.isDebugEnabled()){
			logger.debug("查询的SQL语句 ：[ " + sql + "] 参数为： [" + Arrays.toString(args) + "]");
		}
		return jdbcTemplate.queryForObject(sql, args, new ColumnMapRowMapper());
	}
	
	public Map<String,Object> queryMapIgnoreError(String sql){
		return queryMapIgnoreError(sql,null);	
	}
	
	public Map<String,Object> queryMapIgnoreError(String sql,Object[] args){
		try{
			return queryMap(sql, args);
		}catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public <T> T queryObjectIgnoreError(String sql,Object[] args,Class<T> clazz){
		try{
			return queryObject(sql, args,clazz);
		}catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public <T> T queryObjectIgnoreError(String sql,Class<T> clazz){
		return queryObjectIgnoreError(sql, null, clazz);
	}
	
	public <T> T queryObject(String sql,Object[] args,Class<T> clazz){
		if(logger.isDebugEnabled()){
			logger.debug("查询的SQL语句 ：[ " + sql + "] 参数为： [" + Arrays.toString(args) + "] 转换的类： [" + clazz.getName() + "]");
		}
		RowMapper<T> rowMapper = null;
		if (String.class == clazz || Number.class.isAssignableFrom(clazz)) {
			rowMapper = new SingleColumnRowMapper<T>(clazz);
		}else{
			rowMapper = new BeanPropertyRowMapper<T>(clazz);
		}
		return jdbcTemplate.queryForObject(sql, args, rowMapper);
	}
	
	public <T> T queryObject(String sql,Class<T> clazz){
		return queryObject(sql, null, clazz); 
	}
	
	public Map<String,Object> queryMap(String sql){
		return queryMap(sql, null);
	}
	
	public Page<Map<String,Object>> queryPage(String sql, Object[] args, int currentPage ,int pageSize) {
		try {
			String thql = "select count(1) from (" + sql +") t ";
			BigDecimal _count = jdbcTemplate.queryForObject(thql, args, BigDecimal.class);
			int count = _count.intValue();
			List<Map<String,Object>> results = queryList(sql, args, (currentPage - 1) * pageSize, pageSize);
			return new Page<Map<String,Object>>(results, currentPage, pageSize, count);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public <T> Page<T> queryPage(String sql, Object[] args, int currentPage ,int pageSize,Class<T> clazz) {
		try {
			String thql = "select count(1) from (" + sql +") t ";
			BigDecimal _count = jdbcTemplate.queryForObject(thql, args, BigDecimal.class);
			int count = _count.intValue();
			List<T> results = queryList(sql, args, (currentPage - 1) * pageSize, pageSize,clazz);
			return new Page<T>(results, currentPage, pageSize, count);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<Map<String, Object>> queryListForMap(String sql,Object[] args) {
		if(logger.isDebugEnabled()){
			logger.debug("查询的SQL语句 ：[ " + sql + "] 参数为： [" + Arrays.toString(args) + "]");
		}
		return jdbcTemplate.queryForList(sql, args);
	}
	
	private <T> List<T> queryListForClass(String sql,Object[] args,Class<T> clazz) {
		if(logger.isDebugEnabled()){
			logger.debug("查询的SQL语句 ：[ " + sql + "] 参数为： [" + Arrays.toString(args) + "] 转换的类： [" + clazz.getName() + "]");
		}
		if (isSingleColumnClass(clazz)) {
			return jdbcTemplate.query(sql, new ArgumentPreparedStatementSetter(args), new SingleColumnRowMapper<T>(clazz));	
		}else{
			return jdbcTemplate.query(sql, new ArgumentPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(new BeanPropertyRowMapper<>(clazz)));
		}
	}
	
	private boolean isSingleColumnClass(Class<?> requiredType){
		if( String.class == requiredType || 
		boolean.class == requiredType ||
		Boolean.class == requiredType ||
		byte.class == requiredType || 
		Byte.class == requiredType ||
		short.class == requiredType || 
		Short.class == requiredType||
		int.class == requiredType || 
		Integer.class == requiredType ||
		long.class == requiredType || 
		Long.class == requiredType||
		float.class == requiredType || 
		Float.class == requiredType||
		double.class == requiredType || 
		Double.class == requiredType || 
		Number.class == requiredType||
		BigDecimal.class == requiredType ||
		java.sql.Date.class == requiredType||
		java.sql.Time.class == requiredType||
		java.sql.Timestamp.class == requiredType || 
		java.util.Date.class == requiredType||
		byte[].class == requiredType||
		Blob.class == requiredType||
		Clob.class == requiredType )
			return true;
		return false;
	}
	
}
