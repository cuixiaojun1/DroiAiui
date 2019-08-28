package com.droi.aiui.bean;


import java.util.List;

/**
 * Created by cuixiaojun on 17-12-18.
 */

public class PhoneBean extends BaseBean {


    /**
     * data : {"result":[{"fuzzy_score":1,"hasMorePhoneNumber":false,"location":{"city":"�ൺ��","province":"ɽ��"},"name":"��С��","phoneNumber":"15376420781","teleOper":"����"},{"fuzzy_score":1,"hasMorePhoneNumber":false,"location":{"city":"�ൺ��","province":"ɽ��"},"name":"��С��","phoneNumber":"15376420782","teleOper":"����"},{"fuzzy_score":1,"hasMorePhoneNumber":false,"location":{"city":"�Ϻ�"},"name":"��С��","phoneNumber":"17601385666","teleOper":"��ͨ"}]}
     * semantic : [{"intent":"DIAL","slots":[{"name":"name","value":"��С��"}]}]
     * used_state : {"name":"1","operation":"1","state":"moreNumber","state_key":"fg::telephone::default::moreNumber"}
     * answer : {"text":"���ã���Ϊ���ҵ���С��������룬��ѡ��"}
     * dialog_stat : dataInvalid
     * save_history : true
     */

    private DataBean data;
    private UsedStateBean used_state;
    private PhoneBean answer;
    private String dialog_stat;
    private boolean save_history;
    private List<SemanticBean> semantic;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public UsedStateBean getUsed_state() {
        return used_state;
    }

    public void setUsed_state(UsedStateBean used_state) {
        this.used_state = used_state;
    }

    public PhoneBean getAnswer() {
        return answer;
    }

    public void setAnswer(PhoneBean answer) {
        this.answer = answer;
    }

    public String getDialog_stat() {
        return dialog_stat;
    }

    public void setDialog_stat(String dialog_stat) {
        this.dialog_stat = dialog_stat;
    }

    public boolean isSave_history() {
        return save_history;
    }

    public void setSave_history(boolean save_history) {
        this.save_history = save_history;
    }

    public List<SemanticBean> getSemantic() {
        return semantic;
    }

    public void setSemantic(List<SemanticBean> semantic) {
        this.semantic = semantic;
    }

    public static class DataBean {
        private List<ResultBean> result;

        public List<ResultBean> getResult() {
            return result;
        }

        public void setResult(List<ResultBean> result) {
            this.result = result;
        }

        public static class ResultBean {
            /**
             * fuzzy_score : 1
             * hasMorePhoneNumber : false
             * location : {"city":"�ൺ��","province":"ɽ��"}
             * name : ��С��
             * phoneNumber : 15376420781
             * teleOper : ����
             */

            private int fuzzy_score;
            private boolean hasMorePhoneNumber;
            private LocationBean location;
            private String name;
            private String phoneNumber;
            private String teleOper;

            public int getFuzzy_score() {
                return fuzzy_score;
            }

            public void setFuzzy_score(int fuzzy_score) {
                this.fuzzy_score = fuzzy_score;
            }

            public boolean isHasMorePhoneNumber() {
                return hasMorePhoneNumber;
            }

            public void setHasMorePhoneNumber(boolean hasMorePhoneNumber) {
                this.hasMorePhoneNumber = hasMorePhoneNumber;
            }

            public LocationBean getLocation() {
                return location;
            }

            public void setLocation(LocationBean location) {
                this.location = location;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPhoneNumber() {
                return phoneNumber;
            }

            public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
            }

            public String getTeleOper() {
                return teleOper;
            }

            public void setTeleOper(String teleOper) {
                this.teleOper = teleOper;
            }

            public static class LocationBean {
                /**
                 * city : �ൺ��
                 * province : ɽ��
                 */

                private String city;
                private String province;

                public String getCity() {
                    return city;
                }

                public void setCity(String city) {
                    this.city = city;
                }

                public String getProvince() {
                    return province;
                }

                public void setProvince(String province) {
                    this.province = province;
                }
            }
        }
    }

    public static class UsedStateBean {
        /**
         * name : 1
         * operation : 1
         * state : moreNumber
         * state_key : fg::telephone::default::moreNumber
         */

        private String name;
        private String operation;
        private String state;
        private String state_key;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getState_key() {
            return state_key;
        }

        public void setState_key(String state_key) {
            this.state_key = state_key;
        }
    }

    public static class SemanticBean {
        /**
         * intent : DIAL
         * slots : [{"name":"name","value":"��С��"}]
         */

        private String intent;
        private List<SlotsBean> slots;

        public String getIntent() {
            return intent;
        }

        public void setIntent(String intent) {
            this.intent = intent;
        }

        public List<SlotsBean> getSlots() {
            return slots;
        }

        public void setSlots(List<SlotsBean> slots) {
            this.slots = slots;
        }

        public static class SlotsBean {
            /**
             * name : name
             * value : ��С��
             */

            private String name;
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}