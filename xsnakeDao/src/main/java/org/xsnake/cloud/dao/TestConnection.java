package org.xsnake.cloud.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestConnection implements InitializingBean {

	private static final Log logger = LogFactory.getLog(TestConnection.class);
	
	@Autowired
	DaoTemplate daoTemplate;
	
	@Autowired
	Dialect dialect;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		try{
			logger.info("开始测试数据库连接");
			daoTemplate.queryObject(dialect.getTestSql(),String.class);
			logger.info("测试数据库连接正常");
		}catch (Exception e) {
			logger.error("测试数据库连接异常" + e.getMessage());
			throw e;
		}
	}

}
