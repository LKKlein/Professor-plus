package cn.edu.swufe.fife.professor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import cn.bmob.v3.BmobObject;

/**
 * Created by Klein on 2017/5/4.
 */

public class Professor {
    private String name, university, web_page, url, path_name;

    public Professor(){
        super();
    }

    public Professor(String name, String university, String web_page, String path, String path_name) {
        try {
            this.name = URLEncoder.encode(name, "utf-8");
            this.university = URLEncoder.encode(university, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.web_page = web_page;
        this.url = path;
        this.path_name = path_name;
    }

    public String getName() {
        try {
            return URLDecoder.decode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniversity() {
        try {
            return URLDecoder.decode(university, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
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

    public String getPath_name() {
        return path_name;
    }

    public void setPath_name(String path_name) {
        this.path_name = path_name;
    }
}
