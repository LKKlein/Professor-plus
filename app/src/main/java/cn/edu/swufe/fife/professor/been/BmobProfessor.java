package cn.edu.swufe.fife.professor.been;

import cn.bmob.v3.BmobObject;

/**
 * Created by Klein on 2017/7/5.
 */

public class BmobProfessor extends BmobObject {
    private String face_token, faceset_id, name, university, web_page, url;

//    public BmobProfessor(){
//        this.setTableName("professor");
//    }

    public String getFace_token() {
        return face_token;
    }

    public void setFace_token(String face_token) {
        this.face_token = face_token;
    }

    public String getFaceset_id() {
        return faceset_id;
    }

    public void setFaceset_id(String faceset_id) {
        this.faceset_id = faceset_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getWeb_page() {
        return web_page;
    }

    public void setWeb_page(String web_page) {
        this.web_page = web_page;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
