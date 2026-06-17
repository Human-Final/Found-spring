package com.human.found.domain.lost.vo;

public class LostPoliceVO {
    private Long num;
    private String atcId;
    private String id;
    private String lstPlace;
    private String lstPrdtNm;
    private String lstSbjt;
    private String lstYmd;
    private String prdtClNm;
    private String created_at;
    private String updated_at;
    private Integer done;
    private Integer is_deleted;
    
    public LostPoliceVO() {}

    public Long getNum() { return num; }
    public void setNum(Long num) { this.num = num; }

    public String getAtcId() { return atcId; }
    public void setAtcId(String atcId) { this.atcId = atcId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLstPlace() { return lstPlace; }
    public void setLstPlace(String lstPlace) { this.lstPlace = lstPlace; }

    public String getLstPrdtNm() { return lstPrdtNm; }
    public void setLstPrdtNm(String lstPrdtNm) { this.lstPrdtNm = lstPrdtNm; }

    public String getLstSbjt() { return lstSbjt; }
    public void setLstSbjt(String lstSbjt) { this.lstSbjt = lstSbjt; }

    public String getLstYmd() { return lstYmd; }
    public void setLstYmd(String lstYmd) { this.lstYmd = lstYmd; }

    public String getPrdtClNm() { return prdtClNm; }
    public void setPrdtClNm(String prdtClNm) { this.prdtClNm = prdtClNm; }

    public String getCreated() { return created_at; }
    public void setCreated(String created_at) { this.created_at = created_at; }

    public String getUpdated() { return updated_at; }
    public void setUpdated(String updated_at) { this.updated_at = updated_at; }

    public Integer getDone() { return done; }
    public void setDone(Integer done) { this.done = done; }

    public Integer getDeleted() { return is_deleted; }
    public void setDeleted(Integer is_deleted) { this.is_deleted = is_deleted; }
}