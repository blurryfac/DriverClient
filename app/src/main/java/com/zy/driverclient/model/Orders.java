package com.zy.driverclient.model;

/**
 * Created by blurryFace on 2016/8/19.
 */
public class Orders {
    private Ios ios;
    private Order order;

    @Override
    public String toString() {
        return "Orders{" +
                "ios=" + ios +
                ", order=" + order +
                '}';
    }

    public Ios getIos() {
        return ios;
    }

    public void setIos(Ios ios) {
        this.ios = ios;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public class Ios{
        private String sound;
        private String bage;

        public String getSound() {
            return sound;
        }

        public void setSound(String sound) {
            this.sound = sound;
        }

        public String getBage() {
            return bage;
        }

        public void setBage(String bage) {
            this.bage = bage;
        }
    }

    public class Order{
        private int id;
        private String phone;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "id='" + id + '\'' +
                    ", phone='" + phone + '\'' +
                    '}';
        }
    }


}
