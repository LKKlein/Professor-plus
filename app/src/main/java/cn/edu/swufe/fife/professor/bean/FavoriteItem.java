package cn.edu.swufe.fife.professor.bean;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import cn.edu.swufe.fife.professor.BaseApplication;
import cn.edu.swufe.fife.professor.Utils.CommonUtils;

/**
 * Created by Klein on 2017/9/16.
 */

public class FavoriteItem implements Parcelable {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pid);
        dest.writeString(this.name);
        dest.writeString(this.uni);
        dest.writeString(this.web_url);
        dest.writeString(this.local_path);
        dest.writeString(this.url);
        dest.writeString(this.story);
        dest.writeString(this.tags);
        dest.writeString(this.ptime);
        dest.writeString(this.place_name);
    }

    protected FavoriteItem(Parcel in) {
        this.pid = in.readString();
        this.name = in.readString();
        this.uni = in.readString();
        this.web_url = in.readString();
        this.local_path = in.readString();
        this.url = in.readString();
        this.story = in.readString();
        this.tags = in.readString();
        this.ptime = in.readString();
        this.place_name = in.readString();
    }

    public static final Parcelable.Creator<FavoriteItem> CREATOR = new Parcelable.Creator<FavoriteItem>() {
        @Override
        public FavoriteItem createFromParcel(Parcel source) {
            return new FavoriteItem(source);
        }

        @Override
        public FavoriteItem[] newArray(int size) {
            return new FavoriteItem[size];
        }
    };
}
