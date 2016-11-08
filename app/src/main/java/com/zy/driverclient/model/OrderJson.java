package com.zy.driverclient.model;

/**
 * Created by blurryFace on 2016/8/18.
 */
public class OrderJson {
    private String msg;
    private Orders orders;


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public class Orders {
        private int id;
        private String name;
        private String phone;
        private double longitude;
        private double latitude;
        private String address;
        private String state;
        private String receive_phone;
        private String time;
        private String timeStamp;

        @Override
        public String toString() {
            return "Orders{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    ", longitude='" + longitude + '\'' +
                    ", latitude='" + latitude + '\'' +
                    ", state='" + state + '\'' +
                    ", receive_phone='" + receive_phone + '\'' +
                    ", time='" + time + '\'' +
                    ", timeStamp='" + timeStamp + '\'' +
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

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
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

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }
    }
}
