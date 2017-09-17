package cn.edu.swufe.fife.professor.bean;

import android.content.Context;

import cn.edu.swufe.fife.professor.BaseApplication;
import cn.edu.swufe.fife.professor.Utils.CommonUtils;

/**
 * Created by Klein on 2017/9/16.
 */

public class FavoriteItem {
    private String pid;
    private String name;
    private String uni;
    private String web_url;
    private String local_path;
    private String url;
    private String story;
    private String tags;
    private String ptime;
    private String place_name;

    private ProfessorsDao professorsDao;

    public FavoriteItem() {
    }

    public FavoriteItem(Context mContext, FacesGallery facesGallery) {
        DaoSession daoSession = ((BaseApplication) mContext).getDaoSession();
        professorsDao = daoSession.getProfessorsDao();
        setFromFaceGallery(facesGallery);
    }

    public FavoriteItem(String pid, String name, String uni, String web_url, String local_path, String url, String story, String tags, String ptime, String place_name) {
        this.pid = pid;
        this.name = name;
        this.uni = uni;
        this.web_url = web_url;
        this.local_path = local_path;
        this.url = url;
        this.story = story;
        this.tags = tags;
        this.ptime = ptime;
        this.place_name = place_name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUni() {
        return uni;
    }

    public void setUni(String uni) {
        this.uni = uni;
    }

    public String getWeb_url() {
        return web_url;
    }

    public void setWeb_url(String web_url) {
        this.web_url = web_url;
    }

    public String getLocal_path() {
        return local_path;
    }

    public void setLocal_path(String local_path) {
        this.local_path = local_path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPtime() {
        return ptime;
    }

    public void setPtime(String ptime) {
        this.ptime = ptime;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public void setFromFaceGallery(FacesGallery faceGallery) {
        setPid(faceGallery.getPid());
        setStory(faceGallery.getStory());
        setLocal_path(faceGallery.getLocalpath());
        setUrl(faceGallery.getPUrl());
        setTags(faceGallery.getTags());
        setPtime(CommonUtils.getTimeDate(faceGallery.getPtime()));
        setPlace_name(faceGallery.getPlace_name());
        Professors professors = professorsDao.queryBuilder()
                .where(ProfessorsDao.Properties.Face_token.eq(faceGallery.getFace_token()))
                .build()
                .unique();
        setName(professors.getName());
        setUni(professors.getUniversity());
        setWeb_url(professors.getWeb_url());

    }
}
