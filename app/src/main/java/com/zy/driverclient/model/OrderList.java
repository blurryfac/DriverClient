package com.zy.driverclient.model;

import java.util.List;

/**
 * Created by blurryFace on 2016/8/16.
 */
public class OrderList {
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

    @Override
    public String toString() {
        return "OrderList{" +
                "msg='" + msg + '\'' +
                ", rows='" + rows + '\'' +
                ", page='" + page + '\'' +
                ", content=" + content +
                '}';
    }

    public class Content{
        private int id;
        private String name; //称呼
        private String phone;
        private String start;  //出发点
        private String address;
        private int type;  //订单状态  1-发布  2-取消  3-超时
        private String seat; // yyyy-MM-dd HH:mm:ss
        private int state;
        private String receive_phone;
        private String time ;
        private String order_time;
        private String timestamp;

        @Override
        public String toString() {
            return "Content{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    ", start='" + start + '\'' +
                    ", type=" + type +
                    ", seat='" + seat + '\'' +
                    ", state=" + state +
                    ", receive_phone='" + receive_phone + '\'' +
                    ", time='" + time + '\'' +
                    ", order_time='" + order_time + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
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

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getSeat() {
            return seat;
        }

        public void setSeat(String seat) {
            this.seat = seat;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getReceive_phone() {
            return receive_phone;
        }

        public void setReceive_phone(String receive_phone) {
            this.receive_phone = receive_phone;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getOrder_time() {
            return order_time;
        }

        public void setOrder_time(String order_time) {
            this.order_time = order_time;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
