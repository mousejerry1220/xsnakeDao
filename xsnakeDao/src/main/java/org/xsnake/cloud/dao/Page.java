package org.xsnake.cloud.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	int pageNumber = 0;
	
	int pageSize = 0;
	
	int count = 0;
	
	int pageCount = 0;
	
	int pageNext = 0;
	
	int pagePrevious = 0;
	
	int pageCurrent = 0;

	List<T> list = null;
	
	public Page(){}
	
	public Page(List<T> list,int pageNumber,int pageSize,int count ){
		
		if(list==null){
			
			list = new ArrayList<>();
			
		}
		
		this.list = list;
		
		
		this.pageNumber = pageNumber;
		
		this.pageSize = pageSize;
		
		this.count = count;
		
		this.pageCount = (count / pageSize) + (count % pageSize == 0 ? 0:1);
		
		if (this.pageCount == 0) {
			
			this.pageCount = 1;
			
			this.pageCurrent = 1;
			
		} else {
			
			this.pageCurrent = (pageNumber <= pageCount) ? (pageNumber > 0 ? pageNumber : 1) : pageCount;
			
		}
		
		
		this.pageNext = (pageCurrent+1>pageCount)?pageCount:(pageCurrent+1);
		
		this.pagePrevious = (pageCurrent-1<1)?1:(pageCurrent-1);
		
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public int getCount() {
		return count;
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getPageNext() {
		return pageNext;
	}

	public int getPagePrevious() {
		return pagePrevious;
	}

	public int getPageCurrent() {
		return pageCurrent;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
	
}
