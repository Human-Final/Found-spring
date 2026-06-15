package com.human.found.domain.lost.vo;

public class LostVO {
    private Long num;
    private String atcId;
    private String id;
    private String lstPlace;
    private String lstPrdtNum;
    private String lstSbjt;
    private String lstYmd;
    private String prdtClNum;
    private String created;
    private String updated;
    private Integer done;
    private Integer deleted;

    public LostVO() {}

    public Long getNum() { return num; }
    public void setNum(Long num) { this.num = num; }

    public String getAtcId() { return atcId; }
    public void setAtcId(String atcId) { this.atcId = atcId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLstPlace() { return lstPlace; }
    public void setLstPlace(String lstPlace) { this.lstPlace = lstPlace; }

    public String getLstPrdtNum() { return lstPrdtNum; }
    public void setLstPrdtNum(String lstPrdtNum) { this.lstPrdtNum = lstPrdtNum; }

    public String getLstSbjt() { return lstSbjt; }
    public void setLstSbjt(String lstSbjt) { this.lstSbjt = lstSbjt; }

    public String getLstYmd() { return lstYmd; }
    public void setLstYmd(String lstYmd) { this.lstYmd = lstYmd; }

    public String getPrdtClNum() { return prdtClNum; }
    public void setPrdtClNum(String prdtClNum) { this.prdtClNum = prdtClNum; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }

    public String getUpdated() { return updated; }
    public void setUpdated(String updated) { this.updated = updated; }

    public Integer getDone() { return done; }
    public void setDone(Integer done) { this.done = done; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}