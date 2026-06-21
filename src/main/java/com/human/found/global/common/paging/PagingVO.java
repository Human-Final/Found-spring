package com.human.found.global.common.paging;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PagingVO {
    private int page = 1;
    private int size = 10;
    private int totalCount;
    private int offset;
    private int totalPage;
    private int pageBlock = 10;

    private int startPage;
    private int endPage;

    private boolean prev;
    private boolean next;

    public int getOffset(){
        return (page-1)*size;
    }

    public void pageInfo(int totalCount){
        this.totalCount = totalCount;

        if(totalCount == 0) {
            this.totalPage = 0;
            this.startPage = 0;
            this.endPage = 0;
            this.prev = false;
            this.next = false;
            return;
        }
        
        this.totalPage = (int)Math.ceil((double)totalCount/size);
        this.endPage = (int)Math.ceil((double)page/pageBlock)*pageBlock;
        this.startPage = endPage - pageBlock + 1;

        if(endPage > totalPage){
            endPage = totalPage;
        }
        this.prev = startPage>1;
        this.next = endPage<totalPage;
    }
}
