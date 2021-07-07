package com.tomykrisgreen.airbasetabbed.Models

class Story
{
    private var imageurl: String = ""
    private var timestart: Long = 0
    private var timeend: Long = 0
    private var storyid: String = ""
    private var uid: String = ""

    constructor()
    constructor(imageUrl: String, timestart: Long, timeend: Long, storyid: String, uid: String) {
        this.imageurl = imageUrl
        this.timestart = timestart
        this.timeend = timeend
        this.storyid = storyid
        this.uid = uid
    }

    fun getImageUrl(): String{
        return imageurl
    }

    fun getTimeStart(): Long{
        return timestart
    }

    fun getTimeEnd(): Long{
        return timeend
    }

    fun getStoryId(): String{
        return storyid
    }

    fun getUid(): String{
        return uid
    }


    fun setImageUrl(imageUrl: String)
    {
        this.imageurl = imageurl
    }

    fun setTimeStart(timestart: Long)
    {
        this.timestart = timestart
    }

    fun setTimeEnd(timeend: Long)
    {
        this.timeend = timeend
    }

    fun setStoryId(storyid: String)
    {
        this.storyid = storyid
    }

    fun setUid(uid: String)
    {
        this.uid = uid
    }

}