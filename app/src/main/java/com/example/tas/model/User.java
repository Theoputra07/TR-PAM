package com.example.tas.model;

public class User {
    private String id,name, harga, keterangan,avatar;
    public User(){

    }
    public User(String name, String harga, String keterangan, String avatar){
        this.name =name;
        this.harga=harga;
        this.keterangan=keterangan;
        this.avatar=avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
