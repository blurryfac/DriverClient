package com.zy.driverclient.model;

/**
 * Created by blurryFace on 2016/9/8.
 */
public class VersionMessage {
    private int msg;
    private Version version;

    public int getMsg() {
        return msg;
    }

    public void setMsg(int msg) {
        this.msg = msg;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public class Version{
        private String name; //apk名字   xxxx.apk
        private int versionCode; //版本号   1
        private String versionName; //版本号字符 1.0.1
        private String information;//说明
        private String url;//下载url http://192.168.1.101:8080/app/汴交通司机端.apk

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getInformation() {
            return information;
        }

        public void setInformation(String information) {
            this.information = information;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
