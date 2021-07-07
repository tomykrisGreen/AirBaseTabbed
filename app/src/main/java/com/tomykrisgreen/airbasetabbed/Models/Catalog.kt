package com.tomykrisgreen.airbasetabbed.Models

class Catalog {
    private var catalogid: String = ""
    private var coverid: String = ""
    private var catalogimage: String = ""
    private var publisher: String = ""
    private var titlecatalog: String = ""
    private var descriptioncatalog: String = ""
    private var profileimagecatalog: String = ""
    private var companydescription: String = ""
    private var companyname: String = ""
    private var productprice: String = ""
    private var uid: String = ""
    private var userid: String=""

    constructor()
    constructor(
            catalogid: String,
            coverid: String,
            catalogimage: String,
            publisher: String,
            titlecatalog: String,
            descriptioncatalog: String,
            profileimagecatalog: String,
            companydescription: String,
            companyname: String,
            productprice: String,
            uid: String,
            userid: String
    ) {
        this.catalogid = catalogid
        this.coverid = coverid
        this.catalogimage = catalogimage
        this.publisher = publisher
        this.titlecatalog = titlecatalog
        this.descriptioncatalog = descriptioncatalog
        this.profileimagecatalog = profileimagecatalog
        this.companydescription = companydescription
        this.companyname = companyname
        this.productprice = productprice
        this.uid = uid
        this.userid = userid
    }

    fun getCatalogid(): String{
        return catalogid
    }

    fun setCatalogid(catalogid: String)
    {
        this.catalogid = catalogid
    }

    fun getCoverid(): String{
        return coverid
    }

    fun setCoverid(coverid: String)
    {
        this.coverid = coverid
    }

    fun getCatalogimage(): String{
        return catalogimage
    }

    fun setCatalogimage(catalogimage: String)
    {
        this.catalogimage = catalogimage
    }

    fun getPublisher(): String{
        return publisher
    }

    fun setPublisher(publisher: String)
    {
        this.publisher = publisher
    }

    fun getTitlecatalog(): String{
        return titlecatalog
    }

    fun setTitlecatalog(titlecatalog: String)
    {
        this.titlecatalog = titlecatalog
    }

    fun getDescriptioncatalog(): String{
        return descriptioncatalog
    }

    fun setDescriptioncatalog(descriptioncatalog: String)
    {
        this.descriptioncatalog = descriptioncatalog
    }

    fun getProfileimagecatalog(): String{
        return profileimagecatalog
    }

    fun setProfileimagecatalog(profileimagecatalog: String)
    {
        this.profileimagecatalog = profileimagecatalog
    }

    fun getCompanydescription(): String{
        return companydescription
    }

    fun setCompanydescription(companydescription: String)
    {
        this.companydescription = companydescription
    }

    fun getCompanyname(): String{
        return companyname
    }

    fun setCompanyname(companyname: String)
    {
        this.companyname = companyname
    }

    fun getProductprice(): String{
        return productprice
    }

    fun setProductprice(productprice: String)
    {
        this.productprice = productprice
    }

    fun getUID(): String
    {
        return uid
    }

    fun setUID(uid: String)
    {
        this.uid = uid
    }

    fun getUserId(): String{
        return userid
    }

    fun setUserId(userid: String)
    {
        this.userid = userid
    }


}