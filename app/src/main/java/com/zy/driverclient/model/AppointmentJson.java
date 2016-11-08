package com.zy.driverclient.model;

/**
 * Created by blurryFace on 2016/8/16.
 */
public class AppointmentJson {
    private String msg;
    private Tailored tailored;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Tailored getTailored() {
        return tailored;
    }

    public void setTailored(Tailored tailored) {
        this.tailored = tailored;
    }

    public class Tailored{
        private int id;
        private String name; //称呼
        private String phone;
        private String longitude; //经度
        private String latitude; //纬度
        private String address;  //出发点
        private int state;  //订单状态  1-发布  2-取消  3-超时
        private String time; // yyyy-MM-dd HH:mm:ss
        private String receive_phone;

        @Override
        public String toString() {
            return "Tailored{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    ", longitude='" + longitude + '\'' +
                    ", latitude='" + latitude + '\'' +
                    ", address='" + address + '\'' +
                    ", state=" + state +
                    ", time='" + time + '\'' +
                    ", receive_phone='" + receive_phone + '\'' +
                    '}';
        }

        public String getReceive_phone() {
            return receive_phone;
        }

        public void setReceive_phone(String receive_phone) {
            this.receive_phone = receive_phone;
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

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
