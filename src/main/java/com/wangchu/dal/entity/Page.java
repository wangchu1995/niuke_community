package com.wangchu.dal.entity;



public class Page {
    //分页相关的参数配置项,保存页数相关信息
    //帖子数据库信息可以在controller里面计算，所以不保存
    //当前页码
    private int current = 1;
    //每页显示数
    private int showItems = 10;
    //总页数可以计算得到
    //总数据数
    private int totalItems;
    //默认路径
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
        this.current = current;
    }}

    public int getShowItems() {
        return showItems;
    }


    public void setShowItems(int showItems) {
        if(showItems>0&&showItems<100){
        this.showItems = showItems;
    }}

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        if(totalItems>0){
        this.totalItems = totalItems;
    }}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



    //获取总页数
    public int getTotalPages(){
        //总页数=总条目数/每页显示数
        if(totalItems%showItems==0){
            return totalItems/showItems;
        }else{
            return totalItems/showItems+1;
        }
    }
    //获取起始行
    public int getOffset(){
        return (current-1)*showItems;
    }

    //获取起始页码
    public int getFrom(){
        if(getTo()>=getTotalPages()-1){
            return Math.max(1,getTotalPages()-4);
        }
        int from = current-2;
        return from<1?1:from;
    }
    //获取结束页码
    public int getTo(){
        if(current<=2){
            return Math.min(5,getTotalPages());
        }
        int to = current+2;
        int total = getTotalPages();
        return to>total?total:to;
    }

}
