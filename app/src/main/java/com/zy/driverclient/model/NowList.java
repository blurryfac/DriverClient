package com.zy.driverclient.model;

import java.util.List;

/**
 * Created by blurryFace on 2016/8/28.
 */
public class NowList {
    private String msg;
    private String rows;
    private String page;
    private List<Content> content;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public class Content {
        private int id;
        private String name;
        private String phone;
        private String longitude;
        private String latitude;
        private String address;
        private int state;
        private String time;
        private String receive_phone;
        private String delinfo;
        private String timestamp;


        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getReceive_phone() {
            return receive_phone;
        }

        public void setReceive_phone(String receive_phone) {
            this.receive_phone = receive_phone;
        }

        public String getDelinfo() {
            return delinfo;
        }

        public void setDelinfo(String delinfo) {
            this.delinfo = delinfo;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
