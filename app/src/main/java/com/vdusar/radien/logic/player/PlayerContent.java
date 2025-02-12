package com.vdusar.radien.logic.player;


public class PlayerContent{
    public String Title ;
    public String Logo ;
    public String Background_Url;
    public String Discription ;
    public Object Languages;
    public String Genre;
    public String Channel;
    public String streamUrl;
    public String q_url;
    public String key;

    public PlayerContent(String key,String title,String logo,String burl,String disc,Object lan,String genre,String channel,String stream_url,String q_Url){
        this.Languages=lan;
        this.Background_Url=burl;
        this.Discription=disc;
        this.Logo=logo;
        this.Title=title;
        this.Genre=genre;
        this.Channel=channel;
        this.streamUrl=stream_url;
        this.q_url=q_Url;
        this.key = key;
    }

    public String getQ_url() {
        return q_url;
    }

    public String getLogo() {
        return Logo;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getBackground_Url() {
        return Background_Url;
    }

    public Object getLanguages() {
        return Languages;
    }

    public String getDiscription() {
        return Discription;
    }

    public String getGenre() {
        return Genre;
    }

    public String getTitle() {
        return Title;
    }

    public String getChannel() {
        return Channel;
    }
}
