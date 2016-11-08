package com.zy.driverclient.model;

public class UserJson {
    private String msg;
    private String code;
    private Passenger passenger;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    @Override
    public String toString() {
        return "UserJson{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", passenger=" + passenger +
                '}';
    }

    public class Passenger {
        private int id;
        private String username;
        private String password;
        private String md5_key;
        private String comm;  //公司
        private String phone;
        private int state;
        private int black_state;
        private int oldernum;
        private String reg_time;

        public String getComm() {
            return comm;
        }

        public void setComm(String comm) {
            this.comm = comm;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getMd5_key() {
            return md5_key;
        }

        public void setMd5_key(String md5_key) {
            this.md5_key = md5_key;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getBlack_state() {
            return black_state;
        }

        public void setBlack_state(int black_state) {
            this.black_state = black_state;
        }

        public int getOldernum() {
            return oldernum;
        }

        public void setOldernum(int oldernum) {
            this.oldernum = oldernum;
        }

        public String getReg_time() {
            return reg_time;
        }

        public void setReg_time(String reg_time) {
            this.reg_time = reg_time;
        }

        @Override
        public String toString() {
            return "Passenger{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", md5_key='" + md5_key + '\'' +
                    ", phone='" + phone + '\'' +
                    ", state=" + state +
                    ", black_state=" + black_state +
                    ", oldernum=" + oldernum +
                    ", reg_time='" + reg_time + '\'' +
                    '}';
        }
    }


}
