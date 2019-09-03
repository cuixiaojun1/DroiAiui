package com.droi.aiui.bean;

import java.util.List;

/**
 * Created by cuixiaojun on 18-1-13.
 */

public class RemindBean extends BaseBean {

    /**
     * semantic : [{"intent":"CREATE","slots":[{"name":"content","value":"起床"},{"name":"datetime","value":"早上8点","normValue":"{\"datetime\":\"T08:00:00\",\"suggestDatetime\":\"2018-03-22T08:00:00\"}"},{"name":"name","value":"clock"},{"name":"repeat","value":"EVERYDAY"}]}]
     * used_state : {"content":"1","datetime.INTERVAL":"1","datetime.time":"1","name":"1","operation":"1","param:repeat_key":"1","state":"clockFinished","state_key":"fg::scheduleX::default::clockFinished"}
     * answer : {"text":"好的，每天早上8点我都会提醒您"}
     */

    private UsedStateBean used_state;
    private RemindBean answer;
    private List<SemanticBean> semantic;

    public UsedStateBean getUsed_state() {
        return used_state;
    }

    public void setUsed_state(UsedStateBean used_state) {
        this.used_state = used_state;
    }

    public RemindBean getAnswer() {
        return answer;
    }

    public void setAnswer(RemindBean answer) {
        this.answer = answer;
    }

    public List<SemanticBean> getSemantic() {
        return semantic;
    }

    public void setSemantic(List<SemanticBean> semantic) {
        this.semantic = semantic;
    }

    public static class UsedStateBean {
        /**
         * state : clockFinished
         */
        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    public static class SemanticBean {
        /**
         * intent : CREATE
         * slots : [{"name":"content","value":"起床"},{"name":"datetime","value":"早上8点","normValue":"{\"datetime\":\"T08:00:00\",\"suggestDatetime\":\"2018-03-22T08:00:00\"}"},{"name":"name","value":"clock"},{"name":"repeat","value":"EVERYDAY"}]
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
             * name : content
             * value : 起床
             * normValue : {"datetime":"T08:00:00","suggestDatetime":"2018-03-22T08:00:00"}
             */

            private String name;
            private String value;
            private String normValue;

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

            public String getNormValue() {
                return normValue;
            }

            public void setNormValue(String normValue) {
                this.normValue = normValue;
            }
        }
    }
}