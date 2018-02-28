package com.tcl.choujiang.choujiang;

/**
 * Created by kai.you on 2018/1/24.
 */

public class JiangXiang {

    String jiangXiang = "";
    String count = "";

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    String last = "";

    public void setJiangXiang(String jiangXiang) {
        this.jiangXiang = jiangXiang;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getJiangXiang() {
        return jiangXiang;
    }

    public String getCount() {
        return count;
    }

    public JiangXiang(String jx, String c, String l) {
        jiangXiang = jx;
        count = c;
        last = l;
    }

}
